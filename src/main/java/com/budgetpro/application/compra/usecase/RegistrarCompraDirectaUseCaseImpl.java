package com.budgetpro.application.compra.usecase;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse;
import com.budgetpro.application.compra.port.in.RegistrarCompraDirectaUseCase;
import com.budgetpro.application.compra.port.out.CompraRepository;
import com.budgetpro.application.compra.port.out.InventarioRepository;
import com.budgetpro.domain.finanzas.compra.Cantidad;
import com.budgetpro.domain.finanzas.compra.Compra;
import com.budgetpro.domain.finanzas.compra.DetalleCompra;
import com.budgetpro.domain.finanzas.compra.PrecioUnitario;
import com.budgetpro.domain.finanzas.compra.service.ProcesarCompraDirectaService;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.recurso.model.RecursoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para registrar una compra directa.
 * 
 * Responsabilidades:
 * - Orquestar el flujo de registro de compra directa
 * - Coordinar repositorios (Compra, Billetera, Inventario)
 * - Gestionar transacciones
 * - Invocar Domain Service (ProcesarCompraDirectaService)
 * 
 * NO contiene lógica de negocio profunda (eso está en el Domain Service y los agregados).
 */
@Service
@Validated
@Transactional
public class RegistrarCompraDirectaUseCaseImpl implements RegistrarCompraDirectaUseCase {

    private final CompraRepository compraRepository;
    private final BilleteraRepository billeteraRepository;
    private final InventarioRepository inventarioRepository;
    private final ProcesarCompraDirectaService procesarCompraDirectaService;

    public RegistrarCompraDirectaUseCaseImpl(
            CompraRepository compraRepository,
            BilleteraRepository billeteraRepository,
            InventarioRepository inventarioRepository,
            ProcesarCompraDirectaService procesarCompraDirectaService) {
        this.compraRepository = compraRepository;
        this.billeteraRepository = billeteraRepository;
        this.inventarioRepository = inventarioRepository;
        this.procesarCompraDirectaService = procesarCompraDirectaService;
    }

    @Override
    public RegistrarCompraDirectaResponse ejecutar(RegistrarCompraDirectaCommand command) {
        // 1. Convertir detalles del comando a objetos de dominio
        List<DetalleCompra> detalles = command.detalles().stream()
                .map(detalleCmd -> {
                    RecursoId recursoId = RecursoId.of(detalleCmd.recursoId());
                    Cantidad cantidad = Cantidad.of(detalleCmd.cantidad());
                    PrecioUnitario precioUnitario = PrecioUnitario.of(detalleCmd.precioUnitario());
                    return DetalleCompra.crear(recursoId, cantidad, precioUnitario);
                })
                .collect(Collectors.toList());

        // 2. Crear el agregado Compra
        Compra compra = Compra.crear(command.proyectoId(), command.presupuestoId(), detalles);

        // 3. Cargar la billetera del proyecto
        Billetera billetera = billeteraRepository.findByProyectoId(command.proyectoId())
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No existe una billetera para el proyecto %s", command.proyectoId())
                ));

        // 4. Cargar inventarios por recurso
        List<RecursoId> recursoIds = detalles.stream()
                .map(DetalleCompra::getRecursoId)
                .collect(Collectors.toList());
        Map<RecursoId, InventarioItem> inventariosPorRecurso = inventarioRepository.findAllByRecursoIds(recursoIds);

        // Validar que existan inventarios para todos los recursos
        for (DetalleCompra detalle : detalles) {
            if (!inventariosPorRecurso.containsKey(detalle.getRecursoId())) {
                throw new IllegalStateException(
                    String.format("No existe inventario para el recurso %s", detalle.getRecursoId())
                );
            }
        }

        // 5. Invocar Domain Service para procesar la compra
        procesarCompraDirectaService.procesar(compra, billetera, inventariosPorRecurso);

        // 6. Persistir agregados modificados
        compraRepository.save(compra);
        billeteraRepository.save(billetera);
        for (InventarioItem inventario : inventariosPorRecurso.values()) {
            inventarioRepository.save(inventario);
        }

        // 7. Retornar respuesta
        return new RegistrarCompraDirectaResponse(compra.getId().getValue());
    }
}
