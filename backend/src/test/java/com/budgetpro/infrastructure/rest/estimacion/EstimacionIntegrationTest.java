package com.budgetpro.infrastructure.rest.estimacion;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el módulo de estimaciones y valuaciones.
 */
class EstimacionIntegrationTest extends AbstractIntegrationTest {

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

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto y Partida
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Estimaciones",
            "San Salvador, El Salvador",
            com.budgetpro.domain.proyecto.model.EstadoProyecto.EJECUCION,
            null
        );
        proyecto = proyectoJpaRepository.save(proyecto);
        proyectoId = proyecto.getId();

        PresupuestoEntity presupuesto = new PresupuestoEntity(
            UUID.randomUUID(),
            proyectoId,
            "Presupuesto Base",
            com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.APROBADO,
            true, // esContractual = true (presupuesto aprobado)
            null
        );
        presupuesto = presupuestoJpaRepository.save(presupuesto);
        presupuestoId = presupuesto.getId();

        // Partida: $100,000 (100 m2 a $1,000/m2)
        PartidaEntity partida = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "01",
            "Muro de concreto",
            "m2",
            new BigDecimal("100.00"), // 100 m2
            1,
            null
        );
        partida = partidaJpaRepository.save(partida);
        partidaId = partida.getId();
    }

    @Test
    void testGenerarEstimacion_ConAmortizacionYRetencion_CalculaCorrectamente() throws Exception {
        // Fecha base: 2026-01-15
        LocalDate fechaCorte = LocalDate.of(2026, 1, 15);
        LocalDate periodoInicio = LocalDate.of(2026, 1, 1);
        LocalDate periodoFin = LocalDate.of(2026, 1, 15);

        // 1. Generar Estimación 1 por $50,000 de avance (50 m2 a $1,000/m2)
        // Anticipo: 30%, Retención: 5%
        String estimacionJson = String.format("""
                {
                    "fechaCorte": "%s",
                    "periodoInicio": "%s",
                    "periodoFin": "%s",
                    "detalles": [
                        {
                            "partidaId": "%s",
                            "cantidadAvance": 50.00,
                            "precioUnitario": 1000.00
                        }
                    ],
                    "porcentajeAnticipo": 30.00,
                    "porcentajeRetencionFondoGarantia": 5.00
                }
                """, fechaCorte, periodoInicio, periodoFin, partidaId);

        String estimacionResponseJson = mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/estimaciones", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimacionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$.numeroEstimacion").value(1))
                .andExpect(jsonPath("$.montoBruto").exists())
                .andExpect(jsonPath("$.amortizacionAnticipo").exists())
                .andExpect(jsonPath("$.retencionFondoGarantia").exists())
                .andExpect(jsonPath("$.montoNetoPagar").exists())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode estimacionNode = objectMapper.readTree(estimacionResponseJson);
        
        // 2. Verificar cálculos:
        // Monto Bruto: $50,000 (50 m2 * $1,000/m2)
        BigDecimal montoBruto = new BigDecimal(estimacionNode.get("montoBruto").asText());
        assertThat(montoBruto).isEqualByComparingTo(new BigDecimal("50000.00"));

        // Amortización Anticipo: $15,000 (30% de $50,000)
        BigDecimal amortizacionAnticipo = new BigDecimal(estimacionNode.get("amortizacionAnticipo").asText());
        assertThat(amortizacionAnticipo).isEqualByComparingTo(new BigDecimal("15000.00"));

        // Retención (5%): $2,500 (5% de $50,000)
        BigDecimal retencionFondoGarantia = new BigDecimal(estimacionNode.get("retencionFondoGarantia").asText());
        assertThat(retencionFondoGarantia).isEqualByComparingTo(new BigDecimal("2500.00"));

        // A Pagar: $32,500 ($50,000 - $15,000 - $2,500)
        BigDecimal montoNetoPagar = new BigDecimal(estimacionNode.get("montoNetoPagar").asText());
        assertThat(montoNetoPagar).isEqualByComparingTo(new BigDecimal("32500.00"));

        UUID estimacionId = UUID.fromString(estimacionNode.get("id").asText());

        // 3. Aprobar Estimación
        mockMvc.perform(put("/api/v1/proyectos/estimaciones/{estimacionId}/aprobar", estimacionId))
                .andExpect(status().isNoContent());

        // 4. Verificar que el estado cambió a APROBADA
        // (Nota: Necesitaríamos un endpoint GET para verificar, pero por ahora asumimos que funciona)

        // 5. Verificar saldo en Billetera
        // (Nota: Necesitaríamos un endpoint GET /billetera para verificar, pero por ahora asumimos que funciona)
        // El saldo debería ser $32,500 (monto neto a pagar)
    }
}
