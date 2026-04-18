package com.budgetpro.domain.finanzas.presupuesto.port.out;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;

import java.util.List;

/**
 * Página de presupuestos (sin dependencias de frameworks).
 */
public record PresupuestoPage(List<Presupuesto> content, long totalElements, int pageNumber, int pageSize) {
}
