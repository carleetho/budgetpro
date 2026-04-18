package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.ProveedorEstado;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorValidator;
import com.budgetpro.infrastructure.persistence.repository.compra.ProveedorJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto {@link ProveedorValidator} (L-04: solo proveedores ACTIVO en compras).
 */
@Component
public class ProveedorValidatorAdapter implements ProveedorValidator {

    private final ProveedorJpaRepository jpaRepository;

    public ProveedorValidatorAdapter(ProveedorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean esProveedorActivo(ProveedorId proveedorId) {
        var entity = jpaRepository.findById(proveedorId.getValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Proveedor no encontrado: " + proveedorId.getValue()));
        return entity.getEstado() == ProveedorEstado.ACTIVO;
    }
}
