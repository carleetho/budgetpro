package com.budgetpro.domain.finanzas.reajuste.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio de Dominio para calcular reajustes de costos mediante fórmula polinómica.
 * 
 * Implementa la metodología de Suárez Salazar (Cap. 3.000 - Reajuste de Costos).
 * 
 * Fórmula polinómica genérica: Pr = Po × (I1 / Io)
 * 
 * Donde:
 * - Pr: Precio reajustado
 * - Po: Precio original (base de licitación)
 * - I1: Índice actual
 * - Io: Índice base de licitación
 * 
 * No persiste, solo calcula.
 */
public class CalculadorReajusteService {

    /**
     * Calcula el precio reajustado usando la fórmula polinómica.
     * 
     * Fórmula: Pr = Po × (I1 / Io)
     * 
     * @param precioOriginal Precio original (Po)
     * @param indiceBase Índice base de licitación (Io)
     * @param indiceActual Índice actual (I1)
     * @return Precio reajustado (Pr)
     */
    public BigDecimal calcularPrecioReajustado(BigDecimal precioOriginal, BigDecimal indiceBase, BigDecimal indiceActual) {
        if (precioOriginal == null || precioOriginal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio original debe ser mayor o igual a cero");
        }
        if (indiceBase == null || indiceBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El índice base debe ser mayor a cero");
        }
        if (indiceActual == null || indiceActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El índice actual debe ser mayor a cero");
        }

        return precioOriginal
                .multiply(indiceActual)
                .divide(indiceBase, 4, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el diferencial a cobrar.
     * 
     * Diferencial = Pr - Po
     * 
     * @param precioReajustado Precio reajustado (Pr)
     * @param precioOriginal Precio original (Po)
     * @return Diferencial a cobrar
     */
    public BigDecimal calcularDiferencial(BigDecimal precioReajustado, BigDecimal precioOriginal) {
        if (precioReajustado == null || precioOriginal == null) {
            throw new IllegalArgumentException("Los precios no pueden ser nulos");
        }

        return precioReajustado
                .subtract(precioOriginal)
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el porcentaje de variación.
     * 
     * % Variación = ((I1 / Io) - 1) × 100
     * 
     * @param indiceBase Índice base (Io)
     * @param indiceActual Índice actual (I1)
     * @return Porcentaje de variación
     */
    public BigDecimal calcularPorcentajeVariacion(BigDecimal indiceBase, BigDecimal indiceActual) {
        if (indiceBase == null || indiceBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El índice base debe ser mayor a cero");
        }
        if (indiceActual == null || indiceActual.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El índice actual debe ser mayor a cero");
        }

        BigDecimal factor = indiceActual.divide(indiceBase, 4, RoundingMode.HALF_UP);
        return factor.subtract(BigDecimal.ONE)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
