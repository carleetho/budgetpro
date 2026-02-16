package com.budgetpro.infrastructure.rest.compra.controller;

import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.compra.port.in.*;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.domain.shared.port.out.SecurityPort;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.rest.compra.dto.OrdenCompraRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para OrdenCompraController.
 * 
 * NOTA: Estos tests usan mocks para los use cases y repositorios para aislar
 * la capa REST. Para tests end-to-end completos, ver OrdenCompraE2ETest.
 */
@AutoConfigureMockMvc(addFilters = false)
class OrdenCompraControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrearOrdenCompraUseCase crearOrdenCompraUseCase;

    @MockBean
    private SolicitarAprobacionUseCase solicitarAprobacionUseCase;

    @MockBean
    private AprobarOrdenCompraUseCase aprobarOrdenCompraUseCase;

    @MockBean
    private EnviarOrdenCompraUseCase enviarOrdenCompraUseCase;

    @MockBean
    private ConfirmarRecepcionUseCase confirmarRecepcionUseCase;

    @MockBean
    private OrdenCompraRepository ordenCompraRepository;

    @MockBean
    private ProveedorRepository proveedorRepository;

    @MockBean
    private SecurityPort securityPort;

    private UUID testUserId;
    private UUID proyectoId;
    private ProveedorId proveedorId;
    private OrdenCompraId ordenId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        proyectoId = UUID.randomUUID();
        proveedorId = ProveedorId.from(UUID.randomUUID());
        ordenId = OrdenCompraId.from(UUID.randomUUID());

        when(securityPort.getCurrentUserId()).thenReturn(testUserId);
    }

    @Test
    @DisplayName("POST /api/v1/ordenes-compra debe crear una orden de compra y retornar 201 CREATED")
    void debeCrearOrdenCompra() throws Exception {
        OrdenCompraRequest request = new OrdenCompraRequest(
            proyectoId,
            proveedorId.getValue(), // OrdenCompraRequest espera UUID, no ProveedorId
            LocalDate.now(),
            "Pago a 30 días",
            "Urgente",
            List.of(
                new com.budgetpro.infrastructure.rest.compra.dto.DetalleOrdenCompraRequest(
                    UUID.randomUUID(),
                    "Cemento",
                    new BigDecimal("100"),
                    "kg",
                    new BigDecimal("10.00")
                )
            )
        );

        when(crearOrdenCompraUseCase.crear(any(CrearOrdenCompraUseCase.CrearOrdenCompraCommand.class))).thenReturn(ordenId);

        OrdenCompra ordenCreada = crearOrdenCompraMock();
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(ordenCreada));

        Proveedor proveedor = crearProveedorMock();
        when(proveedorRepository.findById(proveedorId)).thenReturn(java.util.Optional.of(proveedor));

        mockMvc.perform(post("/api/v1/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.numero").exists())
            .andExpect(jsonPath("$.estado").value("BORRADOR"))
            .andExpect(jsonPath("$.montoTotal").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/v1/ordenes-compra/{id} debe retornar 200 OK con la orden")
    void debeObtenerOrdenCompraPorId() throws Exception {
        OrdenCompra orden = crearOrdenCompraMock();
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(orden));

        Proveedor proveedor = crearProveedorMock();
        when(proveedorRepository.findById(proveedorId)).thenReturn(java.util.Optional.of(proveedor));

        mockMvc.perform(get("/api/v1/ordenes-compra/{id}", ordenId.getValue()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ordenId.getValue().toString()))
            .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    @DisplayName("GET /api/v1/ordenes-compra/{id} debe retornar 404 NOT FOUND cuando la orden no existe")
    void debeRetornar404CuandoOrdenNoExiste() throws Exception {
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/ordenes-compra/{id}", ordenId.getValue()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/ordenes-compra/{id}/solicitar debe cambiar estado a SOLICITADA")
    void debeSolicitarAprobacion() throws Exception {
        OrdenCompra orden = crearOrdenCompraMock();
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(orden));

        // El método solicitar es void, no retorna nada
        doNothing().when(solicitarAprobacionUseCase).solicitar(any(OrdenCompraId.class), any(UUID.class));

        OrdenCompra ordenSolicitada = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(ordenSolicitada));

        Proveedor proveedor = crearProveedorMock();
        when(proveedorRepository.findById(proveedorId)).thenReturn(java.util.Optional.of(proveedor));

        mockMvc.perform(post("/api/v1/ordenes-compra/{id}/solicitar", ordenId.getValue()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("SOLICITADA"));
    }

    @Test
    @DisplayName("POST /api/v1/ordenes-compra/{id}/aprobar debe cambiar estado a APROBADA")
    void debeAprobarOrdenCompra() throws Exception {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(orden));

        // El método aprobar es void, no retorna nada
        doNothing().when(aprobarOrdenCompraUseCase).aprobar(any(OrdenCompraId.class), any(UUID.class));

        OrdenCompra ordenAprobada = crearOrdenCompraEnEstado(OrdenCompraEstado.APROBADA);
        when(ordenCompraRepository.findById(ordenId)).thenReturn(java.util.Optional.of(ordenAprobada));

        Proveedor proveedor = crearProveedorMock();
        when(proveedorRepository.findById(proveedorId)).thenReturn(java.util.Optional.of(proveedor));

        mockMvc.perform(post("/api/v1/ordenes-compra/{id}/aprobar", ordenId.getValue()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    private OrdenCompra crearOrdenCompraMock() {
        return crearOrdenCompraEnEstado(OrdenCompraEstado.BORRADOR);
    }

    private OrdenCompra crearOrdenCompraEnEstado(OrdenCompraEstado estado) {
        List<DetalleOrdenCompra> detalles = List.of(
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Cemento", new BigDecimal("100"), "kg", new BigDecimal("10.00"))
        );
        return OrdenCompra.reconstruir(
            ordenId,
            "PO-2024-001",
            proyectoId,
            proveedorId,
            LocalDate.now(),
            estado,
            new BigDecimal("1000.00"),
            null,
            null,
            1L,
            testUserId,
            LocalDateTime.now(),
            testUserId,
            LocalDateTime.now(),
            detalles
        );
    }

    private Proveedor crearProveedorMock() {
        return Proveedor.crear(
            proveedorId,
            "Proveedor Test",
            "20123456789",
            null,
            null,
            testUserId,
            LocalDateTime.now()
        );
    }
}
