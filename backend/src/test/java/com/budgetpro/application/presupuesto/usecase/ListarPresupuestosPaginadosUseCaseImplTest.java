package com.budgetpro.application.presupuesto.usecase;

import com.budgetpro.application.presupuesto.exception.ProyectoNoCoincideConTenantException;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoPage;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService;
import com.budgetpro.domain.finanzas.sobrecosto.service.CalculadoraPrecioVentaService;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.domain.shared.TenancyConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarPresupuestosPaginadosUseCaseImplTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private CalculoPresupuestoService calculoPresupuestoService;

    @Mock
    private CalculadoraPrecioVentaService calculadoraPrecioVentaService;

    @InjectMocks
    private ListarPresupuestosPaginadosUseCaseImpl useCase;

    @Test
    void listar_cuandoTenantNoCoincide_lanza() {
        UUID proyectoId = UUID.randomUUID();
        when(proyectoRepository.existsByIdAndTenantId(ProyectoId.from(proyectoId), TenancyConstants.DEFAULT_TENANT_ID))
                .thenReturn(false);

        assertThatThrownBy(() -> useCase.listar(TenancyConstants.DEFAULT_TENANT_ID, proyectoId, 0, 20))
                .isInstanceOf(ProyectoNoCoincideConTenantException.class);
    }

    @Test
    void listar_devuelvePagina() {
        UUID proyectoId = UUID.randomUUID();
        UUID presId = UUID.randomUUID();
        when(proyectoRepository.existsByIdAndTenantId(ProyectoId.from(proyectoId), TenancyConstants.DEFAULT_TENANT_ID))
                .thenReturn(true);
        Presupuesto p = Presupuesto.reconstruir(PresupuestoId.from(presId), proyectoId, "P1", EstadoPresupuesto.BORRADOR,
                false, 0L);
        when(presupuestoRepository.findByProyectoIdAndTenantId(eq(proyectoId), eq(TenancyConstants.DEFAULT_TENANT_ID),
                eq(0), eq(20))).thenReturn(new PresupuestoPage(List.of(p), 1, 0, 20));
        when(calculoPresupuestoService.calcularCostoTotal(presId)).thenReturn(new BigDecimal("100"));
        when(calculadoraPrecioVentaService.calcularPrecioVenta(any(BigDecimal.class), eq(presId)))
                .thenThrow(new IllegalStateException("sin sobrecosto"));

        var out = useCase.listar(TenancyConstants.DEFAULT_TENANT_ID, proyectoId, 0, 20);

        assertThat(out.content()).hasSize(1);
        assertThat(out.totalElements()).isEqualTo(1);
        assertThat(out.content().get(0).costoTotal()).isEqualByComparingTo("100");
        assertThat(out.content().get(0).precioVenta()).isEqualByComparingTo("100");
    }
}
