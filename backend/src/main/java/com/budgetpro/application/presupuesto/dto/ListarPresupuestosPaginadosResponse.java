package com.budgetpro.application.presupuesto.dto;

import java.util.List;

public record ListarPresupuestosPaginadosResponse(
        List<PresupuestoResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size) {
}
