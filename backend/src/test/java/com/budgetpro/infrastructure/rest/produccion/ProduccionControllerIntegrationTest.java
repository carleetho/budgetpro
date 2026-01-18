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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para API REST de Producción (RPC).
 */
@SuppressWarnings("null")
class ProduccionControllerIntegrationTest extends AbstractIntegrationTest {

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
                "Proyecto Producción API",
                "San Salvador",
                com.budgetpro.domain.proyecto.model.EstadoProyecto.ACTIVO,
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
    void crearReporte_pendiente_ok() throws Exception {
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "items": [
                    { "partidaId": "%s", "cantidad": 10.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), partidaId);

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/produccion", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].partidaId").value(partidaId.toString()))
                .andExpect(jsonPath("$.items[0].cantidad").value(10.0));
    }

    @Test
    void excesoMetrado_conflict() throws Exception {
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

        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "items": [
                    { "partidaId": "%s", "cantidad": 20.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), partidaId);

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/produccion", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio."
                ));
    }

    @Test
    void aprobar_y_rechazar() throws Exception {
        String payload = String.format("""
                {
                  "fechaReporte": "%s",
                  "items": [
                    { "partidaId": "%s", "cantidad": 10.0 }
                  ]
                }
                """, LocalDate.now().minusDays(1), partidaId);

        String response = mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/produccion", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID reporteId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        mockMvc.perform(patch("/api/v1/produccion/{id}/aprobar", reporteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        String rejectPayload = """
                { "motivo": "Falta evidencia" }
                """;
        mockMvc.perform(patch("/api/v1/produccion/{id}/rechazar", reporteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rejectPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Solo se puede rechazar un reporte en estado PENDIENTE."));
    }
}
