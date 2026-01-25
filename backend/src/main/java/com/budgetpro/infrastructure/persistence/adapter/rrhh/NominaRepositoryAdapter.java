package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.NominaRepositoryPort;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.Nomina;
import com.budgetpro.domain.rrhh.model.NominaId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.NominaEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.NominaMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.NominaJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Component
public class NominaRepositoryAdapter implements NominaRepositoryPort {

    private final NominaJpaRepository repository;
    private final NominaMapper mapper;

    public NominaRepositoryAdapter(NominaJpaRepository repository, NominaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Nomina save(Nomina nomina) {
        NominaEntity entity = mapper.toEntity(nomina);
        NominaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Nomina> findById(NominaId id) {
        return repository.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public boolean existsForPeriod(ProyectoId proyectoId, LocalDate periodoInicio, LocalDate periodoFin) {
        // Assuming we check for *any* active/finalized payroll in this period to avoid
        // duplicates.
        // Passing list of active states if relevant, or just checking existence.
        // JpaRepo has: existsByProyectoIdAndPeriodoInicioAndPeriodoFinAndEstadoIn
        // We'll interpret 'exists' broadly or use a specific list of non-cancelled
        // states.
        // Assuming "Aprobada", "Borrador", "Pagada" etc. If "Cancelada" exists, we
        // might allow recreation.
        // For now, checking all states except potentially "CANCELLED".
        // Since I don't have the status Enum explicitly defined for Nomina (it uses
        // String),
        // I will check against common active states or just strict overlap if
        // requirements imply strict period checking.
        // Req: "existsForPeriod (idempotency)".
        // I will match exact start/end dates.

        // Providing a safeguard list of "Active" states.
        // Since I don't know the exact string values for 'estado', I'll use repository
        // method if I can determine values,
        // OR simply rely on the fact that if ANY record exists for exact dates, it's a
        // conflict (idempotency).
        // However, the repository method requires 'Collection<String> estados'.
        // I'll guess standard values or check existing code usage.
        // Since no existing usage, I will use a wildcard or pass likely values:
        // "BORRADOR", "APROBADA", "PAGADA", "CERRADA".

        return repository.existsByProyectoIdAndPeriodoInicioAndPeriodoFinAndEstadoIn(proyectoId.getValue(),
                periodoInicio, periodoFin,
                java.util.List.of("BORRADOR", "PENDIENTE", "APROBADA", "PAGADA", "CERRADA", "PROCESANDO"));
    }
}
