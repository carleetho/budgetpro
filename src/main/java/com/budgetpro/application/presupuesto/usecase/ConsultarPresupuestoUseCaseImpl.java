package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.presupuesto.port.in.ConsultarPresupuestoUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import com.budgetpro.domain.finanzas.sobrecosto.service.CalculadoraPrecioVentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementación del caso de uso para consultar un presupuesto.
 */
@Service
public class ConsultarPresupuestoUseCaseImpl implements ConsultarPresupuestoUseCase {

    private final PresupuestoRepository presupuestoRepository;
    private final CalculoPresupuestoService calculoPresupuestoService;
    private final CalculadoraPrecioVentaService calculadoraPrecioVentaService;

    public ConsultarPresupuestoUseCaseImpl(PresupuestoRepository presupuestoRepository,
                                           CalculoPresupuestoService calculoPresupuestoService,
                                           CalculadoraPrecioVentaService calculadoraPrecioVentaService) {
        this.presupuestoRepository = presupuestoRepository;
        this.calculoPresupuestoService = calculoPresupuestoService;
        this.calculadoraPrecioVentaService = calculadoraPrecioVentaService;
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoResponse consultar(UUID presupuestoId) {
        // 1. Buscar el presupuesto
        PresupuestoId id = PresupuestoId.from(presupuestoId);
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new PresupuestoNoEncontradoException(presupuestoId));

        // 2. Calcular el costo directo total del presupuesto
        BigDecimal costoDirecto = calculoPresupuestoService.calcularCostoTotal(presupuestoId);

        // 3. Calcular el precio de venta usando la cascada de sobrecosto
        BigDecimal precioVenta;
        try {
            precioVenta = calculadoraPrecioVentaService.calcularPrecioVenta(costoDirecto, presupuestoId);
        } catch (IllegalStateException e) {
            // Si no existe análisis de sobrecosto, el precio de venta = costo directo
            precioVenta = costoDirecto;
        }

        // 4. Retornar respuesta enriquecida con costo directo y precio de venta
        return new PresupuestoResponse(
                presupuesto.getId().getValue(),
                presupuesto.getProyectoId(),
                presupuesto.getNombre(),
                presupuesto.getEstado(),
                presupuesto.getEsContractual(),
                costoDirecto, // Costo Directo (CD)
                precioVenta, // Precio de Venta (PV)
                presupuesto.getVersion().intValue(),
                null, // createdAt se obtiene de la entidad después de persistir
                null  // updatedAt se obtiene de la entidad después de persistir
        );
    }
}
