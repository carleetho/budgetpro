package com.budgetpro.infrastructure.persistence.repository.compra;

import com.budgetpro.domain.logistica.compra.model.ProveedorEstado;
import com.budgetpro.infrastructure.persistence.entity.compra.ProveedorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para ProveedorEntity.
 */
@Repository
public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, UUID> {

    /**
     * Verifica si existe un proveedor con el RUC dado.
     * 
     * @param ruc El RUC del proveedor
     * @return true si existe, false en caso contrario
     */
    boolean existsByRuc(String ruc);

    /**
     * Busca un proveedor por su RUC.
     * 
     * @param ruc El RUC del proveedor
     * @return Optional con el proveedor si existe, vacío en caso contrario
     */
    Optional<ProveedorEntity> findByRuc(String ruc);

    /**
     * Busca todos los proveedores con un estado específico.
     * 
     * @param estado El estado del proveedor
     * @return Lista de proveedores con el estado especificado
     */
    List<ProveedorEntity> findByEstado(ProveedorEstado estado);

    /**
     * Cuenta cuántas compras referencian a un proveedor específico por su razón social.
     * 
     * NOTA: Actualmente CompraEntity tiene un campo String "proveedor" que almacena el nombre.
     * En el futuro, cuando se migre a usar ProveedorId, esta consulta deberá actualizarse.
     * 
     * @param razonSocial La razón social del proveedor
     * @return Número de compras que referencian al proveedor
     */
    @Query("SELECT COUNT(c) FROM com.budgetpro.infrastructure.persistence.entity.compra.CompraEntity c WHERE c.proveedor = :razonSocial")
    long countReferenciasEnCompras(@Param("razonSocial") String razonSocial);
}
