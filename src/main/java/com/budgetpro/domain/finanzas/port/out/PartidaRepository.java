package com.budgetpro.domain.finanzas.port.out;

import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (Outbound Port) para la persistencia de Partidas.
 * 
 * Define el contrato para guardar y recuperar agregados Partida desde la capa de infraestructura.
 * 
 * Este puerto es parte del dominio y NO debe tener dependencias de frameworks
 * (Spring, JPA, etc). La implementación concreta estará en la capa de infraestructura.
 */
public interface PartidaRepository {

    /**
     * Guarda una Partida (creación o actualización).
     * 
     * Debe persistir la partida y sus cambios, respetando optimistic locking
     * si la partida ya existe.
     * 
     * @param partida La partida a guardar
     * @return La partida guardada (con version actualizada si aplica)
     */
    Partida save(Partida partida);

    /**
     * Busca una Partida por su ID.
     * 
     * @param id El ID de la partida
     * @return Optional con la partida si existe, vacío en caso contrario
     */
    Optional<Partida> findById(PartidaId id);

    /**
     * Busca todas las Partidas de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de partidas del proyecto (vacía si no hay ninguna)
     */
    List<Partida> findByProyectoId(UUID proyectoId);

    /**
     * Busca todas las Partidas de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de partidas del presupuesto (vacía si no hay ninguna)
     */
    List<Partida> findByPresupuestoId(UUID presupuestoId);

    /**
     * Verifica si existe una Partida con un código específico en un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @param codigo El código de la partida
     * @return true si existe, false en caso contrario
     */
    boolean existsByPresupuestoIdAndCodigo(UUID presupuestoId, String codigo);

    /**
     * Elimina una Partida.
     * 
     * @param id El ID de la partida a eliminar
     */
    void deleteById(PartidaId id);
}
