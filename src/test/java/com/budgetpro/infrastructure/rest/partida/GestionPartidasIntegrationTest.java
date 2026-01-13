package com.budgetpro.infrastructure.rest.partida;

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
 * Test de integración para verificar la creación de Partidas con jerarquía WBS.
 */
class GestionPartidasIntegrationTest extends AbstractIntegrationTest {

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

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();

        // Setup: Crear Proyecto y Presupuesto para los tests
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Partidas",
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
    }

    @Test
    void testCrearPartidaRaizYHija_FlujoCompleto() throws Exception {
        // 1. Crear Partida Raíz (Título)
        String partidaRaizJson = String.format("""
                {
                    "presupuestoId": "%s",
                    "item": "01",
                    "descripcion": "OBRAS PRELIMINARES",
                    "unidad": null,
                    "metrado": 0,
                    "nivel": 1
                }
                """, presupuestoId);

        String partidaRaizResponse = mockMvc.perform(post("/api/v1/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partidaRaizJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.presupuestoId").value(presupuestoId.toString()))
                .andExpect(jsonPath("$.padreId").isEmpty())
                .andExpect(jsonPath("$.item").value("01"))
                .andExpect(jsonPath("$.descripcion").value("OBRAS PRELIMINARES"))
                .andExpect(jsonPath("$.metrado").value(0))
                .andExpect(jsonPath("$.nivel").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID de la partida raíz
        UUID partidaRaizId = extractIdFromJson(partidaRaizResponse);

        // 2. Verificar que la partida raíz se persistió en BD
        PartidaEntity partidaRaizEntity = partidaJpaRepository.findById(partidaRaizId)
                .orElseThrow(() -> new AssertionError("La partida raíz debería existir en BD"));

        assertThat(partidaRaizEntity.getPresupuesto().getId()).isEqualTo(presupuestoId);
        assertThat(partidaRaizEntity.getPadre()).isNull();
        assertThat(partidaRaizEntity.getItem()).isEqualTo("01");
        assertThat(partidaRaizEntity.getDescripcion()).isEqualTo("OBRAS PRELIMINARES");
        assertThat(partidaRaizEntity.getMetrado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(partidaRaizEntity.getNivel()).isEqualTo(1);
        assertThat(partidaRaizEntity.getVersion()).isNotNull();
        assertThat(partidaRaizEntity.getCreatedAt()).isNotNull();
        assertThat(partidaRaizEntity.getUpdatedAt()).isNotNull();

        // 3. Crear Partida Hija (Subtítulo) apuntando a la Raíz
        String partidaHijaJson = String.format("""
                {
                    "presupuestoId": "%s",
                    "padreId": "%s",
                    "item": "01.01",
                    "descripcion": "Limpieza y desbroce",
                    "unidad": "m2",
                    "metrado": 100.50,
                    "nivel": 2
                }
                """, presupuestoId, partidaRaizId);

        String partidaHijaResponse = mockMvc.perform(post("/api/v1/partidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(partidaHijaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.presupuestoId").value(presupuestoId.toString()))
                .andExpect(jsonPath("$.padreId").value(partidaRaizId.toString()))
                .andExpect(jsonPath("$.item").value("01.01"))
                .andExpect(jsonPath("$.descripcion").value("Limpieza y desbroce"))
                .andExpect(jsonPath("$.unidad").value("m2"))
                .andExpect(jsonPath("$.metrado").value(100.50))
                .andExpect(jsonPath("$.nivel").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID de la partida hija
        UUID partidaHijaId = extractIdFromJson(partidaHijaResponse);

        // 4. Verificar que la partida hija se persistió en BD
        PartidaEntity partidaHijaEntity = partidaJpaRepository.findById(partidaHijaId)
                .orElseThrow(() -> new AssertionError("La partida hija debería existir en BD"));

        assertThat(partidaHijaEntity.getPresupuesto().getId()).isEqualTo(presupuestoId);
        assertThat(partidaHijaEntity.getPadre()).isNotNull();
        assertThat(partidaHijaEntity.getPadre().getId()).isEqualTo(partidaRaizId);
        assertThat(partidaHijaEntity.getItem()).isEqualTo("01.01");
        assertThat(partidaHijaEntity.getDescripcion()).isEqualTo("Limpieza y desbroce");
        assertThat(partidaHijaEntity.getUnidad()).isEqualTo("m2");
        assertThat(partidaHijaEntity.getMetrado()).isEqualByComparingTo(new BigDecimal("100.50"));
        assertThat(partidaHijaEntity.getNivel()).isEqualTo(2);
        assertThat(partidaHijaEntity.getVersion()).isNotNull();
        assertThat(partidaHijaEntity.getCreatedAt()).isNotNull();
        assertThat(partidaHijaEntity.getUpdatedAt()).isNotNull();

        // 5. Verificar jerarquía: La partida raíz debe tener la hija como hijo
        // (Esto se verifica indirectamente al cargar la relación padre-hijo)
        assertThat(partidaHijaEntity.getPadre().getId()).isEqualTo(partidaRaizEntity.getId());
    }

    /**
     * Extrae el ID de un JSON de respuesta.
     */
    private UUID extractIdFromJson(String json) throws Exception {
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(json);
        String idString = jsonNode.get("id").asText();
        return UUID.fromString(idString);
    }
}
