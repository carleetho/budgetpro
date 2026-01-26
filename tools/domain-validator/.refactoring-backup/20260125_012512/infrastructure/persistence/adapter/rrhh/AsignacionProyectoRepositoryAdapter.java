package com.budgetpro.infrastructure.persistence.adapter.rrhh;

import com.budgetpro.application.rrhh.port.out.AsignacionProyectoRepositoryPort;
import com.budgetpro.domain.rrhh.model.AsignacionProyecto;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsignacionProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.rrhh.AsignacionProyectoJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
}
