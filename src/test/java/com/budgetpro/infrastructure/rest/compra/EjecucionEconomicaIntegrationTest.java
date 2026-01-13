package com.budgetpro.infrastructure.rest.compra;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity;
import com.budgetpro.infrastructure.persistence.entity.consumo.ConsumoPartidaEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.compra.CompraJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.consumo.ConsumoPartidaJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para verificar el flujo completo de ejecución económica:
 * Registro de compra, generación de consumo presupuestal y descuento de billetera.
 */
class EjecucionEconomicaIntegrationTest extends AbstractIntegrationTest {

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
    private BilleteraRepository billeteraRepository;

    @Autowired
    private CompraJpaRepository compraJpaRepository;

    @Autowired
    private ConsumoPartidaJpaRepository consumoPartidaJpaRepository;

    private MockMvc mockMvc;
    private UUID proyectoId;
    private UUID presupuestoId;
    private UUID partidaId;
    private UUID recursoId;
    private UUID billeteraId;

    @BeforeEach
    void setUp() {
        // Configurar MockMvc
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Limpiar datos de prueba
        consumoPartidaJpaRepository.deleteAll();
        compraJpaRepository.deleteAll();
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto, Partida, Recurso y Billetera
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Ejecución",
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

        // Crear Billetera con saldo inicial de $2000 usando el dominio
        BilleteraId billeteraIdValue = BilleteraId.generate();
        Billetera billetera = Billetera.crear(billeteraIdValue, proyectoId);
        // Ingresar saldo inicial de $2000
        billetera.ingresar(new BigDecimal("2000.00"), "Saldo inicial de prueba", null);
        billeteraRepository.save(billetera);
        billeteraId = billetera.getId().getValue();
    }

    @Test
    void testRegistrarCompraYVerificarConsumoYBilletera_FlujoCompleto() throws Exception {
        // 1. Registrar Compra de $200 imputada a la Partida
        String compraJson = String.format("""
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

        String compraResponse = mockMvc.perform(post("/api/v1/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compraJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$.fecha").value("2026-01-12"))
                .andExpect(jsonPath("$.proveedor").value("PROVEEDOR ABC S.A.C."))
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.total").value(200.00))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles[0].recursoId").value(recursoId.toString()))
                .andExpect(jsonPath("$.detalles[0].partidaId").value(partidaId.toString()))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(10))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(20.00))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(200.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID de la compra
        UUID compraId = extractIdFromJson(compraResponse);

        // 2. Verificar que la Compra se guardó en BD
        CompraEntity compraEntity = compraJpaRepository.findById(compraId)
                .orElseThrow(() -> new AssertionError("La compra debería existir en BD"));

        assertThat(compraEntity.getProyectoId()).isEqualTo(proyectoId);
        assertThat(compraEntity.getFecha()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(compraEntity.getProveedor()).isEqualTo("PROVEEDOR ABC S.A.C.");
        assertThat(compraEntity.getEstado()).isEqualTo(com.budgetpro.domain.logistica.compra.model.EstadoCompra.APROBADA);
        assertThat(compraEntity.getTotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(compraEntity.getVersion()).isNotNull();
        assertThat(compraEntity.getDetalles()).hasSize(1);

        // 3. Verificar que se creó el ConsumoPartida
        List<ConsumoPartidaEntity> consumos = consumoPartidaJpaRepository.findByPartidaId(partidaId);
        assertThat(consumos).hasSize(1);
        
        ConsumoPartidaEntity consumo = consumos.get(0);
        assertThat(consumo.getPartida().getId()).isEqualTo(partidaId);
        assertThat(consumo.getCompraDetalleId()).isEqualTo(compraEntity.getDetalles().get(0).getId());
        assertThat(consumo.getMonto()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(consumo.getFecha()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(consumo.getTipo()).isEqualTo(com.budgetpro.domain.finanzas.consumo.model.TipoConsumo.COMPRA);
        assertThat(consumo.getVersion()).isNotNull();

        // 4. Verificar que la Billetera fue descontada
        Billetera billeteraActualizada = billeteraRepository.findById(BilleteraId.of(billeteraId))
                .orElseThrow(() -> new AssertionError("La billetera debería existir en BD"));

        assertThat(billeteraActualizada.getSaldoActual())
                .isEqualByComparingTo(new BigDecimal("1800.00")); // 2000 - 200 = 1800
        assertThat(billeteraActualizada.getVersion()).isNotNull();
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
