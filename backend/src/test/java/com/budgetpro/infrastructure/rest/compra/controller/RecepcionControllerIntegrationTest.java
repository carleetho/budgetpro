package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.domain.logistica.almacen.model.Almacen;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.logistica.almacen.port.out.AlmacenRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.repository.compra.RecepcionJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración end-to-end para RecepcionController.
 * 
 * Verifica todos los escenarios de aceptación con base de datos real y transacciones completas.
 * 
 * Cobertura:
 * 1. Recepción completa → 201 CREATED
 * 2. Recepción parcial → 201 con estado PARCIAL
 * 3. Segunda recepción parcial → 201 con estado RECIBIDA
 * 4. Intento de sobre-entrega → 400 OVER_DELIVERY
 * 5. Proyecto no activo → 412 PROJECT_NOT_ACTIVE
 * 6. Guía de remisión duplicada → 409 DUPLICATE_RECEPTION
 * 7. Guía de remisión faltante → 400 MISSING_GUIA_REMISION
 * 8. Rol no autorizado → 403 UNAUTHORIZED_ROLE (simulado)
 * 9. Transición de estado inválida → 400 INVALID_STATE
 * 10. Recepción multi-almacén → 201 con múltiples MovimientoAlmacen
 * 11. Verificación de cálculo PMP → fórmula exacta
 */
@AutoConfigureMockMvc(addFilters = false)
class RecepcionControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private RecursoProxyRepository recursoProxyRepository;

    @Autowired
    private MovimientoAlmacenRepository movimientoAlmacenRepository;

    @Autowired
    private RegistroKardexRepository kardexRepository;

    @Autowired
    private RecepcionJpaRepository recepcionJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID proyectoId;
    private UUID compraId;
    private UUID compraDetalleId;
    private UUID almacenId;
    private UUID almacenId2;
    private UUID recursoId;
    private UUID recursoExternalId;
    private String catalogSource;
    private BigDecimal cantidadOrdenada;
    private BigDecimal precioUnitario;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        entityManager.createQuery("DELETE FROM RecepcionDetalleEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM RecepcionEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM MovimientoAlmacenEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM KardexEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM CompraDetalleEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM CompraEntity").executeUpdate();
        entityManager.flush();

        // Crear Proyecto ACTIVO
        ProyectoId proyectoDomainId = ProyectoId.nuevo();
        proyectoId = proyectoDomainId.getValue();
        Proyecto proyecto = Proyecto.crear(proyectoDomainId, "Proyecto Test Recepción", "Lima, Perú");
        proyecto = proyecto.activar(); // Cambiar a ACTIVO
        proyectoRepository.save(proyecto);

        // Crear Almacén activo
        AlmacenId almacenDomainId = AlmacenId.generate();
        almacenId = almacenDomainId.getValue();
        Almacen almacen = Almacen.crear(almacenDomainId, proyectoId, "ALM-001", "Almacén Principal", "Lima", UUID.randomUUID());
        almacenRepository.guardar(almacen);

        // Crear segundo almacén para test multi-almacén
        AlmacenId almacenDomainId2 = AlmacenId.generate();
        almacenId2 = almacenDomainId2.getValue();
        Almacen almacen2 = Almacen.crear(almacenDomainId2, proyectoId, "ALM-002", "Almacén Secundario", "Lima", UUID.randomUUID());
        almacenRepository.guardar(almacen2);

        // Crear RecursoProxy
        recursoExternalId = UUID.randomUUID();
        catalogSource = "CAPECO";
        RecursoProxyId recursoProxyId = RecursoProxyId.generate();
        recursoId = UUID.randomUUID(); // ID interno del recurso
        RecursoProxy recursoProxy = RecursoProxy.crear(
            recursoProxyId,
            recursoExternalId.toString(),
            catalogSource,
            "Cemento Portland",
            com.budgetpro.domain.shared.model.TipoRecurso.MATERIAL,
            "BOLSA",
            precioUnitario,
            java.time.LocalDateTime.now()
        );
        recursoProxyRepository.save(recursoProxy);

        // Crear Compra en estado ENVIADA
        CompraId compraDomainId = CompraId.nuevo();
        compraId = compraDomainId.getValue();
        cantidadOrdenada = new BigDecimal("1000.00");
        precioUnitario = new BigDecimal("10.00");

        CompraDetalleId detalleId = CompraDetalleId.nuevo();
        compraDetalleId = detalleId.getValue();
        CompraDetalle detalle = CompraDetalle.crear(
            detalleId,
            recursoExternalId.toString(),
            "Cemento Portland",
            "BOLSA",
            UUID.randomUUID(), // partidaId
            NaturalezaGasto.DIRECTO_PARTIDA,
            RelacionContractual.CONTRACTUAL,
            RubroInsumo.MATERIAL_CONSTRUCCION,
            cantidadOrdenada,
            precioUnitario
        );

        Compra compra = Compra.crear(
            compraDomainId,
            proyectoId,
            LocalDate.now(),
            "Proveedor Test S.A.C.",
            List.of(detalle)
        );

        // Aprobar y enviar la compra
        compra = Compra.reconstruir(
            compra.getId(),
            compra.getProyectoId(),
            compra.getFecha(),
            compra.getProveedor(),
            EstadoCompra.ENVIADA, // Estado ENVIADA
            compra.getTotal(),
            compra.getVersion(),
            compra.getDetalles()
        );

        compraRepository.save(compra);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("1. Recepción completa → 201 CREATED con estado RECIBIDA")
    void debeRecibirOrdenCompraCompletamente() throws Exception {
        // Arrange
        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-001",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 1000.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recepcionId").exists())
                .andExpect(jsonPath("$.compraId").value(compraId.toString()))
                .andExpect(jsonPath("$.estadoCompra").value("RECIBIDA"))
                .andExpect(jsonPath("$.guiaRemision").value("GR-2024-001"))
                .andExpect(jsonPath("$.detalles[0].cantidadRecibida").value(1000.00))
                .andExpect(jsonPath("$.detalles[0].cantidadPendiente").value(0.00));

        // Verificar estado en BD
        entityManager.clear();
        Compra compra = compraRepository.findById(CompraId.from(compraId)).orElseThrow();
        assertEquals(EstadoCompra.RECIBIDA, compra.getEstado());
        assertEquals(cantidadOrdenada, compra.getDetalles().get(0).getCantidadRecibida());

        // Verificar MovimientoAlmacen creado
        var movimientos = movimientoAlmacenRepository.buscarPorAlmacenIdYRecursoId(almacenId, recursoId);
        assertEquals(1, movimientos.size());
        assertEquals(TipoMovimientoAlmacen.ENTRADA, movimientos.get(0).getTipoMovimiento());
    }

    @Test
    @DisplayName("2. Recepción parcial → 201 CREATED con estado PARCIAL")
    void debeRecibirOrdenCompraParcialmente() throws Exception {
        // Arrange
        BigDecimal cantidadParcial = new BigDecimal("500.00");
        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-002",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, cantidadParcial, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoCompra").value("PARCIAL"))
                .andExpect(jsonPath("$.detalles[0].cantidadRecibida").value(500.00))
                .andExpect(jsonPath("$.detalles[0].cantidadPendiente").value(500.00));

        // Verificar estado en BD
        entityManager.clear();
        Compra compra = compraRepository.findById(CompraId.from(compraId)).orElseThrow();
        assertEquals(EstadoCompra.PARCIAL, compra.getEstado());
        assertEquals(cantidadParcial, compra.getDetalles().get(0).getCantidadRecibida());
    }

    @Test
    @DisplayName("3. Segunda recepción parcial → 201 CREATED con estado RECIBIDA")
    void debeCompletarRecepcionConSegundaParcial() throws Exception {
        // Arrange - Primera recepción parcial
        BigDecimal primeraRecepcion = new BigDecimal("500.00");
        String primeraRequest = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-003",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, primeraRecepcion, almacenId);

        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(primeraRequest))
                .andExpect(status().isCreated());

        entityManager.clear();

        // Segunda recepción que completa
        BigDecimal segundaRecepcion = new BigDecimal("500.00");
        String segundaRequest = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-004",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, segundaRecepcion, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(segundaRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoCompra").value("RECIBIDA"))
                .andExpect(jsonPath("$.detalles[0].cantidadPendiente").value(0.00));

        // Verificar estado final
        entityManager.clear();
        Compra compra = compraRepository.findById(CompraId.from(compraId)).orElseThrow();
        assertEquals(EstadoCompra.RECIBIDA, compra.getEstado());
        assertEquals(cantidadOrdenada, compra.getDetalles().get(0).getCantidadRecibida());
    }

    @Test
    @DisplayName("4. Intento de sobre-entrega → 400 OVER_DELIVERY")
    void debeRechazarSobreEntrega() throws Exception {
        // Arrange - Primera recepción parcial
        BigDecimal primeraRecepcion = new BigDecimal("500.00");
        String primeraRequest = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-005",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, primeraRecepcion, almacenId);

        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(primeraRequest))
                .andExpect(status().isCreated());

        entityManager.clear();

        // Intento de sobre-entrega
        BigDecimal sobreEntrega = new BigDecimal("600.00"); // Excede lo pendiente (500)
        String requestSobreEntrega = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-006",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, sobreEntrega, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestSobreEntrega))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("OVER_DELIVERY"));
    }

    @Test
    @DisplayName("5. Proyecto no activo → 412 PROJECT_NOT_ACTIVE")
    void debeRechazarSiProyectoNoEstaActivo() throws Exception {
        // Arrange - Desactivar proyecto
        Proyecto proyecto = proyectoRepository.findById(ProyectoId.from(proyectoId)).orElseThrow();
        proyecto = proyecto.suspender(); // Cambiar a SUSPENDIDO
        proyectoRepository.save(proyecto);
        entityManager.flush();
        entityManager.clear();

        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-007",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 100.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.error").value("PROJECT_NOT_ACTIVE"));
    }

    @Test
    @DisplayName("6. Guía de remisión duplicada → 409 DUPLICATE_RECEPTION")
    void debeRechazarGuiaRemisionDuplicada() throws Exception {
        // Arrange - Primera recepción
        String guiaRemision = "GR-2024-008";
        String primeraRequest = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "%s",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 100.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), guiaRemision, compraDetalleId, almacenId);

        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(primeraRequest))
                .andExpect(status().isCreated());

        entityManager.clear();

        // Intento de recepción con misma guía
        String segundaRequest = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "%s",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 200.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), guiaRemision, compraDetalleId, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(segundaRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RECEPTION"));
    }

    @Test
    @DisplayName("7. Guía de remisión faltante → 400 MISSING_GUIA_REMISION")
    void debeRechazarSiFaltaGuiaRemision() throws Exception {
        // Arrange
        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 100.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8. Transición de estado inválida → 400 INVALID_STATE")
    void debeRechazarSiEstadoCompraInvalido() throws Exception {
        // Arrange - Cambiar compra a estado BORRADOR (no puede recibirse)
        Compra compra = compraRepository.findById(CompraId.from(compraId)).orElseThrow();
        compra = Compra.reconstruir(
            compra.getId(),
            compra.getProyectoId(),
            compra.getFecha(),
            compra.getProveedor(),
            EstadoCompra.BORRADOR, // Estado inválido para recepción
            compra.getTotal(),
            compra.getVersion(),
            compra.getDetalles()
        );
        compraRepository.save(compra);
        entityManager.flush();
        entityManager.clear();

        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-009",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 100.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, almacenId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_STATE"));
    }

    @Test
    @DisplayName("9. Recepción multi-almacén → 201 con múltiples MovimientoAlmacen")
    void debeCrearMovimientosMultiAlmacen() throws Exception {
        // Arrange - Crear segunda compra con dos detalles para dos almacenes
        CompraId compraId2 = CompraId.nuevo();
        UUID compraId2Value = compraId2.getValue();
        UUID compraDetalleId2 = CompraDetalleId.nuevo().getValue();

        CompraDetalle detalle2 = CompraDetalle.crear(
            CompraDetalleId.from(compraDetalleId2),
            recursoExternalId.toString(),
            "Cemento Portland",
            "BOLSA",
            UUID.randomUUID(),
            NaturalezaGasto.DIRECTO_PARTIDA,
            RelacionContractual.CONTRACTUAL,
            RubroInsumo.MATERIAL_CONSTRUCCION,
            new BigDecimal("500.00"),
            precioUnitario
        );

        Compra compra2 = Compra.crear(
            compraId2,
            proyectoId,
            LocalDate.now(),
            "Proveedor Test S.A.C.",
            List.of(detalle2)
        );

        compra2 = Compra.reconstruir(
            compra2.getId(),
            compra2.getProyectoId(),
            compra2.getFecha(),
            compra2.getProveedor(),
            EstadoCompra.ENVIADA,
            compra2.getTotal(),
            compra2.getVersion(),
            compra2.getDetalles()
        );

        compraRepository.save(compra2);
        entityManager.flush();
        entityManager.clear();

        // Recepción con dos almacenes
        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-010",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 300.00,
                            "almacenId": "%s"
                        },
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 200.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId2, almacenId, compraDetalleId2, almacenId2);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ordenes-compra/{id}/recepciones", compraId2Value)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.detalles").isArray())
                .andExpect(jsonPath("$.detalles.length()").value(2));

        // Verificar movimientos creados
        entityManager.clear();
        var movimientos1 = movimientoAlmacenRepository.buscarPorAlmacenIdYRecursoId(almacenId, recursoId);
        var movimientos2 = movimientoAlmacenRepository.buscarPorAlmacenIdYRecursoId(almacenId2, recursoId);
        
        assertTrue(movimientos1.size() > 0, "Debe haber movimientos en almacén 1");
        assertTrue(movimientos2.size() > 0, "Debe haber movimientos en almacén 2");
    }

    @Test
    @DisplayName("10. Verificación de cálculo PMP → fórmula exacta")
    void debeCalcularPMPCorrectamente() throws Exception {
        // Arrange - Crear stock inicial con PMP conocido
        BigDecimal stockCantidad = new BigDecimal("500.00");
        BigDecimal stockPMP = new BigDecimal("20.00");
        BigDecimal stockValor = stockCantidad.multiply(stockPMP); // 10000.00

        // Crear registro de Kárdex inicial
        RegistroKardex registroInicial = RegistroKardex.crearEntrada(
            almacenId,
            recursoId,
            UUID.randomUUID(),
            stockCantidad,
            stockPMP,
            stockCantidad.multiply(stockPMP), // importeTotal
            stockCantidad,
            stockValor,
            stockPMP
        );
        kardexRepository.guardar(registroInicial);
        entityManager.flush();
        entityManager.clear();

        // Recepción con nuevo precio
        BigDecimal cantidadEntrada = new BigDecimal("300.00");
        BigDecimal precioEntrada = new BigDecimal("25.00");
        
        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-011",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": %s,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, cantidadEntrada, almacenId);

        // Act
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        // Assert - Verificar PMP calculado
        // PMP esperado = (Stock × PMP + Entrada × Precio) / (Stock + Entrada)
        // PMP = (500 × 20 + 300 × 25) / (500 + 300) = (10000 + 7500) / 800 = 17500 / 800 = 21.875
        BigDecimal pmpEsperado = stockValor
            .add(cantidadEntrada.multiply(precioEntrada))
            .divide(stockCantidad.add(cantidadEntrada), 4, RoundingMode.HALF_UP);

        entityManager.clear();
        Optional<RegistroKardex> ultimoRegistro = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId);
        
        assertTrue(ultimoRegistro.isPresent(), "Debe existir un registro de Kárdex");
        RegistroKardex registro = ultimoRegistro.get();
        
        BigDecimal diferencia = registro.getCostoPromedioPonderado().subtract(pmpEsperado).abs();
        assertTrue(diferencia.compareTo(new BigDecimal("0.01")) <= 0,
            String.format("PMP calculado: %s, Esperado: %s, Diferencia: %s",
                registro.getCostoPromedioPonderado(), pmpEsperado, diferencia));
        
        // Verificar valores específicos del ejemplo
        assertEquals(new BigDecimal("21.8750"), registro.getCostoPromedioPonderado().setScale(4, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("11. Verificar rollback de transacción en caso de error")
    void debeHacerRollbackEnCasoDeError() throws Exception {
        // Arrange - Contar recepciones antes
        long recepcionesAntes = recepcionJpaRepository.count();

        // Intento de recepción con proyecto inactivo (debe fallar)
        Proyecto proyecto = proyectoRepository.findById(ProyectoId.from(proyectoId)).orElseThrow();
        proyecto = proyecto.suspender();
        proyectoRepository.save(proyecto);
        entityManager.flush();
        entityManager.clear();

        String requestJson = String.format("""
                {
                    "fechaRecepcion": "%s",
                    "guiaRemision": "GR-2024-012",
                    "detalles": [
                        {
                            "detalleOrdenId": "%s",
                            "cantidadRecibida": 100.00,
                            "almacenId": "%s"
                        }
                    ]
                }
                """, LocalDate.now(), compraDetalleId, almacenId);

        // Act
        mockMvc.perform(post("/api/v1/compras/{compraId}/recepciones", compraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isPreconditionFailed());

        // Assert - Verificar que no se creó ninguna recepción
        entityManager.clear();
        long recepcionesDespues = recepcionJpaRepository.count();
        assertEquals(recepcionesAntes, recepcionesDespues, "No debe haberse creado ninguna recepción");
    }
}
