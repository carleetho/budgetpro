package com.budgetpro.infrastructure.rest.presupuesto;

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
import com.budgetpro.infrastructure.persistence.repository.apu.ApuInsumoJpaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el cálculo de costos y aprobación de presupuestos.
 */
class CalculoPresupuestoIntegrationTest extends AbstractIntegrationTest {

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

    @Autowired
    private ApuInsumoJpaRepository apuInsumoJpaRepository;

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;
    private UUID recursoId;
    private UUID apuId;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        apuInsumoJpaRepository.deleteAll();
        apuJpaRepository.deleteAll();
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto, Partida, Recurso y APU
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Cálculo",
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

        PartidaEntity partida = new PartidaEntity(
            UUID.randomUUID(),
            presupuesto,
            null, // Sin padre (raíz)
            "01",
            "Concreto f'c=210 kg/cm2",
            "m3",
            new BigDecimal("100.00"), // 100 m3
            1,
            null
        );
        partida = partidaJpaRepository.save(partida);
        partidaId = partida.getId();

        UUID recursoIdValue = UUID.randomUUID();
        UUID createdByValue = UUID.randomUUID();
        RecursoEntity recurso = new RecursoEntity(
            recursoIdValue,
            "CEMENTO PORTLAND",
            "CEMENTO PORTLAND",
            com.budgetpro.domain.recurso.model.TipoRecurso.MATERIAL,
            "BOLSA",
            null,
            com.budgetpro.domain.recurso.model.EstadoRecurso.ACTIVO,
            createdByValue
        );
        recurso = recursoJpaRepository.save(recurso);
        recursoId = recurso.getId();

        // Crear APU para la partida con costo unitario de $10/m3
        ApuEntity apu = new ApuEntity(
            UUID.randomUUID(),
            partida,
            null, // Sin rendimiento
            "m3",
            null // version = null para nueva entidad
        );
        apu = apuJpaRepository.save(apu);
        apuId = apu.getId();

        // Agregar insumo al APU: 7.5 bolsas a $0.80/bolsa = $6.00, pero ajustamos para que el costo total sea $10/m3
        // Si queremos $10/m3, necesitamos: cantidad * precioUnitario = 10
        // Por ejemplo: 12.5 bolsas * $0.80 = $10
        ApuInsumoEntity insumo = new ApuInsumoEntity(
            UUID.randomUUID(),
            apu,
            recurso,
            new BigDecimal("12.5"), // Cantidad
            new BigDecimal("0.80"), // Precio unitario
            new BigDecimal("10.00"), // Subtotal: 12.5 * 0.80 = 10.00
            null // version = null para nueva entidad
        );
        apuInsumoJpaRepository.save(insumo);
    }

    @Test
    void testAprobarPresupuestoYVerificarCostoTotal_FlujoCompleto() throws Exception {
        // 1. Aprobar el presupuesto
        mockMvc.perform(post("/api/v1/presupuestos/{presupuestoId}/aprobar", presupuestoId))
                .andExpect(status().isNoContent());

        // 2. Verificar que el presupuesto pasó a APROBADO en BD
        PresupuestoEntity presupuestoEntity = presupuestoJpaRepository.findById(presupuestoId)
                .orElseThrow(() -> new AssertionError("El presupuesto debería existir en BD"));

        assertThat(presupuestoEntity.getEstado())
                .isEqualTo(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.APROBADO);
        assertThat(presupuestoEntity.getEsContractual())
                .isTrue(); // Debe estar marcado como contractual

        // 3. Consultar el presupuesto y verificar que el costo total sea $1000 (100 m3 * $10/m3)
        String responseJson = mockMvc.perform(get("/api/v1/presupuestos/{presupuestoId}", presupuestoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(presupuestoId.toString()))
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.esContractual").value(true))
                .andExpect(jsonPath("$.costoTotal").value(1000.00)) // 100 m3 * $10/m3 = $1000
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Validar el cálculo manualmente
        assertThat(presupuestoEntity.getEstado())
                .isEqualTo(com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto.APROBADO);
    }
}
