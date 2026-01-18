package com.budgetpro.infrastructure.rest.produccion;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.produccion.ReporteProduccionJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para Reportes de Producción (RPC).
 */
@SuppressWarnings("null")
class ReporteProduccionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private PartidaJpaRepository partidaJpaRepository;

    @Autowired
    private ReporteProduccionJpaRepository reporteProduccionJpaRepository;

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID partidaId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        reporteProduccionJpaRepository.deleteAll();
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();

        ProyectoEntity proyecto = new ProyectoEntity(
                UUID.randomUUID(),
                "Proyecto RPC Test",
                "San Salvador",
                com.budgetpro.domain.proyecto.model.EstadoProyecto.EJECUCION,
                null
        );
        proyecto = proyectoJpaRepository.save(proyecto);
        proyectoId = proyecto.getId();

        PresupuestoEntity presupuesto = new PresupuestoEntity(
                UUID.randomUUID(),
                proyectoId,
                "Presupuesto Base",
                com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.BORRADOR,
                false,
                null
        );
        presupuesto = presupuestoJpaRepository.save(presupuesto);
        PartidaEntity partida = new PartidaEntity(
                UUID.randomUUID(),
                presupuesto,
                null,
                "01.01",
                "Partida Test",
                "m2",
                new BigDecimal("100.00"),
                1,
                null
        );
        partida = partidaJpaRepository.save(partida);
        partidaId = partida.getId();
    }

    @Test
    void crearReporte_ok() throws Exception {
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "responsableId": "%s",
                  "comentario": "Avance diario",
                  "ubicacionGps": "13.70,-89.21",
                  "detalles": [
                    { "partidaId": "%s", "cantidadReportada": 20.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), UUID.randomUUID(), partidaId);

        mockMvc.perform(post("/api/v1/produccion/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.detalles[0].partidaId").value(partidaId.toString()))
                .andExpect(jsonPath("$.detalles[0].cantidadReportada").value(20.0));
    }

    @Test
    void excesoMetrado_bloquea() throws Exception {
        // Reporte aprobado previo de 90
        ReporteProduccionEntity aprobado = new ReporteProduccionEntity();
        aprobado.setId(UUID.randomUUID());
        aprobado.setFechaReporte(LocalDate.now().minusDays(2));
        aprobado.setResponsableId(UUID.randomUUID());
        aprobado.setEstado(EstadoReporteProduccion.APROBADO);

        DetalleRPCEntity detalle = new DetalleRPCEntity();
        detalle.setId(UUID.randomUUID());
        detalle.setReporteProduccion(aprobado);
        detalle.setPartida(partidaJpaRepository.findById(partidaId).orElseThrow());
        detalle.setCantidadReportada(new BigDecimal("90.00"));
        aprobado.setDetalles(List.of(detalle));

        reporteProduccionJpaRepository.save(aprobado);

        // Intentar reportar 20 adicionales (total 110 > 100)
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "responsableId": "%s",
                  "comentario": "Avance diario",
                  "detalles": [
                    { "partidaId": "%s", "cantidadReportada": 20.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), UUID.randomUUID(), partidaId);

        mockMvc.perform(post("/api/v1/produccion/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio."
                ));
    }

    @Test
    void aprobado_no_permite_actualizar() throws Exception {
        // Crear reporte
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "responsableId": "%s",
                  "detalles": [
                    { "partidaId": "%s", "cantidadReportada": 10.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), UUID.randomUUID(), partidaId);

        String response = mockMvc.perform(post("/api/v1/produccion/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID reporteId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        // Aprobar
        String aprobarPayload = String.format("""
                { "aprobadorId": "%s" }
                """, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/produccion/reportes/{id}/aprobar", reporteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(aprobarPayload))
                .andExpect(status().isOk());

        // Intentar actualizar
        String updatePayload = String.format("""
                {
                  "fechaReporte": "%s",
                  "responsableId": "%s",
                  "comentario": "No permitido",
                  "detalles": [
                    { "partidaId": "%s", "cantidadReportada": 15.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), UUID.randomUUID(), partidaId);

        mockMvc.perform(put("/api/v1/produccion/reportes/{id}", reporteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Un reporte aprobado es inmutable. Debe crear una Nota de Crédito o un Reporte Deductivo para corregir."
                ));
    }

    @Test
    void fechaFutura_bloquea() throws Exception {
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "responsableId": "%s",
                  "detalles": [
                    { "partidaId": "%s", "cantidadReportada": 5.0 }
                  ]
                }
                """, LocalDate.now().plusDays(1), UUID.randomUUID(), partidaId);

        mockMvc.perform(post("/api/v1/produccion/reportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La fecha del reporte no puede ser futura."));
    }
}
