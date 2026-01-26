package com.budgetpro.domain.logistica.almacen.service;

import com.budgetpro.domain.logistica.almacen.model.RegistroKardex;
import com.budgetpro.domain.logistica.almacen.model.TipoMovimientoAlmacen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Servicio de Dominio para gestionar Kárdex y calcular costo promedio ponderado (CPP).
 * 
 * Implementa la metodología de Suárez Salazar - Control de consumo físico vs teórico.
 * 
 * Lógica de Entrada:
 * - Aumenta stock: saldo_cantidad = saldo_anterior + cantidad_entrada
 * - Calcula nuevo valor: saldo_valor = saldo_valor_anterior + importe_total
 * - Calcula nuevo CPP: cpp = saldo_valor / saldo_cantidad
 * 
 * Lógica de Salida:
 * - Disminuye stock: saldo_cantidad = saldo_anterior - cantidad_salida
 * - Calcula valor salida: valor_salida = cantidad_salida × CPP_actual
 * - Actualiza saldo valor: saldo_valor = saldo_valor_anterior - valor_salida
 * - CPP se mantiene igual (solo cambia con entradas)
 * 
 * No persiste, solo calcula.
 */
public class GestionKardexService {

    /**
     * Procesa una entrada de material y calcula el nuevo registro de Kárdex.
     * 
     * @param almacenId ID del almacén
     * @param recursoId ID del recurso
     * @param cantidad Cantidad de entrada
     * @param precioUnitario Precio unitario del movimiento
     * @param movimientoId ID del movimiento asociado
     * @param saldoCantidadAnterior Saldo de cantidad anterior
     * @param saldoValorAnterior Saldo de valor anterior
     * @return Nuevo registro de Kárdex
     */
    public RegistroKardex procesarEntrada(
            UUID almacenId,
            UUID recursoId,
            BigDecimal cantidad,
            BigDecimal precioUnitario,
            UUID movimientoId,
            BigDecimal saldoCantidadAnterior,
            BigDecimal saldoValorAnterior) {
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de entrada debe ser mayor a cero");
        }
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor o igual a cero");
        }
        if (saldoCantidadAnterior == null) {
            saldoCantidadAnterior = BigDecimal.ZERO;
        }
        if (saldoValorAnterior == null) {
            saldoValorAnterior = BigDecimal.ZERO;
        }

        // Calcular nuevos valores
        BigDecimal nuevoSaldoCantidad = saldoCantidadAnterior.add(cantidad);
        BigDecimal importeTotal = cantidad.multiply(precioUnitario);
        BigDecimal nuevoSaldoValor = saldoValorAnterior.add(importeTotal);
        BigDecimal nuevoCPP = nuevoSaldoValor.divide(nuevoSaldoCantidad, 4, RoundingMode.HALF_UP);

        return RegistroKardex.crearEntrada(
            almacenId,
            recursoId,
            movimientoId,
            cantidad,
            precioUnitario,
            importeTotal,
            nuevoSaldoCantidad,
            nuevoSaldoValor,
            nuevoCPP
        );
    }

    /**
     * Procesa una salida de material y calcula el nuevo registro de Kárdex.
     * 
     * @param almacenId ID del almacén
     * @param recursoId ID del recurso
     * @param cantidad Cantidad de salida
     * @param movimientoId ID del movimiento asociado
     * @param saldoCantidadAnterior Saldo de cantidad anterior
     * @param saldoValorAnterior Saldo de valor anterior
     * @param costoPromedioPonderado Costo promedio ponderado actual
     * @return Nuevo registro de Kárdex
     */
    public RegistroKardex procesarSalida(
            UUID almacenId,
            UUID recursoId,
            BigDecimal cantidad,
            UUID movimientoId,
            BigDecimal saldoCantidadAnterior,
            BigDecimal saldoValorAnterior,
            BigDecimal costoPromedioPonderado) {
        
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad de salida debe ser mayor a cero");
        }
        if (saldoCantidadAnterior == null || saldoCantidadAnterior.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo de cantidad anterior debe ser mayor o igual a cero");
        }
        if (costoPromedioPonderado == null || costoPromedioPonderado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo promedio ponderado debe ser mayor o igual a cero");
        }

        // Validar stock suficiente
        if (saldoCantidadAnterior.compareTo(cantidad) < 0) {
            throw new IllegalStateException(
                String.format("Stock insuficiente. Disponible: %s, Solicitado: %s", 
                    saldoCantidadAnterior, cantidad));
        }

        // Calcular valores
        BigDecimal nuevoSaldoCantidad = saldoCantidadAnterior.subtract(cantidad);
        BigDecimal valorSalida = cantidad.multiply(costoPromedioPonderado);
        BigDecimal nuevoSaldoValor = saldoValorAnterior.subtract(valorSalida);
        BigDecimal cppActual = costoPromedioPonderado; // Se mantiene igual

        return RegistroKardex.crearSalida(
            almacenId,
            recursoId,
            movimientoId,
            cantidad,
            valorSalida,
            nuevoSaldoCantidad,
            nuevoSaldoValor,
            cppActual
        );
    }
}
