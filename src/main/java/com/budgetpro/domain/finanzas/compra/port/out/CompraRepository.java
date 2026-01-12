package com.budgetpro.domain.finanzas.compra.port.out;

import com.budgetpro.domain.finanzas.compra.Compra;
import com.budgetpro.domain.finanzas.compra.CompraId;

import java.util.Optional;

/**
 * Puerto de Salida (Outbound Port) para la persistencia del agregado Compra.
 * 
 * Esta interfaz define el contrato que debe implementar la capa de infraestructura.
 * 
 * Según Directiva Maestra v2.0: Los puertos de salida deben residir en domain/model/{agregado}/port/out
 */
public interface CompraRepository {

    /**
     * Guarda o actualiza una compra en el repositorio.
     * 
     * @param compra La compra a persistir (no puede ser nula)
     */
    void save(Compra compra);

    /**
     * Busca una compra por su identificador único.
     * 
     * @param id El identificador de la compra (no puede ser nulo)
     * @return Un Optional que contiene la compra si existe, o vacío si no se encuentra
     */
    Optional<Compra> findById(CompraId id);
}
