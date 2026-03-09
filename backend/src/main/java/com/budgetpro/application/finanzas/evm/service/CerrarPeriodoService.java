package com.budgetpro.application.finanzas.evm.service;

import com.budgetpro.application.finanzas.evm.event.ValuacionCerradaEvent;
import com.budgetpro.application.finanzas.evm.exception.PeriodoFechaInvalidaException;
import com.budgetpro.application.finanzas.evm.port.in.CerrarPeriodoUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación del caso de uso para cerrar un período de valuación (REQ-64, Invariante E-04).
 */
@Service
public class CerrarPeriodoService implements CerrarPeriodoUseCase {

    private static final String PERIODO_PREFIX = "PER-";

    private final ProyectoRepository proyectoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CerrarPeriodoService(
            ProyectoRepository proyectoRepository,
            ApplicationEventPublisher eventPublisher) {
        this.proyectoRepository = proyectoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public String cerrar(UUID proyectoId, LocalDate fechaCorte) {
        Proyecto proyecto = proyectoRepository.findById(ProyectoId.from(proyectoId))
                .orElseThrow(() -> new ProyectoNotFoundException(proyectoId));

        if (!proyecto.esFechaCorteValida(fechaCorte)) {
            FrecuenciaControl freq = proyecto.getFrecuenciaControl();
            throw new PeriodoFechaInvalidaException(fechaCorte, freq != null ? freq : FrecuenciaControl.SEMANAL);
        }

        String periodoId = PERIODO_PREFIX + fechaCorte;
        eventPublisher.publishEvent(new ValuacionCerradaEvent(proyectoId, periodoId, fechaCorte));
        return periodoId;
    }
}
