package com.budgetpro.infrastructure.persistence.mapper.rrhh;

import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.AsistenciaRegistro;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.persistence.entity.rrhh.AsistenciaRegistroEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class AsistenciaMapper {

    public AsistenciaRegistro toDomain(AsistenciaRegistroEntity entity) {
        if (entity == null) {
            return null;
        }
        // Ubicacion is not present in Entity, so passing null.
        return AsistenciaRegistro.registrar(AsistenciaId.of(entity.getId()),
                EmpleadoId.of(entity.getEmpleado().getId()), ProyectoId.from(entity.getProyecto().getId()),
                entity.getFecha(), entity.getHoraEntrada(), entity.getHoraSalida(), null);
    }

    public AsistenciaRegistroEntity toEntity(AsistenciaRegistro domain) {
        if (domain == null) {
            return null;
        }
        AsistenciaRegistroEntity entity = new AsistenciaRegistroEntity();
        entity.setId(domain.getId().getValue());

        EmpleadoEntity empleado = new EmpleadoEntity();
        empleado.setId(domain.getEmpleadoId().getValue());
        entity.setEmpleado(empleado);

        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(domain.getProyectoId().getValue());
        entity.setProyecto(proyecto);

        entity.setFecha(domain.getFecha());
        entity.setHoraEntrada(domain.getHoraEntrada());
        entity.setHoraSalida(domain.getHoraSalida());
        entity.setEstado(domain.getEstado());

        // Calculated fields
        Duration horas = domain.calcularHoras();
        if (horas != null) {
            double hours = horas.toMinutes() / 60.0;
            entity.setHorasTrabajadas(BigDecimal.valueOf(hours));
        }

        Duration extras = domain.calcularHorasExtras();
        if (extras != null) {
            double extraHours = extras.toMinutes() / 60.0;
            entity.setHorasExtras(BigDecimal.valueOf(extraHours));
        }

        return entity;
    }
}
