package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.AsistenciaRepositoryPort;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import com.budgetpro.infrastructure.persistence.mapper.rrhh.AsistenciaMapper;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsistenciaRegistroJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsistenciaRepositoryAdapter implements AsistenciaRepositoryPort {

    private final AsistenciaRegistroJpaRepository repository;
    private final AsistenciaMapper mapper;

    public AsistenciaRepositoryAdapter(AsistenciaRegistroJpaRepository repository, AsistenciaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AsistenciaRegistro save(AsistenciaRegistro asistencia) {
        AsistenciaRegistroEntity entity = mapper.toEntity(asistencia);
        AsistenciaRegistroEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<AsistenciaRegistro> findByEmpleadoAndPeriodo(EmpleadoId empleadoId, LocalDate startDate,
            LocalDate endDate) {
        return repository.findByEmpleadoIdAndFechaBetween(empleadoId.getValue(), startDate, endDate).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaRegistro> findByEmpleadosAndPeriodo(List<EmpleadoId> empleadoIds, LocalDate startDate,
            LocalDate endDate) {
        List<java.util.UUID> uuids = empleadoIds.stream().map(EmpleadoId::getValue).collect(Collectors.toList());
        return repository.findByEmpleadoIdInAndFechaBetween(uuids, startDate, endDate).stream().map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaRegistro> findOverlapping(EmpleadoId empleadoId, ProyectoId proyectoId, LocalDateTime start,
            LocalDateTime end) {
        LocalDate dateStart = start.toLocalDate().minusDays(1);
        LocalDate dateEnd = end.toLocalDate().plusDays(1);

        List<AsistenciaRegistroEntity> candidates = repository.findByEmpleadoIdAndFechaBetween(empleadoId.getValue(),
                dateStart, dateEnd);

        // Misma semántica que el registro persistido: fecha = día de inicio; salida puede ser < entrada (turno nocturno).
        AsistenciaRegistro nuevo = AsistenciaRegistro.registrar(AsistenciaId.random(), empleadoId, proyectoId,
                start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), null);

        return candidates.stream().map(mapper::toDomain).filter(candidato -> candidato.detectOverlap(nuevo))
                .collect(Collectors.toList());
    }

    @Override
    public List<AsistenciaRegistro> findByProyectoAndPeriodo(ProyectoId proyectoId, LocalDate startDate,
            LocalDate endDate) {
        return repository.findByProyectoIdAndFechaBetween(proyectoId.getValue(), startDate, endDate).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }
}
