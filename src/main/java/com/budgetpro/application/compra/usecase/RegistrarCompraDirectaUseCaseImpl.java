package com.budgetpro.application.compra.usecase;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse;
import com.budgetpro.application.compra.port.in.RegistrarCompraDirectaUseCase;
import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse.StockInfo;
import com.budgetpro.domain.finanzas.compra.port.out.CompraRepository;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.finanzas.compra.port.out.OutboxEventRepository;
import com.budgetpro.domain.finanzas.compra.event.CompraRegistradaEvent;
import com.budgetpro.domain.finanzas.compra.Cantidad;
import com.budgetpro.domain.finanzas.compra.Compra;
import com.budgetpro.domain.finanzas.compra.DetalleCompra;
import com.budgetpro.domain.finanzas.compra.PrecioUnitario;
import com.budgetpro.domain.finanzas.compra.service.ProcesarCompraDirectaService;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.recurso.model.Recurso;
import com.budgetpro.domain.recurso.model.RecursoId;
import com.budgetpro.domain.recurso.port.out.RecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final RecursoRepository recursoRepository;
    private final ProcesarCompraDirectaService procesarCompraDirectaService;
    private final OutboxEventRepository outboxEventRepository;

    public RegistrarCompraDirectaUseCaseImpl(
            CompraRepository compraRepository,
            BilleteraRepository billeteraRepository,
            InventarioRepository inventarioRepository,
            RecursoRepository recursoRepository,
            ProcesarCompraDirectaService procesarCompraDirectaService,
            OutboxEventRepository outboxEventRepository) {
        this.compraRepository = compraRepository;
        this.billeteraRepository = billeteraRepository;
        this.inventarioRepository = inventarioRepository;
        this.recursoRepository = recursoRepository;
        this.procesarCompraDirectaService = procesarCompraDirectaService;
        this.outboxEventRepository = outboxEventRepository;
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

        // 2. Crear el agregado Compra (estado inicial: PENDIENTE)
        Compra compra = Compra.crear(command.proyectoId(), command.presupuestoId(), detalles);

        try {
            // 4. Cargar la billetera del proyecto
            Billetera billetera = billeteraRepository.findByProyectoId(command.proyectoId())
                    .orElseThrow(() -> new IllegalStateException(
                        String.format("No existe una billetera para el proyecto %s", command.proyectoId())
                    ));

            // 5. Cargar inventarios por recurso del proyecto específico
            List<RecursoId> recursoIds = detalles.stream()
                    .map(DetalleCompra::getRecursoId)
                    .collect(Collectors.toList());
            // Usar método con proyectoId para búsqueda precisa (evita ambigüedades multi-proyecto)
            Map<RecursoId, InventarioItem> inventariosPorRecurso = 
                inventarioRepository.findAllByProyectoIdAndRecursoIds(command.proyectoId(), recursoIds);

            // Validar que existan inventarios para todos los recursos
            for (DetalleCompra detalle : detalles) {
                if (!inventariosPorRecurso.containsKey(detalle.getRecursoId())) {
                    throw new IllegalStateException(
                        String.format("No existe inventario para el recurso %s", detalle.getRecursoId())
                    );
                }
            }

            // Guardar stock anterior antes de procesar (para respuesta)
            Map<RecursoId, BigDecimal> stockAnteriorPorRecurso = inventariosPorRecurso.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> entry.getValue().getStock()
                    ));

            // Guardar saldo anterior antes de procesar (para respuesta)
            BigDecimal saldoAnterior = billetera.getSaldoActual();

            // 6. Invocar Domain Service para procesar la compra
            procesarCompraDirectaService.procesar(compra, billetera, inventariosPorRecurso);

            // 7. Confirmar la compra (PENDIENTE -> CONFIRMADA)
            compra.confirmar();

            // 8. Persistir agregados modificados
            // Guardar compra (con estado CONFIRMADA)
            compraRepository.save(compra);
            billeteraRepository.save(billetera);
            // Guardar inventarios con proyectoId explícito (permite crear nuevos si es necesario)
            for (InventarioItem inventario : inventariosPorRecurso.values()) {
                inventarioRepository.save(inventario, command.proyectoId());
            }

            // 9. Persistir evento en Outbox (dentro de la misma transacción)
            CompraRegistradaEvent evento = new CompraRegistradaEvent(
                compra.getId().getValue(),
                compra.getProyectoId(),
                compra.getPresupuestoId(),
                compra.getTotal().getValue().getValue(), // TotalCompra -> Monto -> BigDecimal
                LocalDateTime.now()
            );
            outboxEventRepository.save(evento);

            // 10. Construir información de stock actualizado para la respuesta
            List<StockInfo> stockActualizado = detalles.stream()
                    .map(detalle -> {
                        RecursoId recursoId = detalle.getRecursoId();
                        InventarioItem inventario = inventariosPorRecurso.get(recursoId);
                        BigDecimal stockAnterior = stockAnteriorPorRecurso.get(recursoId);
                        BigDecimal stockActual = inventario.getStock();
                        
                        // Obtener información del recurso (nombre y unidad)
                        Recurso recurso = recursoRepository.findById(recursoId)
                                .orElseThrow(() -> new IllegalStateException(
                                    String.format("No se encontró el recurso %s", recursoId)
                                ));
                        
                        return new StockInfo(
                                recursoId.getValue(),
                                recurso.getNombre(),
                                stockAnterior,
                                stockActual,
                                recurso.getUnidadBase()
                        );
                    })
                    .collect(Collectors.toList());

            // 11. Retornar respuesta con estado CONFIRMADA, saldo actual y stock actualizado
            return new RegistrarCompraDirectaResponse(
                compra.getId().getValue(),
                compra.getEstado(),
                null, // Sin mensaje de error
                billetera.getSaldoActual(), // Saldo actualizado después de la compra
                stockActualizado
            );
        } catch (Exception e) {
            // 10. Marcar compra como ERROR solo si aún está en estado PENDIENTE
            // Si ya se confirmó, no intentar cambiar el estado (evita IllegalStateException)
            if (compra.getEstado() == com.budgetpro.domain.finanzas.compra.EstadoCompra.PENDIENTE) {
                compra.marcarError();
                compraRepository.save(compra);
            }

            // Re-lanzar la excepción para que el ControllerAdvice la maneje
            throw e;
        }
    }
}
