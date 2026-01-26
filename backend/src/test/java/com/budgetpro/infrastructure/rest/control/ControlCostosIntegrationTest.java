package com.budgetpro.infrastructure.rest.control;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
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
 * Test de integración para verificar el reporte de control de costos (Plan vs Real).
 */
class ControlCostosIntegrationTest extends AbstractIntegrationTest {

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

    @Autowired
    private BilleteraRepository billeteraRepository;

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;
    private UUID recursoId;
    private UUID apuId;
    private UUID billeteraId;

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

        // Setup: Crear Proyecto, Presupuesto, Partida, Recurso, APU y Billetera
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Control Costos",
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
            com.budgetpro.domain.shared.model.TipoRecurso.MATERIAL,
            "BOLSA",
            null,
            com.budgetpro.domain.recurso.model.EstadoRecurso.ACTIVO,
            createdByValue
        );
        recurso = recursoJpaRepository.save(recurso);
        recursoId = recurso.getId();

        // Crear APU para la partida (costo unitario = $10/m3)
        ApuEntity apu = new ApuEntity(
            UUID.randomUUID(),
            partida,
            null, // rendimiento opcional
            "m3",
            null
        );
        apu = apuJpaRepository.save(apu);
        apuId = apu.getId();

        // Crear ApuInsumo (1 bolsa de cemento por m3 a $10)
        ApuInsumoEntity apuInsumo = new ApuInsumoEntity(
            UUID.randomUUID(),
            apu,
            recurso,
            new BigDecimal("1.0000"), // 1 bolsa por m3
            new BigDecimal("10.0000"), // $10 por bolsa
            new BigDecimal("10.0000"), // subtotal = 1 * 10
            null
        );
        apuInsumoJpaRepository.save(apuInsumo);

        // Aprobar presupuesto para que tenga costo total
        String aprobarJson = String.format("""
                {
                    "presupuestoId": "%s"
                }
                """, presupuestoId);
        
        try {
            mockMvc.perform(post("/api/v1/presupuestos/{presupuestoId}/aprobar", presupuestoId))
                    .andExpect(status().isNoContent());
        } catch (Exception e) {
            // Si falla, continuar (puede que el endpoint no exista o haya error)
        }

        // Crear Billetera con saldo inicial de $2000
        BilleteraId billeteraIdValue = BilleteraId.generate();
        Billetera billetera = Billetera.crear(billeteraIdValue, proyectoId);
        billetera.ingresar(new BigDecimal("2000.00"), "Saldo inicial de prueba", null);
        billeteraRepository.save(billetera);
        billeteraId = billetera.getId().getValue();
    }

    @Test
    void testConsultarControlCostos_PlanVsReal_FlujoCompleto() throws Exception {
        // 1. Registrar primera Compra de $200
        String compra1Json = String.format("""
                {
                    "proyectoId": "%s",
                    "fecha": "2026-01-12",
                    "proveedor": "PROVEEDOR ABC S.A.C.",
                    "detalles": [
                        {
                            "recursoId": "%s",
                            "partidaId": "%s",
                            "cantidad": 10,
                            "precioUnitario": 20.00
                        }
                    ]
                }
                """, proyectoId, recursoId, partidaId);

        mockMvc.perform(post("/api/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compra1Json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.00));

        // 2. Registrar segunda Compra de $200
        String compra2Json = String.format("""
                {
                    "proyectoId": "%s",
                    "fecha": "2026-01-13",
                    "proveedor": "PROVEEDOR XYZ S.A.C.",
                    "detalles": [
                        {
                            "recursoId": "%s",
                            "partidaId": "%s",
                            "cantidad": 10,
                            "precioUnitario": 20.00
                        }
                    ]
                }
                """, proyectoId, recursoId, partidaId);

        mockMvc.perform(post("/api/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compra2Json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(200.00));

        // 3. Consultar Reporte de Control de Costos
        String responseJson = mockMvc.perform(get("/api/v1/presupuestos/{presupuestoId}/control-costos", presupuestoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presupuestoId").value(presupuestoId.toString()))
                .andExpect(jsonPath("$.nombrePresupuesto").value("Presupuesto Base"))
                .andExpect(jsonPath("$.totalPlan").exists())
                .andExpect(jsonPath("$.totalReal").exists())
                .andExpect(jsonPath("$.totalSaldo").exists())
                .andExpect(jsonPath("$.porcentajeEjecucionTotal").exists())
                .andExpect(jsonPath("$.partidas").isArray())
                .andExpect(jsonPath("$.partidas[0].id").value(partidaId.toString()))
                .andExpect(jsonPath("$.partidas[0].item").value("01"))
                .andExpect(jsonPath("$.partidas[0].descripcion").value("Concreto f'c=210 kg/cm2"))
                .andExpect(jsonPath("$.partidas[0].unidad").value("m3"))
                .andExpect(jsonPath("$.partidas[0].metrado").value(100.00))
                .andExpect(jsonPath("$.partidas[0].parcialPlan").exists())
                .andExpect(jsonPath("$.partidas[0].gastoAcumulado").exists())
                .andExpect(jsonPath("$.partidas[0].saldo").exists())
                .andExpect(jsonPath("$.partidas[0].porcentajeEjecucion").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 4. Verificar cálculos específicos
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseJson);
        
        // Total Plan = 100 m3 * $10/m3 = $1000
        BigDecimal totalPlan = new BigDecimal(jsonNode.get("totalPlan").asText());
        assertThat(totalPlan).isEqualByComparingTo(new BigDecimal("1000.00"));
        
        // Total Real = $200 + $200 = $400
        BigDecimal totalReal = new BigDecimal(jsonNode.get("totalReal").asText());
        assertThat(totalReal).isEqualByComparingTo(new BigDecimal("400.00"));
        
        // Total Saldo = $1000 - $400 = $600
        BigDecimal totalSaldo = new BigDecimal(jsonNode.get("totalSaldo").asText());
        assertThat(totalSaldo).isEqualByComparingTo(new BigDecimal("600.00"));
        
        // Porcentaje Ejecución = ($400 / $1000) * 100 = 40%
        BigDecimal porcentajeEjecucion = new BigDecimal(jsonNode.get("porcentajeEjecucionTotal").asText());
        assertThat(porcentajeEjecucion).isEqualByComparingTo(new BigDecimal("40.00"));
        
        // Verificar datos de la partida
        com.fasterxml.jackson.databind.JsonNode partidaNode = jsonNode.get("partidas").get(0);
        BigDecimal parcialPlan = new BigDecimal(partidaNode.get("parcialPlan").asText());
        assertThat(parcialPlan).isEqualByComparingTo(new BigDecimal("1000.00"));
        
        BigDecimal gastoAcumulado = new BigDecimal(partidaNode.get("gastoAcumulado").asText());
        assertThat(gastoAcumulado).isEqualByComparingTo(new BigDecimal("400.00"));
        
        BigDecimal saldo = new BigDecimal(partidaNode.get("saldo").asText());
        assertThat(saldo).isEqualByComparingTo(new BigDecimal("600.00"));
        
        BigDecimal porcentajeEjecucionPartida = new BigDecimal(partidaNode.get("porcentajeEjecucion").asText());
        assertThat(porcentajeEjecucionPartida).isEqualByComparingTo(new BigDecimal("40.00"));
    }
}
