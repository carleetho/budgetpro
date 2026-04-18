package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.dto.ListarPresupuestosPaginadosResponse;
import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;
import com.budgetpro.application.presupuesto.exception.ProyectoNoCoincideConTenantException;
import com.budgetpro.application.presupuesto.port.in.ListarPresupuestosPaginadosUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoPage;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import com.budgetpro.domain.finanzas.sobrecosto.service.CalculadoraPrecioVentaService;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ListarPresupuestosPaginadosUseCaseImpl implements ListarPresupuestosPaginadosUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProyectoRepository proyectoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final CalculoPresupuestoService calculoPresupuestoService;
    private final CalculadoraPrecioVentaService calculadoraPrecioVentaService;

    public ListarPresupuestosPaginadosUseCaseImpl(ProyectoRepository proyectoRepository,
            PresupuestoRepository presupuestoRepository,
            CalculoPresupuestoService calculoPresupuestoService,
            CalculadoraPrecioVentaService calculadoraPrecioVentaService) {
        this.proyectoRepository = proyectoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.calculoPresupuestoService = calculoPresupuestoService;
        this.calculadoraPrecioVentaService = calculadoraPrecioVentaService;
    }

    @Override
    @Transactional(readOnly = true)
    public ListarPresupuestosPaginadosResponse listar(UUID tenantId, UUID proyectoId, int page, int size) {
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (!proyectoRepository.existsByIdAndTenantId(ProyectoId.from(proyectoId), tenantId)) {
            throw new ProyectoNoCoincideConTenantException(proyectoId, tenantId);
        }

        PresupuestoPage slice = presupuestoRepository.findByProyectoIdAndTenantId(proyectoId, tenantId, page, size);
        List<PresupuestoResponse> rows = new ArrayList<>();
        for (Presupuesto presupuesto : slice.content()) {
            UUID pid = presupuesto.getId().getValue();
            BigDecimal costoDirecto = calculoPresupuestoService.calcularCostoTotal(pid);
            BigDecimal precioVenta;
            try {
                precioVenta = calculadoraPrecioVentaService.calcularPrecioVenta(costoDirecto, pid);
            } catch (IllegalStateException e) {
                precioVenta = costoDirecto;
            }
            rows.add(new PresupuestoResponse(
                    pid,
                    presupuesto.getProyectoId(),
                    presupuesto.getNombre(),
                    presupuesto.getEstado(),
                    presupuesto.getEsContractual(),
                    costoDirecto,
                    precioVenta,
                    presupuesto.getVersion().intValue(),
                    null,
                    null));
        }

        int totalPages = size == 0 ? 0 : (int) Math.ceil(slice.totalElements() / (double) size);
        return new ListarPresupuestosPaginadosResponse(rows, slice.totalElements(), totalPages, page, size);
    }
}
