package com.budgetpro.infrastructure.persistence.repository.rrhh;

import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.infrastructure.persistence.entity.rrhh.EmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpleadoJpaRepository extends JpaRepository<EmpleadoEntity, UUID> {

    Optional<EmpleadoEntity> findByNumeroIdentificacion(String numeroIdentificacion);

    List<EmpleadoEntity> findByEstado(EstadoEmpleado estado);

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);

    @Query("SELECT e FROM EmpleadoEntity e JOIN e.historialLaboral h " + "WHERE e.estado = :estado "
            + "AND h.fechaFin IS NULL " + "AND LOWER(h.cargo) LIKE LOWER(CONCAT('%', :puesto, '%'))")
    List<EmpleadoEntity> findByEstadoAndPuestoContaining(@Param("estado") EstadoEmpleado estado,
            @Param("puesto") String puesto);
}
