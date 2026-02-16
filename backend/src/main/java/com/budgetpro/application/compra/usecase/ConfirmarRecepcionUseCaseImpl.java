package com.budgetpro.application.compra.usecase;

import com.budgetpro.domain.logistica.compra.event.OrdenCompraRecibidaEvent;
import com.budgetpro.domain.logistica.compra.model.DetalleOrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.port.in.ConfirmarRecepcionUseCase;
import com.budgetpro.domain.logistica.compra.port.out.InventarioService;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para confirmar la recepción de una orden de compra.
 * 
 * Actualiza el inventario para partidas de tipo Material y publica el evento OrdenCompraRecibidaEvent.
 * Si la actualización del inventario falla, la transacción se revierte.
 */
@Service
public class ConfirmarRecepcionUseCaseImpl implements ConfirmarRecepcionUseCase {

    private final OrdenCompraRepository ordenCompraRepository;
    private final InventarioService inventarioService;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarRecepcionUseCaseImpl(OrdenCompraRepository ordenCompraRepository,
                                        InventarioService inventarioService,
                                        ApplicationEventPublisher eventPublisher) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.inventarioService = inventarioService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void confirmarRecepcion(OrdenCompraId ordenCompraId, UUID userId) {
        // 1. Cargar orden de compra
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Orden de compra no encontrada: %s", ordenCompraId)
                ));

        // 2. Confirmar recepción (ENVIADA → RECIBIDA)
        LocalDateTime now = LocalDateTime.now();
        ordenCompra.confirmarRecepcion(userId, now);

        // 3. Actualizar inventario para partidas de tipo Material
        // NOTA: Solo se actualiza inventario para partidas Material.
        // La validación del tipo de partida se hace en InventarioService.
        // Si una partida no es Material, InventarioService lanzará IllegalArgumentException
        // con un mensaje descriptivo, que se captura y se ignora para continuar con el siguiente detalle.
        // Si hay otro error (ej. bodega no encontrada), se propaga para hacer rollback de la transacción.
        String referencia = "PO-" + ordenCompra.getNumero();
        for (DetalleOrdenCompra detalle : ordenCompra.getDetalles()) {
            try {
                inventarioService.increaseStock(
                    ordenCompra.getProyectoId(),
                    detalle.getPartidaId(),
                    detalle.getCantidad(),
                    referencia
                );
            } catch (IllegalArgumentException e) {
                // Si la partida no es Material o no tiene APU, se ignora silenciosamente
                // (InventarioService lanzará IllegalArgumentException con mensaje descriptivo)
                String message = e.getMessage() != null ? e.getMessage() : "";
                if (message.contains("no tiene insumos de tipo MATERIAL") || 
                    message.contains("no tiene APU asociado")) {
                    // Partida no es Material o no tiene APU, continuar con el siguiente detalle
                    continue;
                }
                // Otro error de validación, propagar para hacer rollback
                throw e;
            } catch (IllegalStateException e) {
                // Errores de estado (ej. bodega no encontrada) deben hacer rollback
                throw e;
            }
        }

        // 4. Persistir cambios (si llegamos aquí, todo fue exitoso)
        ordenCompraRepository.save(ordenCompra);

        // 5. Publicar evento OrdenCompraRecibidaEvent
        List<OrdenCompraRecibidaEvent.DetalleEvento> detallesEvento = ordenCompra.getDetalles().stream()
                .map(detalle -> new OrdenCompraRecibidaEvent.DetalleEvento(
                    detalle.getPartidaId(),
                    detalle.getDescripcion(),
                    detalle.getCantidad(),
                    detalle.getUnidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()
                ))
                .collect(Collectors.toList());

        String correlationId = UUID.randomUUID().toString();
        OrdenCompraRecibidaEvent event = new OrdenCompraRecibidaEvent(
            this,
            ordenCompra.getId().getValue(),
            ordenCompra.getNumero(),
            ordenCompra.getProyectoId(),
            ordenCompra.getProveedorId().getValue(),
            ordenCompra.getMontoTotal(),
            detallesEvento,
            userId,
            correlationId
        );

        eventPublisher.publishEvent(event);
    }
}
