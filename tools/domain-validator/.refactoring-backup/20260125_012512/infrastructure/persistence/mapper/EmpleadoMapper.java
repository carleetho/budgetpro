package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.rrhh.model.*;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import com.budgetpro.infrastructure.persistence.entity.rrhh.HistorialLaboralEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmpleadoMapper {

    public Empleado toDomain(EmpleadoEntity entity) {
        if (entity == null) {
            return null;
        }

        List<HistorialLaboral> historial = entity.getHistorialLaboral().stream().map(this::toDomainHistorial)
                .collect(Collectors.toList());

        Contacto contacto = Contacto.of(entity.getEmail(), entity.getTelefono(), null);

        return Empleado.reconstruir(EmpleadoId.of(entity.getId()), entity.getNombre(), entity.getApellido(),
                entity.getNumeroIdentificacion(), contacto, entity.getEstado(), entity.getAtributos(), historial);
    }

    public EmpleadoEntity toEntity(Empleado domain) {
        if (domain == null) {
            return null;
        }

        EmpleadoEntity entity = new EmpleadoEntity();
        entity.setId(domain.getId().getValue());
        entity.setNombre(domain.getNombre());
        entity.setApellido(domain.getApellido());
        entity.setNumeroIdentificacion(domain.getNumeroIdentificacion());
        if (domain.getContacto() != null) {
            entity.setEmail(domain.getContacto().getEmail());
            entity.setTelefono(domain.getContacto().getTelefono());
        }
        entity.setEstado(domain.getEstado());
        entity.setAtributos(domain.getAtributos()); // Map<String, Object> matches

        // Map history
        if (domain.getHistorial() != null) {
            List<HistorialLaboralEntity> historyEntities = domain.getHistorial().stream()
                    .map(h -> toEntityHistorial(h, entity)).collect(Collectors.toList());
            entity.setHistorialLaboral(historyEntities);
        }

        return entity;
    }

    private HistorialLaboral toDomainHistorial(HistorialLaboralEntity entity) {
        return HistorialLaboral.reconstruir(HistorialId.of(entity.getId()), entity.getCargo(), entity.getSalarioBase(),
                entity.getTipoEmpleado(), entity.getFechaInicio(), entity.getFechaFin());
    }

    private HistorialLaboralEntity toEntityHistorial(HistorialLaboral domain, EmpleadoEntity parent) {
        HistorialLaboralEntity entity = new HistorialLaboralEntity();
        entity.setId(domain.getId().getValue());
        entity.setEmpleado(parent);
        entity.setCargo(domain.getCargo());
        entity.setSalarioBase(domain.getSalarioBase());
        entity.setTipoEmpleado(domain.getTipoEmpleado());
        entity.setFechaInicio(domain.getFechaInicio());
        entity.setFechaFin(domain.getFechaFin());
        entity.setUnidadSalario(domain.getUnidadSalario());
        // Motivo cambio not directly in domain core constructor/getter exposed easily?
        // Domain model doesn't expose 'motivoCambio' getter in HistorialLaboral!
        // It keeps it implicitly or not at all. Assuming null for now.

        return entity;
    }
}
