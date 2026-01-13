package com.budgetpro.infrastructure.rest.avance;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el registro de avance físico.
 */
class AvanceFisicoIntegrationTest extends AbstractIntegrationTest {

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

        // Setup: Crear Proyecto, Presupuesto y Partida "Muro" con Metrado Total 100 m2
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Avance",
            "Lima, Perú",
            com.budgetpro.domain.proyecto.model.EstadoProyecto.BORRADOR,
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

        PartidaEntity partida = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "01",
            "Muro",
            "m2",
            new BigDecimal("100.00"), // 100 m2 total
            1,
            null
        );
        partida = partidaJpaRepository.save(partida);
        partidaId = partida.getId();
    }

    @Test
    void testRegistrarAvance_VerificarPorcentajeAvance() throws Exception {
        // 1. Registrar Avance de 20 m2
        String avanceJson = String.format("""
                {
                    "fecha": "2026-01-12",
                    "metradoEjecutado": 20.00,
                    "observacion": "Avance de muro en zona A"
                }
                """);

        String responseJson = mockMvc.perform(post("/api/v1/partidas/{partidaId}/avances", partidaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(avanceJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.partidaId").value(partidaId.toString()))
                .andExpect(jsonPath("$.fecha").value("2026-01-12"))
                .andExpect(jsonPath("$.metradoEjecutado").value(20.00))
                .andExpect(jsonPath("$.observacion").value("Avance de muro en zona A"))
                .andExpect(jsonPath("$.porcentajeAvance").exists())
                .andExpect(jsonPath("$.version").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 2. Verificar que el % de Avance de la partida ahora es 20%
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseJson);
        BigDecimal porcentajeAvance = new BigDecimal(jsonNode.get("porcentajeAvance").asText());
        
        assertThat(porcentajeAvance).isEqualByComparingTo(new BigDecimal("20.00"));
        
        // 3. Verificar que el avance se guardó correctamente
        assertThat(jsonNode.get("id").asText()).isNotNull();
        assertThat(jsonNode.get("metradoEjecutado").asDouble()).isEqualTo(20.00);
    }
}
