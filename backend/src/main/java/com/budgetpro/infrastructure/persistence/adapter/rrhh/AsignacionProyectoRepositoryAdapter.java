package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.domain.catalogo.model.RecursoProxyId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.AsignacionProyectoId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsignacionProyectoJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AsignacionProyectoRepositoryAdapter implements AsignacionProyectoRepositoryPort {

    private final AsignacionProyectoJpaRepository repository;

    public AsignacionProyectoRepositoryAdapter(AsignacionProyectoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsActiveAssignment(EmpleadoId empleadoId) {
        return repository.existsByEmpleado_IdAndFechaFinIsNull(empleadoId.getValue());
    }

    @Override
    public void save(AsignacionProyecto domain) {
        AsignacionProyectoEntity entity = new AsignacionProyectoEntity();
        entity.setId(domain.getId().getValue());

        EmpleadoEntity empleado = new EmpleadoEntity();
        empleado.setId(domain.getEmpleadoId().getValue());
        entity.setEmpleado(empleado);

        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(domain.getProyectoId().getValue());
        entity.setProyecto(proyecto);

        entity.setFechaInicio(domain.getFechaInicio());
        entity.setFechaFin(domain.getFechaFin());
        entity.setRolProyecto(domain.getRolProyecto());

        repository.save(entity);
    }

    @Override
    public boolean existsOverlap(EmpleadoId employeeId, LocalDate start, LocalDate end) {
        return repository.existsOverlap(employeeId.getValue(), start, end);
    }

    @Override
    public boolean existsVigenteAsignacionEmpleadoProyectoEnFecha(EmpleadoId empleadoId, ProyectoId proyectoId,
            LocalDate fecha) {
        return repository.existsVigenteAsignacionEmpleadoProyectoEnFecha(empleadoId.getValue(), proyectoId.getValue(),
                fecha);
    }

    @Override
    public List<AsignacionProyecto> findAsignacionesByEmpleadoId(EmpleadoId empleadoId) {
        return repository.findByEmpleado_IdOrderByFechaInicioAsc(empleadoId.getValue()).stream().map(this::toDomain)
                .toList();
    }

    private AsignacionProyecto toDomain(AsignacionProyectoEntity entity) {
        return AsignacionProyecto.reconstruir(AsignacionProyectoId.of(entity.getId()),
                EmpleadoId.of(entity.getEmpleado().getId()), ProyectoId.from(entity.getProyecto().getId()),
                RecursoProxyId.generate(), entity.getFechaInicio(), entity.getFechaFin(), null, entity.getRolProyecto());
    }
}
