package com.budgetpro.application.rrhh.port.out;

import com.budgetpro.domain.rrhh.model.Cuadrilla;
import com.budgetpro.domain.rrhh.model.CuadrillaId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.rrhh.model.EstadoCuadrilla;

import java.util.List;
import java.util.Optional;

public interface CuadrillaRepositoryPort {
    Cuadrilla save(Cuadrilla cuadrilla);

    Optional<Cuadrilla> findById(CuadrillaId id);

    List<Cuadrilla> findByProyectoAndEstado(ProyectoId proyectoId, EstadoCuadrilla estado);
}
