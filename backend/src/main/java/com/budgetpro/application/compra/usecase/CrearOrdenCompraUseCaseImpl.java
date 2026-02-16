package com.budgetpro.application.compra.usecase;

import com.budgetpro.domain.logistica.compra.model.DetalleOrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompra;
import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.domain.logistica.compra.port.in.CrearOrdenCompraUseCase;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para crear una orden de compra.
 */
@Service
public class CrearOrdenCompraUseCaseImpl implements CrearOrdenCompraUseCase {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;

    public CrearOrdenCompraUseCaseImpl(OrdenCompraRepository ordenCompraRepository,
                                       ProveedorRepository proveedorRepository) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    @Transactional
    public OrdenCompraId crear(CrearOrdenCompraCommand command) {
        // 1. Validar que el proveedor exista y esté ACTIVO
        ProveedorId proveedorId = ProveedorId.from(command.proveedorId());
        var proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Proveedor no encontrado: %s", command.proveedorId())
                ));

        if (proveedor.getEstado() != com.budgetpro.domain.logistica.compra.model.ProveedorEstado.ACTIVO) {
            throw new IllegalStateException(
                String.format("El proveedor %s no está ACTIVO. Estado actual: %s", command.proveedorId(), proveedor.getEstado())
            );
        }

        // 2. Generar número secuencial
        int year = command.fecha().getYear();
        String numero = ordenCompraRepository.generateNextNumero(year);

        // 3. Crear detalles
        List<DetalleOrdenCompra> detalles = command.detalles().stream()
                .map(detalleCommand -> DetalleOrdenCompra.crear(
                    detalleCommand.partidaId(),
                    detalleCommand.descripcion(),
                    detalleCommand.cantidad(),
                    detalleCommand.unidad(),
                    detalleCommand.precioUnitario()
                ))
                .collect(Collectors.toList());

        // 4. Crear orden de compra en estado BORRADOR
        OrdenCompraId ordenId = OrdenCompraId.nuevo();
        LocalDateTime now = LocalDateTime.now();
        OrdenCompra ordenCompra = OrdenCompra.crear(
            ordenId,
            numero,
            command.proyectoId(),
            proveedorId,
            command.fecha(),
            command.condicionesPago(),
            command.observaciones(),
            detalles,
            command.createdBy(),
            now
        );

        // 5. Persistir
        ordenCompraRepository.save(ordenCompra);

        return ordenId;
    }
}
