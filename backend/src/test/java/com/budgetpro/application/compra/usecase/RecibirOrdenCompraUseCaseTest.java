package com.budgetpro.application.compra.usecase;

import com.budgetpro.application.compra.command.RecibirOrdenCompraCommand;
import com.budgetpro.application.compra.exception.BusinessRuleException;
import com.budgetpro.domain.catalogo.model.RecursoProxy;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.logistica.almacen.model.Almacen;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.port.out.AlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.domain.logistica.almacen.service.GestionKardexService;
import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.RecepcionRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para RecibirOrdenCompraUseCase.
 * 
 * Verifica la orquestación completa del workflow de recepción con repositorios mockeados.
 * 
 * Escenarios cubiertos:
 * - Happy path: recepción completa
 * - Happy path: recepción parcial
 * - Error: proyecto no activo
 * - Error: guía de remisión duplicada
 * - Error: sobre-entrega (over-delivery)
 * - Error: estado inválido de compra
 * - Error: detalle de compra no encontrado
 * - Error: almacén no encontrado
 * - Error: almacén inactivo
 * - Error: recurso proxy no encontrado
 */
@ExtendWith(MockitoExtension.class)
class RecibirOrdenCompraUseCaseTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private RecepcionRepository recepcionRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private MovimientoAlmacenRepository movimientoAlmacenRepository;

    @Mock
    private RegistroKardexRepository kardexRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Mock
    private GestionKardexService gestionKardexService;

    @Mock
    private RecursoProxyRepository recursoProxyRepository;

    @InjectMocks
    private RecibirOrdenCompraUseCase useCase;

    private UUID compraId;
    private UUID proyectoId;
    private UUID usuarioId;
    private UUID almacenId;
    private UUID detalleCompraId;
    private UUID recursoId;
    private CompraId compraDomainId;
    private ProyectoId proyectoDomainId;
    private AlmacenId almacenDomainId;
    private CompraDetalleId detalleCompraDomainId;
    private String guiaRemision;
    private LocalDate fechaRecepcion;

    @BeforeEach
    void setUp() {
        compraId = UUID.randomUUID();
        proyectoId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        almacenId = UUID.randomUUID();
        detalleCompraId = UUID.randomUUID();
        recursoId = UUID.randomUUID();
        
        compraDomainId = CompraId.from(compraId);
        proyectoDomainId = ProyectoId.from(proyectoId);
        almacenDomainId = AlmacenId.of(almacenId);
        detalleCompraDomainId = CompraDetalleId.from(detalleCompraId);
        
        guiaRemision = "GR-2024-001";
        fechaRecepcion = LocalDate.now();
    }

    @Test
    @DisplayName("Debe recibir orden de compra completamente (happy path)")
    void debeRecibirOrdenCompraCompletamente() {
        // Arrange
        BigDecimal cantidadOrdenada = new BigDecimal("100.00");
        BigDecimal cantidadRecibida = new BigDecimal("100.00");
        BigDecimal precioUnitario = new BigDecimal("10.50");

        CompraDetalle detalleCompra = crearCompraDetalle(
            detalleCompraDomainId,
            "MAT-001",
            cantidadOrdenada,
            precioUnitario,
            BigDecimal.ZERO // cantidadRecibida inicial
        );

        Compra compra = Compra.reconstruir(
            compraDomainId,
            proyectoId,
            fechaRecepcion,
            "Proveedor Test",
            EstadoCompra.ENVIADA,
            new BigDecimal("1050.00"),
            1L,
            List.of(detalleCompra)
        );

        Proyecto proyecto = Proyecto.reconstruir(
            proyectoDomainId,
            "Proyecto Test",
            "Lima",
            EstadoProyecto.ACTIVO
        );

        Almacen almacen = Almacen.reconstruir(
            almacenDomainId,
            proyectoId,
            "ALM-001",
            "Almacén Principal",
            "Lima",
            usuarioId,
            true
        );

        RecursoProxy recursoProxy = crearRecursoProxy("MAT-001");

        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleCompraId,
                    cantidadRecibida,
                    almacenId
                )
            ),
            usuarioId
        );

        // Mock repository responses
        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision)).thenReturn(false);
        when(almacenRepository.buscarPorId(almacenDomainId)).thenReturn(Optional.of(almacen));
        when(recursoProxyRepository.findByExternalId("MAT-001", "CAPECO")).thenReturn(Optional.of(recursoProxy));
        when(kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId))
            .thenReturn(Optional.empty());

        RegistroKardex nuevoRegistroKardex = crearRegistroKardexEntrada(
            almacenId,
            recursoId,
            cantidadRecibida,
            precioUnitario
        );

        when(gestionKardexService.procesarEntrada(
            eq(almacenId),
            eq(recursoId),
            eq(cantidadRecibida),
            eq(precioUnitario),
            any(UUID.class),
            eq(BigDecimal.ZERO),
            eq(BigDecimal.ZERO)
        )).thenReturn(nuevoRegistroKardex);

        // Act
        Recepcion recepcion = useCase.ejecutar(command);

        // Assert
        assertNotNull(recepcion);
        assertNotNull(recepcion.getId());

        // Verificar que se guardó la recepción
        verify(recepcionRepository, times(1)).save(any(Recepcion.class));

        // Verificar que se actualizó la compra
        verify(compraRepository, times(1)).save(any(Compra.class));

        // Verificar que se creó el movimiento de almacén
        verify(movimientoAlmacenRepository, times(1)).guardar(any(MovimientoAlmacen.class));

        // Verificar que se guardó el registro de kárdex
        verify(kardexRepository, times(1)).guardar(any(RegistroKardex.class));

        // Verificar que la compra se marcó como RECIBIDA
        verify(compraRepository).save(argThat(c -> c.getEstado() == EstadoCompra.RECIBIDA));
    }

    @Test
    @DisplayName("Debe recibir orden de compra parcialmente (happy path)")
    void debeRecibirOrdenCompraParcialmente() {
        // Arrange
        BigDecimal cantidadOrdenada = new BigDecimal("100.00");
        BigDecimal cantidadRecibida = new BigDecimal("50.00");
        BigDecimal precioUnitario = new BigDecimal("10.50");

        CompraDetalle detalleCompra = crearCompraDetalle(
            detalleCompraDomainId,
            "MAT-001",
            cantidadOrdenada,
            precioUnitario,
            BigDecimal.ZERO
        );

        Compra compra = Compra.reconstruir(
            compraDomainId,
            proyectoId,
            fechaRecepcion,
            "Proveedor Test",
            EstadoCompra.ENVIADA,
            new BigDecimal("1050.00"),
            1L,
            List.of(detalleCompra)
        );

        Proyecto proyecto = Proyecto.reconstruir(
            proyectoDomainId,
            "Proyecto Test",
            "Lima",
            EstadoProyecto.ACTIVO
        );

        Almacen almacen = Almacen.reconstruir(
            almacenDomainId,
            proyectoId,
            "ALM-001",
            "Almacén Principal",
            "Lima",
            usuarioId,
            true
        );

        RecursoProxy recursoProxy = crearRecursoProxy("MAT-001");

        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleCompraId,
                    cantidadRecibida,
                    almacenId
                )
            ),
            usuarioId
        );

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision)).thenReturn(false);
        when(almacenRepository.buscarPorId(almacenDomainId)).thenReturn(Optional.of(almacen));
        when(recursoProxyRepository.findByExternalId("MAT-001", "CAPECO")).thenReturn(Optional.of(recursoProxy));
        when(kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId))
            .thenReturn(Optional.empty());

        RegistroKardex nuevoRegistroKardex = crearRegistroKardexEntrada(
            almacenId,
            recursoId,
            cantidadRecibida,
            precioUnitario
        );

        when(gestionKardexService.procesarEntrada(
            eq(almacenId),
            eq(recursoId),
            eq(cantidadRecibida),
            eq(precioUnitario),
            any(UUID.class),
            eq(BigDecimal.ZERO),
            eq(BigDecimal.ZERO)
        )).thenReturn(nuevoRegistroKardex);

        // Act
        Recepcion recepcion = useCase.ejecutar(command);

        // Assert
        assertNotNull(recepcion);
        assertNotNull(recepcion.getId());

        // Verificar que se guardó la recepción
        verify(recepcionRepository, times(1)).save(any(Recepcion.class));

        // Verificar que se actualizó la compra a PARCIAL
        verify(compraRepository).save(argThat(c -> c.getEstado() == EstadoCompra.PARCIAL));

        // Verificar que se actualizó la cantidad recibida en el detalle
        verify(compraRepository).save(argThat(c -> 
            c.getDetalles().get(0).getCantidadRecibida().compareTo(cantidadRecibida) == 0
        ));
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si el proyecto no está ACTIVO")
    void debeLanzarExcepcionSiProyectoNoEstaActivo() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = Proyecto.reconstruir(
            proyectoDomainId,
            "Proyecto Test",
            "Lima",
            EstadoProyecto.SUSPENDIDO
        );

        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("no está ACTIVO"));
        assertTrue(exception.getMessage().contains("SUSPENDIDO"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si la guía de remisión ya existe")
    void debeLanzarExcepcionSiGuiaRemisionDuplicada() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = crearProyectoActivo();

        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Ya existe una recepción con la guía de remisión"));
        assertTrue(exception.getMessage().contains(guiaRemision));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar BusinessRuleException si hay sobre-entrega (over-delivery)")
    void debeLanzarExcepcionSiSobreEntrega() {
        // Arrange
        BigDecimal cantidadOrdenada = new BigDecimal("100.00");
        BigDecimal cantidadRecibida = new BigDecimal("150.00"); // Excede la ordenada
        BigDecimal precioUnitario = new BigDecimal("10.50");

        CompraDetalle detalleCompra = crearCompraDetalle(
            detalleCompraDomainId,
            "MAT-001",
            cantidadOrdenada,
            precioUnitario,
            BigDecimal.ZERO
        );

        Compra compra = Compra.reconstruir(
            compraDomainId,
            proyectoId,
            fechaRecepcion,
            "Proveedor Test",
            EstadoCompra.ENVIADA,
            new BigDecimal("1050.00"),
            1L,
            List.of(detalleCompra)
        );

        Proyecto proyecto = crearProyectoActivo();

        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleCompraId,
                    cantidadRecibida,
                    almacenId
                )
            ),
            usuarioId
        );

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Sobre-entrega detectada"));
        assertTrue(exception.getMessage().contains("150.00"));
        assertTrue(exception.getMessage().contains("100.00"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si la compra no está en estado ENVIADA o PARCIAL")
    void debeLanzarExcepcionSiEstadoCompraInvalido() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.BORRADOR);
        Proyecto proyecto = crearProyectoActivo();

        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("debe estar en estado ENVIADA o PARCIAL"));
        assertTrue(exception.getMessage().contains("BORRADOR"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el detalle de compra no existe")
    void debeLanzarExcepcionSiDetalleCompraNoExiste() {
        // Arrange
        UUID detalleInexistenteId = UUID.randomUUID();
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = crearProyectoActivo();

        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleInexistenteId, // ID que no existe en la compra
                    new BigDecimal("50.00"),
                    almacenId
                )
            ),
            usuarioId
        );

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Detalle de compra no encontrado"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el almacén no existe")
    void debeLanzarExcepcionSiAlmacenNoExiste() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = crearProyectoActivo();
        AlmacenId almacenInexistenteId = AlmacenId.of(UUID.randomUUID());

        RecibirOrdenCompraCommand command = new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleCompraId,
                    new BigDecimal("50.00"),
                    almacenInexistenteId.getValue()
                )
            ),
            usuarioId
        );

        RecursoProxy recursoProxy = crearRecursoProxy("MAT-001");

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);
        when(recursoProxyRepository.findByExternalId("MAT-001", "CAPECO")).thenReturn(Optional.of(recursoProxy));
        when(almacenRepository.buscarPorId(almacenInexistenteId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Almacén no encontrado"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si el almacén no está activo")
    void debeLanzarExcepcionSiAlmacenNoEstaActivo() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = crearProyectoActivo();
        Almacen almacenInactivo = Almacen.reconstruir(
            almacenDomainId,
            proyectoId,
            "ALM-001",
            "Almacén Principal",
            "Lima",
            usuarioId,
            false // Inactivo
        );

        RecibirOrdenCompraCommand command = crearCommandBasico();

        RecursoProxy recursoProxy = crearRecursoProxy("MAT-001");

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);
        when(recursoProxyRepository.findByExternalId("MAT-001", "CAPECO")).thenReturn(Optional.of(recursoProxy));
        when(almacenRepository.buscarPorId(almacenDomainId)).thenReturn(Optional.of(almacenInactivo));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("no está activo"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el recurso proxy no se encuentra")
    void debeLanzarExcepcionSiRecursoProxyNoExiste() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        Proyecto proyecto = crearProyectoActivo();

        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.of(proyecto));
        when(recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision))
            .thenReturn(false);
        when(recursoProxyRepository.findByExternalId("MAT-001", "CAPECO")).thenReturn(Optional.empty());
        when(recursoProxyRepository.findByExternalId("MAT-001", "CATALOGO_GLOBAL")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("No se encontró RecursoProxy"));
        assertTrue(exception.getMessage().contains("MAT-001"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la compra no existe")
    void debeLanzarExcepcionSiCompraNoExiste() {
        // Arrange
        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Compra no encontrada"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el proyecto no existe")
    void debeLanzarExcepcionSiProyectoNoExiste() {
        // Arrange
        Compra compra = crearCompraBasica(EstadoCompra.ENVIADA);
        RecibirOrdenCompraCommand command = crearCommandBasico();

        when(compraRepository.findById(compraDomainId)).thenReturn(Optional.of(compra));
        when(proyectoRepository.findById(proyectoDomainId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> useCase.ejecutar(command)
        );

        assertTrue(exception.getMessage().contains("Proyecto no encontrado"));

        // Verificar que no se persistió nada
        verify(recepcionRepository, never()).save(any());
        verify(compraRepository, never()).save(any());
    }

    // Helper methods

    private Compra crearCompraBasica(EstadoCompra estado) {
        CompraDetalle detalle = crearCompraDetalle(
            detalleCompraDomainId,
            "MAT-001",
            new BigDecimal("100.00"),
            new BigDecimal("10.50"),
            BigDecimal.ZERO
        );

        return Compra.reconstruir(
            compraDomainId,
            proyectoId,
            fechaRecepcion,
            "Proveedor Test",
            estado,
            new BigDecimal("1050.00"),
            1L,
            List.of(detalle)
        );
    }

    private CompraDetalle crearCompraDetalle(
            CompraDetalleId id,
            String recursoExternalId,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            BigDecimal cantidadRecibida) {
        return CompraDetalle.reconstruir(
            id,
            recursoExternalId,
            "Cemento Portland",
            "BOLSA",
            UUID.randomUUID(),
            NaturalezaGasto.DIRECTO_PARTIDA,
            RelacionContractual.CONTRACTUAL,
            RubroInsumo.MATERIAL_CONSTRUCCION,
            cantidad,
            precioUnitario,
            cantidad.multiply(precioUnitario),
            cantidadRecibida
        );
    }

    private Proyecto crearProyectoActivo() {
        return Proyecto.reconstruir(
            proyectoDomainId,
            "Proyecto Test",
            "Lima",
            EstadoProyecto.ACTIVO
        );
    }

    private RecibirOrdenCompraCommand crearCommandBasico() {
        return new RecibirOrdenCompraCommand(
            compraId,
            fechaRecepcion,
            guiaRemision,
            List.of(
                new RecibirOrdenCompraCommand.DetalleCommand(
                    detalleCompraId,
                    new BigDecimal("50.00"),
                    almacenId
                )
            ),
            usuarioId
        );
    }

    private RecursoProxy crearRecursoProxy(String externalId) {
        return RecursoProxy.reconstruir(
            RecursoProxyId.of(recursoId),
            externalId,
            "CAPECO",
            "Cemento Portland",
            com.budgetpro.domain.shared.model.TipoRecurso.MATERIAL,
            "BOLSA",
            new BigDecimal("10.50"),
            LocalDateTime.now(),
            com.budgetpro.domain.catalogo.model.EstadoProxy.ACTIVO,
            null,
            1L
        );
    }

    private RegistroKardex crearRegistroKardexEntrada(
            UUID almacenId,
            UUID recursoId,
            BigDecimal cantidad,
            BigDecimal precioUnitario) {
        BigDecimal importeTotal = cantidad.multiply(precioUnitario);
        BigDecimal saldoCantidad = cantidad;
        BigDecimal saldoValor = importeTotal;
        BigDecimal cpp = saldoValor.divide(saldoCantidad, 4, RoundingMode.HALF_UP);

        return RegistroKardex.crearEntrada(
            almacenId,
            recursoId,
            UUID.randomUUID(),
            cantidad,
            precioUnitario,
            importeTotal,
            saldoCantidad,
            saldoValor,
            cpp
        );
    }
}
