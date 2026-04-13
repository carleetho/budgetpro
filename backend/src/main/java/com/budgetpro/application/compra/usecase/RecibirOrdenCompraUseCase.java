package com.budgetpro.application.compra.usecase;

import com.budgetpro.application.compra.command.RecibirOrdenCompraCommand;
import com.budgetpro.application.compra.exception.BusinessRuleException;
import com.budgetpro.application.compra.exception.DuplicateReceptionException;
import com.budgetpro.application.compra.exception.InvalidStateException;
import com.budgetpro.application.compra.exception.ProjectNotActiveException;
import com.budgetpro.application.compra.port.in.RecibirOrdenCompraInputPort;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.port.out.AlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.domain.logistica.almacen.service.GestionKardexService;
import com.budgetpro.domain.catalogo.port.RecursoProxyRepository;
import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.compra.port.out.CompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.RecepcionRepository;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para recibir una orden de compra.
 * 
 * Orquesta el workflow completo de recepción con integración sincrónica de inventario
 * y recálculo de PMP (Precio Medio Ponderado).
 * 
 * REGLA-150: Proyecto debe estar ACTIVO
 * REGLA-117: Generar MovimientoAlmacen sincrónicamente
 * REGLA-167: Registrar usuario que crea la recepción
 */
@Service
public class RecibirOrdenCompraUseCase implements RecibirOrdenCompraInputPort {

    private final CompraRepository compraRepository;
    private final RecepcionRepository recepcionRepository;
    private final ProyectoRepository proyectoRepository;
    private final MovimientoAlmacenRepository movimientoAlmacenRepository;
    private final RegistroKardexRepository kardexRepository;
    private final AlmacenRepository almacenRepository;
    private final GestionKardexService gestionKardexService;
    private final RecursoProxyRepository recursoProxyRepository;

    public RecibirOrdenCompraUseCase(
            CompraRepository compraRepository,
            RecepcionRepository recepcionRepository,
            ProyectoRepository proyectoRepository,
            MovimientoAlmacenRepository movimientoAlmacenRepository,
            RegistroKardexRepository kardexRepository,
            AlmacenRepository almacenRepository,
            GestionKardexService gestionKardexService,
            RecursoProxyRepository recursoProxyRepository) {
        this.compraRepository = compraRepository;
        this.recepcionRepository = recepcionRepository;
        this.proyectoRepository = proyectoRepository;
        this.movimientoAlmacenRepository = movimientoAlmacenRepository;
        this.kardexRepository = kardexRepository;
        this.almacenRepository = almacenRepository;
        this.gestionKardexService = gestionKardexService;
        this.recursoProxyRepository = recursoProxyRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Recepcion ejecutar(RecibirOrdenCompraCommand command) {
        // Step 1: Validar rol de usuario (RESIDENTE)
        // NOTA: La validación de roles se hace típicamente en el controlador o mediante Spring Security
        // Por ahora, asumimos que el usuario tiene los permisos necesarios
        
        // Step 2: Validar que el proyecto existe y está ACTIVO (REGLA-150)
        CompraId compraId = CompraId.from(command.getCompraId());
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Compra no encontrada: %s", compraId.getValue())
                ));
        
        ProyectoId proyectoId = ProyectoId.from(compra.getProyectoId());
        var proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Proyecto no encontrado: %s", compra.getProyectoId())
                ));
        
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
            throw new ProjectNotActiveException(
                String.format("El proyecto %s no está ACTIVO. Estado actual: %s", 
                    proyecto.getNombre(), proyecto.getEstado())
            );
        }
        
        // Step 3: Verificar idempotencia (guía de remisión única por compra)
        if (recepcionRepository.existsByCompraIdAndGuiaRemision(compraId, command.getGuiaRemision())) {
            throw new DuplicateReceptionException(
                String.format("Ya existe una recepción con la guía de remisión '%s' para la compra %s",
                    command.getGuiaRemision(), compraId.getValue())
            );
        }
        
        // Step 4: La compra ya está cargada (sin locking pesimista, usamos optimistic locking con @Version)
        // El locking optimista se maneja automáticamente con @Version en la entidad
        
        // Step 5: Validar estado de compra (debe estar ENVIADA o PARCIAL)
        if (compra.getEstado() != EstadoCompra.ENVIADA && compra.getEstado() != EstadoCompra.PARCIAL) {
            throw new InvalidStateException(
                String.format("La compra debe estar en estado ENVIADA o PARCIAL para recibir. Estado actual: %s",
                    compra.getEstado())
            );
        }
        
        // Step 6: Validar cantidades (hard block over-delivery)
        // Crear mapa de detalles de compra por ID para validación rápida
        Map<UUID, CompraDetalle> detallesPorId = compra.getDetalles().stream()
                .collect(Collectors.toMap(d -> d.getId().getValue(), d -> d));
        
        for (var detalleCommand : command.getDetalles()) {
            CompraDetalle detalleCompra = detallesPorId.get(detalleCommand.getDetalleOrdenId());
            if (detalleCompra == null) {
                throw new IllegalArgumentException(
                    String.format("Detalle de compra no encontrado: %s", detalleCommand.getDetalleOrdenId())
                );
            }
            
            BigDecimal cantidadPendiente = detalleCompra.getCantidadPendiente();
            if (detalleCommand.getCantidadRecibida().compareTo(cantidadPendiente) > 0) {
                throw new BusinessRuleException(
                    String.format("Sobre-entrega detectada. Detalle %s: cantidad recibida %s excede pendiente %s",
                        detalleCommand.getDetalleOrdenId(), 
                        detalleCommand.getCantidadRecibida(), 
                        cantidadPendiente)
                );
            }
        }
        
        // Step 7: Preparar recepción (los detalles se crean después de crear MovimientoAlmacen)
        RecepcionId recepcionId = RecepcionId.generate();
        List<RecepcionDetalle> detallesRecepcion = new ArrayList<>();
        
        // Step 8: Actualizar cantidad_recibida en CompraDetalle
        for (var detalleCommand : command.getDetalles()) {
            CompraDetalle detalleCompra = detallesPorId.get(detalleCommand.getDetalleOrdenId());
            detalleCompra.registrarRecepcion(detalleCommand.getCantidadRecibida());
        }
        
        // Step 9: Determinar nuevo estado de Compra (PARCIAL vs RECIBIDA)
        if (compra.estaCompletamenteRecibida()) {
            compra.marcarComoRecibida();
        } else {
            compra.marcarComoParcialmenteRecibida();
        }
        
        // Step 10: Para cada detalle del comando: crear MovimientoAlmacen, procesar Kardex, recalcular PMP y crear RecepcionDetalle
        for (var detalleCommand : command.getDetalles()) {
            CompraDetalle detalleCompra = detallesPorId.get(detalleCommand.getDetalleOrdenId());
            
            // Obtener precio unitario del detalle de compra
            BigDecimal precioUnitario = detalleCompra.getPrecioUnitario();
            
            // Obtener recursoId desde recursoExternalId usando servicio de catálogo
            UUID recursoId = obtenerRecursoIdDesdeExternalId(detalleCompra.getRecursoExternalId());
            
            AlmacenId almacenId = AlmacenId.of(detalleCommand.getAlmacenId());
            
            // Validar que el almacén existe y está activo
            var almacen = almacenRepository.buscarPorId(almacenId)
                    .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Almacén no encontrado: %s", almacenId.getValue())
                    ));
            
            if (!almacen.isActivo()) {
                throw new IllegalStateException(
                    String.format("El almacén %s no está activo", almacenId.getValue())
                );
            }
            
            // Crear MovimientoAlmacen de entrada
            MovimientoAlmacenId movimientoId = MovimientoAlmacenId.generate();
            MovimientoAlmacen movimiento = MovimientoAlmacen.crearEntrada(
                movimientoId,
                almacenId,
                recursoId,
                command.getFechaRecepcion(),
                detalleCommand.getCantidadRecibida(),
                precioUnitario,
                command.getGuiaRemision(), // numeroDocumento
                String.format("Recepción de compra %s", compraId.getValue()) // observaciones
            );
            
            // Obtener último registro de Kárdex (locking implícito por transacción)
            RegistroKardex ultimoRegistro = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(
                almacenId.getValue(),
                recursoId
            ).orElse(null);
            
            BigDecimal saldoCantidadAnterior = ultimoRegistro != null 
                ? ultimoRegistro.getSaldoCantidad() 
                : BigDecimal.ZERO;
            BigDecimal saldoValorAnterior = ultimoRegistro != null 
                ? ultimoRegistro.getSaldoValor() 
                : BigDecimal.ZERO;
            
            // Procesar entrada y calcular nuevo PMP (REGLA-117)
            RegistroKardex nuevoRegistroKardex = gestionKardexService.procesarEntrada(
                almacenId.getValue(),
                recursoId,
                detalleCommand.getCantidadRecibida(),
                precioUnitario,
                movimientoId.getValue(),
                saldoCantidadAnterior,
                saldoValorAnterior
            );
            
            // Persistir movimiento y registro de Kárdex
            movimientoAlmacenRepository.guardar(movimiento);
            kardexRepository.guardar(nuevoRegistroKardex);
            
            // Crear RecepcionDetalle con el movimientoAlmacenId
            RecepcionDetalleId detalleId = RecepcionDetalleId.generate();
            RecepcionDetalle detalleRecepcion = RecepcionDetalle.crear(
                detalleId,
                detalleCommand.getDetalleOrdenId(),
                recursoId,
                almacenId,
                detalleCommand.getCantidadRecibida(),
                precioUnitario,
                movimientoId
            );
            detallesRecepcion.add(detalleRecepcion);
        }
        
        // Crear la recepción con los detalles creados
        Recepcion recepcion;
        try {
            recepcion = Recepcion.crear(
                recepcionId,
                compraId,
                command.getFechaRecepcion(),
                command.getGuiaRemision(),
                detallesRecepcion,
                command.getUsuarioId()
            );
        } catch (com.budgetpro.domain.logistica.compra.exception.CompraDomainRuleException e) {
            // Normalizar a excepción de aplicación para el boundary REST
            throw new BusinessRuleException(e.getMessage());
        }
        
        // Step 11: Persistir todos los cambios
        recepcionRepository.save(recepcion);
        compraRepository.save(compra);
        
        // Step 12: Retornar Recepcion completa
        return recepcion;
    }
    
    /**
     * Obtiene el recursoId (UUID) desde el recursoExternalId (String).
     * 
     * Busca el RecursoProxy usando el externalId. Intenta con diferentes catalogSource
     * si el primero no funciona (CAPECO, CATALOGO_GLOBAL).
     * 
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @return UUID del recurso
     * @throws IllegalArgumentException si no se encuentra el recurso proxy
     */
    private UUID obtenerRecursoIdDesdeExternalId(String recursoExternalId) {
        // Intentar con diferentes catalogSource comunes
        String[] catalogSources = {"CAPECO", "CATALOGO_GLOBAL"};
        
        for (String catalogSource : catalogSources) {
            var recursoProxy = recursoProxyRepository.findByExternalId(recursoExternalId, catalogSource);
            if (recursoProxy.isPresent()) {
                return recursoProxy.get().getId().getValue();
            }
        }
        
        throw new IllegalArgumentException(
            String.format("No se encontró RecursoProxy para externalId '%s' en ningún catálogo conocido",
                recursoExternalId)
        );
    }
}
