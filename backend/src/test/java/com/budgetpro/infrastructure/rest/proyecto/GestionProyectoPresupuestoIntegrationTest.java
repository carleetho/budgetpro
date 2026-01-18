package com.budgetpro.infrastructure.rest.proyecto;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar la creación de Proyecto y Presupuesto.
 */
class GestionProyectoPresupuestoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
    }

    @Test
    void testCrearProyectoYPresupuesto_FlujoCompleto() throws Exception {
        // 1. Crear un Proyecto
        String proyectoJson = """
                {
                    "nombre": "Proyecto Test",
                    "ubicacion": "Lima, Perú"
                }
                """;

        String proyectoResponse = mockMvc.perform(post("/api/v1/proyectos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(proyectoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Proyecto Test"))
                .andExpect(jsonPath("$.ubicacion").value("Lima, Perú"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID del proyecto de la respuesta
        UUID proyectoId = extractIdFromJson(proyectoResponse);

        // 2. Verificar que el proyecto se persistió en BD
        ProyectoEntity proyectoEntity = proyectoJpaRepository.findById(proyectoId)
                .orElseThrow(() -> new AssertionError("El proyecto debería existir en BD"));

        assertThat(proyectoEntity.getNombre()).isEqualTo("Proyecto Test");
        assertThat(proyectoEntity.getUbicacion()).isEqualTo("Lima, Perú");
        assertThat(proyectoEntity.getEstado()).isEqualTo(com.budgetpro.domain.proyecto.model.EstadoProyecto.BORRADOR);
        assertThat(proyectoEntity.getVersion()).isNotNull();
        assertThat(proyectoEntity.getCreatedAt()).isNotNull();
        assertThat(proyectoEntity.getUpdatedAt()).isNotNull();

        // 3. Crear un Presupuesto para ese proyecto
        String presupuestoJson = String.format("""
                {
                    "proyectoId": "%s",
                    "nombre": "Presupuesto Base"
                }
                """, proyectoId);

        String presupuestoResponse = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presupuestoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$.nombre").value("Presupuesto Base"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.esContractual").value(false))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID del presupuesto de la respuesta
        UUID presupuestoId = extractIdFromJson(presupuestoResponse);

        // 4. Verificar que el presupuesto se persistió en BD
        PresupuestoEntity presupuestoEntity = presupuestoJpaRepository.findById(presupuestoId)
                .orElseThrow(() -> new AssertionError("El presupuesto debería existir en BD"));

        assertThat(presupuestoEntity.getProyectoId()).isEqualTo(proyectoId);
        assertThat(presupuestoEntity.getNombre()).isEqualTo("Presupuesto Base");
        assertThat(presupuestoEntity.getEstado()).isEqualTo(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.BORRADOR);
        assertThat(presupuestoEntity.getEsContractual()).isFalse();
        assertThat(presupuestoEntity.getVersion()).isNotNull();
        assertThat(presupuestoEntity.getCreatedAt()).isNotNull();
        assertThat(presupuestoEntity.getUpdatedAt()).isNotNull();
    }

    /**
     * Extrae el ID de un JSON de respuesta.
     * Método auxiliar simple para tests.
     */
    private UUID extractIdFromJson(String json) throws Exception {
        // Usar ObjectMapper para parsear el JSON
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(json);
        String idString = jsonNode.get("id").asText();
        return UUID.fromString(idString);
    }
}
