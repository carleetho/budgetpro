package com.budgetpro.infrastructure.rest.apu;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository;
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
 * Test de integración para verificar la creación de APU con insumos.
 */
class GestionApuIntegrationTest extends AbstractIntegrationTest {

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
    private RecursoJpaRepository recursoJpaRepository;

    @Autowired
    private ApuJpaRepository apuJpaRepository;

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;
    private UUID recursoId;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        apuJpaRepository.deleteAll();
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto, Partida y Recurso
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test APU",
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
            com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.BORRADOR,
            false,
            null
        );
        presupuesto = presupuestoJpaRepository.save(presupuesto);
        presupuestoId = presupuesto.getId();

        // Crear PartidaEntity usando el constructor público
        PartidaEntity partida = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "01",
            "Concreto f'c=210 kg/cm2",
            "m3",
            new BigDecimal("100.00"),
            1,
            null // version = null para nueva entidad
        );
        partida = partidaJpaRepository.save(partida);
        partidaId = partida.getId();

        RecursoEntity recurso = new RecursoEntity(
            UUID.randomUUID(),
            "CEMENTO PORTLAND",
            "CEMENTO PORTLAND",
            com.budgetpro.domain.shared.model.TipoRecurso.MATERIAL,
            "BOLSA",
            null,
            com.budgetpro.domain.finanzas.recurso.model.EstadoRecurso.ACTIVO,
            UUID.randomUUID() // createdBy
        );
        recurso = recursoJpaRepository.save(recurso);
        recursoId = recurso.getId();
    }

    @Test
    void testCrearApuConInsumo_FlujoCompleto() throws Exception {
        // 1. Crear APU para la Partida agregando el Recurso
        String apuJson = String.format("""
                {
                    "rendimiento": 10.5,
                    "unidad": "m3",
                    "insumos": [
                        {
                            "recursoId": "%s",
                            "cantidad": 7.5,
                            "precioUnitario": 25.50
                        }
                    ]
                }
                """, recursoId);

        String apuResponse = mockMvc.perform(post("/api/v1/partidas/{partidaId}/apu", partidaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apuJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.partidaId").value(partidaId.toString()))
                .andExpect(jsonPath("$.rendimiento").value(10.5))
                .andExpect(jsonPath("$.unidad").value("m3"))
                .andExpect(jsonPath("$.costoTotal").value(191.25)) // 7.5 * 25.50
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.insumos").isArray())
                .andExpect(jsonPath("$.insumos[0].recursoId").value(recursoId.toString()))
                .andExpect(jsonPath("$.insumos[0].cantidad").value(7.5))
                .andExpect(jsonPath("$.insumos[0].precioUnitario").value(25.50))
                .andExpect(jsonPath("$.insumos[0].subtotal").value(191.25))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID del APU
        UUID apuId = extractIdFromJson(apuResponse);

        // 2. Verificar que el APU se guardó en BD
        ApuEntity apuEntity = apuJpaRepository.findById(apuId)
                .orElseThrow(() -> new AssertionError("El APU debería existir en BD"));

        assertThat(apuEntity.getPartida().getId()).isEqualTo(partidaId);
        assertThat(apuEntity.getRendimiento()).isEqualByComparingTo(new BigDecimal("10.5"));
        assertThat(apuEntity.getUnidad()).isEqualTo("m3");
        assertThat(apuEntity.getVersion()).isNotNull();
        assertThat(apuEntity.getCreatedAt()).isNotNull();
        assertThat(apuEntity.getUpdatedAt()).isNotNull();

        // 3. Verificar que el ApuInsumo se guardó correctamente
        assertThat(apuEntity.getInsumos()).hasSize(1);
        
        ApuInsumoEntity insumoEntity = apuEntity.getInsumos().get(0);
        assertThat(insumoEntity.getRecurso().getId()).isEqualTo(recursoId);
        assertThat(insumoEntity.getCantidad()).isEqualByComparingTo(new BigDecimal("7.5"));
        assertThat(insumoEntity.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(insumoEntity.getSubtotal()).isEqualByComparingTo(new BigDecimal("191.25"));
        assertThat(insumoEntity.getVersion()).isNotNull();
        assertThat(insumoEntity.getCreatedAt()).isNotNull();
        assertThat(insumoEntity.getUpdatedAt()).isNotNull();
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
