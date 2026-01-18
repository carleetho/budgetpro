package com.budgetpro.infrastructure.persistence.mapper.almacen;

import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.infrastructure.persistence.entity.almacen.KardexEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper para convertir entre RegistroKardex (dominio) y KardexEntity (persistencia).
 */
@Component
public class KardexMapper {

    /**
     * Convierte un RegistroKardex (dominio) a KardexEntity (persistencia) para CREACIÓN.
     */
    public KardexEntity toEntity(RegistroKardex registro) {
        if (registro == null) {
            return null;
        }

        return new KardexEntity(
            registro.getId(),
            registro.getAlmacenId(),
            registro.getRecursoId(),
            registro.getFechaMovimiento(),
            registro.getMovimientoId(),
            registro.getTipoMovimiento(),
            registro.getCantidadEntrada(),
            registro.getCantidadSalida(),
            registro.getPrecioUnitario(),
            registro.getSaldoCantidad(),
            registro.getSaldoValor(),
            registro.getCostoPromedioPonderado(),
            null // CRÍTICO: null para nuevas entidades, Hibernate lo manejará
        );
    }

    /**
     * Convierte un KardexEntity (persistencia) a RegistroKardex (dominio).
     * 
     * NOTA: El dominio genera un nuevo ID en crear(), pero aquí necesitamos preservar el ID de la BD.
     * Por ahora, usamos crear() que generará un nuevo UUID, pero en producción deberíamos tener
     * un método reconstruir() en el dominio similar a otros agregados.
     */
    public RegistroKardex toDomain(KardexEntity entity) {
        if (entity == null) {
            return null;
        }

        // Crear registro (generará nuevo ID, pero preserva los datos)
        // NOTA: En producción, deberíamos tener un método reconstruir() que acepte el ID
        if (entity.getTipoMovimiento() == com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen.ENTRADA) {
            // Calcular importe total de entrada
            java.math.BigDecimal importeTotal = entity.getCantidadEntrada().multiply(entity.getPrecioUnitario());
            return RegistroKardex.crearEntrada(
                entity.getAlmacenId(),
                entity.getRecursoId(),
                entity.getMovimientoId(),
                entity.getCantidadEntrada(),
                entity.getPrecioUnitario(),
                importeTotal,
                entity.getSaldoCantidad(),
                entity.getSaldoValor(),
                entity.getCostoPromedioPonderado()
            );
        } else {
            // Para salidas, calcular el valor de salida desde el CPP
            java.math.BigDecimal valorSalida = entity.getCantidadSalida().multiply(entity.getCostoPromedioPonderado());
            return RegistroKardex.crearSalida(
                entity.getAlmacenId(),
                entity.getRecursoId(),
                entity.getMovimientoId(),
                entity.getCantidadSalida(),
                valorSalida,
                entity.getSaldoCantidad(),
                entity.getSaldoValor(),
                entity.getCostoPromedioPonderado()
            );
        }
    }
}
