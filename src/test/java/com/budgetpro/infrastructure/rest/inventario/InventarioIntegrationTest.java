package com.budgetpro.infrastructure.rest.inventario;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.inventario.InventarioItemEntity;
import com.budgetpro.infrastructure.persistence.entity.inventario.MovimientoInventarioEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.compra.CompraJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.inventario.InventarioItemJpaRepository;
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
 * Test de integración para verificar el flujo completo de inventario:
 * Registro de compra, generación automática de entrada en inventario y Kardex.
 */
class InventarioIntegrationTest extends AbstractIntegrationTest {

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
    private InventarioItemJpaRepository inventarioItemJpaRepository;

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
        inventarioItemJpaRepository.deleteAll();
        compraJpaRepository.deleteAll();
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        proyectoJpaRepository.deleteAll();
        recursoJpaRepository.deleteAll();

        // Setup: Crear Proyecto, Presupuesto, Partida, Recurso y Billetera
        ProyectoEntity proyecto = new ProyectoEntity(
            UUID.randomUUID(),
            "Proyecto Test Inventario",
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
    void testRegistrarCompraYVerificarInventario_FlujoCompleto() throws Exception {
        // 1. Verificar que inicialmente NO existe InventarioItem
        assertThat(inventarioItemJpaRepository.findByProyectoIdAndRecursoId(proyectoId, recursoId))
                .isEmpty();

        // 2. Registrar Compra de 10 unidades
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
                .andExpect(jsonPath("$.total").value(200.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer el ID de la compra
        UUID compraId = extractIdFromJson(compraResponse);

        // 3. Verificar que se creó el InventarioItem automáticamente
        InventarioItemEntity inventarioItem = inventarioItemJpaRepository
                .findByProyectoIdAndRecursoId(proyectoId, recursoId)
                .orElseThrow(() -> new AssertionError("El InventarioItem debería existir en BD"));

        assertThat(inventarioItem.getProyectoId()).isEqualTo(proyectoId);
        assertThat(inventarioItem.getRecurso().getId()).isEqualTo(recursoId);
        assertThat(inventarioItem.getCantidadFisica())
                .isEqualByComparingTo(new BigDecimal("10.00")); // Stock actualizado
        assertThat(inventarioItem.getCostoPromedio())
                .isEqualByComparingTo(new BigDecimal("20.00")); // Costo promedio = precio unitario
        assertThat(inventarioItem.getVersion()).isNotNull();
        assertThat(inventarioItem.getUltimaActualizacion()).isNotNull();

        // 4. Verificar que existe un MovimientoInventario tipo ENTRADA_COMPRA
        assertThat(inventarioItem.getMovimientos()).hasSize(1);
        
        MovimientoInventarioEntity movimiento = inventarioItem.getMovimientos().get(0);
        assertThat(movimiento.getInventarioItem().getId()).isEqualTo(inventarioItem.getId());
        assertThat(movimiento.getTipo())
                .isEqualTo(com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario.ENTRADA_COMPRA);
        assertThat(movimiento.getCantidad())
                .isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(movimiento.getCostoUnitario())
                .isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(movimiento.getCostoTotal())
                .isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(movimiento.getCompraDetalleId()).isNotNull(); // Trazabilidad
        assertThat(movimiento.getReferencia()).contains("Compra #");
        assertThat(movimiento.getFechaHora()).isNotNull();
        assertThat(movimiento.getVersion()).isNotNull();

        // 5. Consultar inventario por REST API
        mockMvc.perform(get("/api/v1/proyectos/{proyectoId}/inventario", proyectoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(inventarioItem.getId().toString()))
                .andExpect(jsonPath("$[0].proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$[0].recursoId").value(recursoId.toString()))
                .andExpect(jsonPath("$[0].cantidadFisica").value(10.00))
                .andExpect(jsonPath("$[0].costoPromedio").value(20.00));
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
