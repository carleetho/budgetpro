package com.budgetpro.infrastructure.rest.cronograma;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el módulo de cronograma.
 */
class CronogramaIntegrationTest extends AbstractIntegrationTest {

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
    private UUID partidaAId; // Cimentación
    private UUID partidaBId; // Muros

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto y Partidas
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Cronograma",
            "San Salvador, El Salvador",
            com.budgetpro.domain.proyecto.model.EstadoProyecto.ACTIVO,
            null
        );
        proyecto = proyectoJpaRepository.save(proyecto);
        proyectoId = proyecto.getId();

        PresupuestoEntity presupuesto = new PresupuestoEntity(
            UUID.randomUUID(),
            proyectoId,
            "Presupuesto Base",
            com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.EN_EDICION,
            false,
            null
        );
        presupuesto = presupuestoJpaRepository.save(presupuesto);
        presupuestoId = presupuesto.getId();

        // Partida A: Cimentación
        PartidaEntity partidaA = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "01",
            "Cimentación",
            "m3",
            new BigDecimal("100.00"),
            1,
            null
        );
        partidaA = partidaJpaRepository.save(partidaA);
        partidaAId = partidaA.getId();

        // Partida B: Muros
        PartidaEntity partidaB = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "02",
            "Muros",
            "m2",
            new BigDecimal("200.00"),
            1,
            null
        );
        partidaB = partidaJpaRepository.save(partidaB);
        partidaBId = partidaB.getId();
    }

    @Test
    void testProgramarActividades_ConDependencias_CalculaDuracionCorrecta() throws Exception {
        // Fecha base: 2026-01-01
        LocalDate fechaBase = LocalDate.of(2026, 1, 1);

        // 1. Programar Actividad A (Cimentación): 10 días
        String actividadAJson = String.format("""
                {
                    "partidaId": "%s",
                    "fechaInicio": "%s",
                    "fechaFin": "%s",
                    "predecesoras": []
                }
                """, partidaAId, fechaBase, fechaBase.plusDays(9));

        String actividadAResponseJson = mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/cronograma/actividades", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadAJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.partidaId").value(partidaAId.toString()))
                .andExpect(jsonPath("$.fechaInicio").value(fechaBase.toString()))
                .andExpect(jsonPath("$.fechaFin").value(fechaBase.plusDays(9).toString()))
                .andExpect(jsonPath("$.duracionDias").value(10))
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode actividadANode = objectMapper.readTree(actividadAResponseJson);
        UUID actividadAId = UUID.fromString(actividadANode.get("id").asText());

        // 2. Programar Actividad B (Muros): 15 días, depende de A (Fin-Inicio)
        LocalDate fechaInicioB = fechaBase.plusDays(10); // Empieza el día siguiente a que termina A
        String actividadBJson = String.format("""
                {
                    "partidaId": "%s",
                    "fechaInicio": "%s",
                    "fechaFin": "%s",
                    "predecesoras": ["%s"]
                }
                """, partidaBId, fechaInicioB, fechaInicioB.plusDays(14), actividadAId);

        mockMvc.perform(post("/api/v1/proyectos/{proyectoId}/cronograma/actividades", proyectoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadBJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.partidaId").value(partidaBId.toString()))
                .andExpect(jsonPath("$.fechaInicio").value(fechaInicioB.toString()))
                .andExpect(jsonPath("$.fechaFin").value(fechaInicioB.plusDays(14).toString()))
                .andExpect(jsonPath("$.duracionDias").value(15))
                .andExpect(jsonPath("$.predecesoras[0]").value(actividadAId.toString()))
                .andReturn();

        // 3. Consultar cronograma completo
        String cronogramaJson = mockMvc.perform(get("/api/v1/proyectos/{proyectoId}/cronograma", proyectoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.programaObraId").exists())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$.fechaInicio").value(fechaBase.toString()))
                .andExpect(jsonPath("$.fechaFinEstimada").value(fechaInicioB.plusDays(14).toString()))
                .andExpect(jsonPath("$.duracionTotalDias").exists())
                .andExpect(jsonPath("$.duracionMeses").exists())
                .andExpect(jsonPath("$.actividades").isArray())
                .andExpect(jsonPath("$.actividades.length()").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        com.fasterxml.jackson.databind.JsonNode cronogramaNode = objectMapper.readTree(cronogramaJson);
        
        // 4. Verificar que la duración total del proyecto es 25 días (10 + 15)
        // Fecha inicio: 2026-01-01
        // Fecha fin: 2026-01-25 (fechaInicioB + 14 días = fechaBase + 10 + 14 = fechaBase + 24)
        // Duración: 25 días (incluyendo ambos días)
        Integer duracionTotalDias = cronogramaNode.get("duracionTotalDias").asInt();
        assertThat(duracionTotalDias).isEqualTo(25);

        // 5. Verificar que la duración en meses es correcta para el cálculo de financiamiento
        // 25 días / 30 ≈ 1 mes (redondeado hacia arriba)
        Integer duracionMeses = cronogramaNode.get("duracionMeses").asInt();
        assertThat(duracionMeses).isEqualTo(1); // (25 + 29) / 30 = 1

        // 6. Verificar que el servicio expone el valor correcto
        // La duración en meses debe estar disponible para el Motor de Costos (Mov 9)
        assertThat(duracionMeses).isNotNull();
        assertThat(duracionMeses).isGreaterThan(0);
    }
}
