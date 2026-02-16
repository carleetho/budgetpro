package com.budgetpro.application.compra.usecase;

import com.budgetpro.domain.logistica.compra.event.OrdenCompraEnviadaEvent;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.port.in.EnviarOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para enviar una orden de compra al proveedor.
 */
@Service
public class EnviarOrdenCompraUseCaseImpl implements EnviarOrdenCompraUseCase {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EnviarOrdenCompraUseCaseImpl(OrdenCompraRepository ordenCompraRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void enviar(OrdenCompraId ordenCompraId, UUID userId) {
        // 1. Cargar orden de compra
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenCompraId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Orden de compra no encontrada: %s", ordenCompraId)
                ));

        // 2. Enviar orden (APROBADA → ENVIADA)
        LocalDateTime now = LocalDateTime.now();
        ordenCompra.enviar(userId, now);

        // 3. Persistir cambios
        ordenCompraRepository.save(ordenCompra);

        // 4. Publicar evento OrdenCompraEnviadaEvent
        List<OrdenCompraEnviadaEvent.DetalleEvento> detallesEvento = ordenCompra.getDetalles().stream()
                .map(detalle -> new OrdenCompraEnviadaEvent.DetalleEvento(
                    detalle.getPartidaId(),
                    detalle.getDescripcion(),
                    detalle.getCantidad(),
                    detalle.getUnidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()
                ))
                .collect(Collectors.toList());

        String correlationId = UUID.randomUUID().toString();
        OrdenCompraEnviadaEvent event = new OrdenCompraEnviadaEvent(
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
