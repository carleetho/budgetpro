package com.budgetpro.domain.finanzas.estimacion.service;

import com.budgetpro.domain.finanzas.estimacion.exception.PeriodoSolapadoException;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class PeriodoValidatorService {

    private final EstimacionRepository estimacionRepository;

    public PeriodoValidatorService(EstimacionRepository estimacionRepository) {
        this.estimacionRepository = Objects.requireNonNull(estimacionRepository,
                "El repositorio de estimaciones no puede ser nulo");
    }

    public void validatePeriodo(LocalDate inicio, LocalDate fin) {
        Objects.requireNonNull(inicio, "La fecha de inicio no puede ser nula");
        Objects.requireNonNull(fin, "La fecha de fin no puede ser nula");

        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    public void validateNoOverlap(UUID proyectoId, LocalDate inicio, LocalDate fin) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        validatePeriodo(inicio, fin);

        if (estimacionRepository.existsPeriodoSolapado(proyectoId, inicio, fin)) {
            throw new PeriodoSolapadoException(proyectoId, inicio, fin);
        }
    }

    public void validateNoOverlapExcludingEstimacion(UUID proyectoId, LocalDate inicio, LocalDate fin,
            EstimacionId excludeId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(excludeId, "El ID a excluir no puede ser nulo");
        validatePeriodo(inicio, fin);

        if (estimacionRepository.existsPeriodoSolapadoExcludingId(proyectoId, inicio, fin, excludeId)) {
            throw new PeriodoSolapadoException(proyectoId, inicio, fin);
        }
    }
}
