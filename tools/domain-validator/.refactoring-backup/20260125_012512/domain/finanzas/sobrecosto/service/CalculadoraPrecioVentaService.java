package com.budgetpro.domain.finanzas.sobrecosto.service;

import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.AnalisisSobrecostoRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Servicio de Dominio para calcular el Precio de Venta usando la cascada de cargos.
 * 
 * Implementa la metodología de Suárez Salazar (Pág. 54) de forma estricta.
 * 
 * Cálculo en Cascada (NO suma lineal):
 * 1. CostoDirecto (CD) - Base
 * 2. Subtotal1 = CD + Indirectos (% sobre CD)
 * 3. Subtotal2 = Subtotal1 + Financiamiento (% sobre Subtotal1)
 * 4. Subtotal3 = Subtotal2 + Utilidad (% sobre Subtotal2)
 * 5. PrecioVenta = Subtotal3 + CargosAdicionales (% sobre Subtotal3)
 * 
 * No persiste, solo calcula.
 */
public class CalculadoraPrecioVentaService {

    private final AnalisisSobrecostoRepository analisisSobrecostoRepository;

    public CalculadoraPrecioVentaService(AnalisisSobrecostoRepository analisisSobrecostoRepository) {
        this.analisisSobrecostoRepository = analisisSobrecostoRepository;
    }

    /**
     * Calcula el Precio de Venta a partir del Costo Directo usando la cascada de cargos.
     * 
     * @param costoDirecto El costo directo (base)
     * @param presupuestoId El ID del presupuesto (para obtener AnalisisSobrecosto)
     * @return El precio de venta calculado
     */
    public BigDecimal calcularPrecioVenta(BigDecimal costoDirecto, UUID presupuestoId) {
        if (costoDirecto == null || costoDirecto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo directo no puede ser negativo");
        }

        // Buscar análisis de sobrecosto del presupuesto
        AnalisisSobrecosto analisis = analisisSobrecostoRepository.findByPresupuestoId(presupuestoId)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No existe análisis de sobrecosto para el presupuesto %s. " +
                                     "Debe configurarse antes de calcular precio de venta.", presupuestoId)));

        return calcularPrecioVentaConAnalisis(costoDirecto, analisis);
    }

    /**
     * Calcula el Precio de Venta usando un análisis de sobrecosto específico.
     * 
     * @param costoDirecto El costo directo (base)
     * @param analisis El análisis de sobrecosto con los porcentajes configurados
     * @return El precio de venta calculado
     */
    public BigDecimal calcularPrecioVentaConAnalisis(BigDecimal costoDirecto, AnalisisSobrecosto analisis) {
        if (costoDirecto == null || costoDirecto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo directo no puede ser negativo");
        }

        // Nivel 1: Costo Directo (Base)
        BigDecimal subtotal1 = costoDirecto;

        // Nivel 2: Indirectos (% sobre CD)
        BigDecimal porcentajeIndirectosTotal = analisis.getPorcentajeIndirectosTotal();
        BigDecimal indirectos = subtotal1.multiply(porcentajeIndirectosTotal)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal2 = subtotal1.add(indirectos);

        // Nivel 3: Financiamiento (% sobre Subtotal2)
        BigDecimal financiamiento = subtotal2.multiply(analisis.getPorcentajeFinanciamiento())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal3 = subtotal2.add(financiamiento);

        // Nivel 4: Utilidad (% sobre Subtotal3)
        BigDecimal utilidad = subtotal3.multiply(analisis.getPorcentajeUtilidad())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal4 = subtotal3.add(utilidad);

        // Nivel 5: Cargos Adicionales (% sobre Subtotal4)
        BigDecimal porcentajeCargosAdicionalesTotal = analisis.getPorcentajeCargosAdicionalesTotal();
        BigDecimal cargosAdicionales = subtotal4.multiply(porcentajeCargosAdicionalesTotal)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal precioVenta = subtotal4.add(cargosAdicionales);

        return precioVenta.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el desglose completo de la cascada de cargos.
     * 
     * @param costoDirecto El costo directo (base)
     * @param analisis El análisis de sobrecosto
     * @return Desglose con todos los subtotales y cargos
     */
    public DesglosePrecioVenta calcularDesglose(BigDecimal costoDirecto, AnalisisSobrecosto analisis) {
        BigDecimal subtotal1 = costoDirecto;
        
        BigDecimal porcentajeIndirectosTotal = analisis.getPorcentajeIndirectosTotal();
        BigDecimal indirectos = subtotal1.multiply(porcentajeIndirectosTotal)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal2 = subtotal1.add(indirectos);
        
        BigDecimal financiamiento = subtotal2.multiply(analisis.getPorcentajeFinanciamiento())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal3 = subtotal2.add(financiamiento);
        
        BigDecimal utilidad = subtotal3.multiply(analisis.getPorcentajeUtilidad())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal4 = subtotal3.add(utilidad);
        
        BigDecimal porcentajeCargosAdicionalesTotal = analisis.getPorcentajeCargosAdicionalesTotal();
        BigDecimal cargosAdicionales = subtotal4.multiply(porcentajeCargosAdicionalesTotal)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal precioVenta = subtotal4.add(cargosAdicionales);

        return new DesglosePrecioVenta(
            costoDirecto,
            indirectos,
            subtotal2,
            financiamiento,
            subtotal3,
            utilidad,
            subtotal4,
            cargosAdicionales,
            precioVenta
        );
    }

    /**
     * Clase interna para almacenar el desglose completo de la cascada.
     */
    public static class DesglosePrecioVenta {
        private final BigDecimal costoDirecto;
        private final BigDecimal indirectos;
        private final BigDecimal subtotal2; // CD + Indirectos
        private final BigDecimal financiamiento;
        private final BigDecimal subtotal3; // Subtotal2 + Financiamiento
        private final BigDecimal utilidad;
        private final BigDecimal subtotal4; // Subtotal3 + Utilidad
        private final BigDecimal cargosAdicionales;
        private final BigDecimal precioVenta;

        public DesglosePrecioVenta(BigDecimal costoDirecto, BigDecimal indirectos,
                                  BigDecimal subtotal2, BigDecimal financiamiento,
                                  BigDecimal subtotal3, BigDecimal utilidad,
                                  BigDecimal subtotal4, BigDecimal cargosAdicionales,
                                  BigDecimal precioVenta) {
            this.costoDirecto = costoDirecto;
            this.indirectos = indirectos;
            this.subtotal2 = subtotal2;
            this.financiamiento = financiamiento;
            this.subtotal3 = subtotal3;
            this.utilidad = utilidad;
            this.subtotal4 = subtotal4;
            this.cargosAdicionales = cargosAdicionales;
            this.precioVenta = precioVenta;
        }

        public BigDecimal costoDirecto() { return costoDirecto; }
        public BigDecimal indirectos() { return indirectos; }
        public BigDecimal subtotal2() { return subtotal2; }
        public BigDecimal financiamiento() { return financiamiento; }
        public BigDecimal subtotal3() { return subtotal3; }
        public BigDecimal utilidad() { return utilidad; }
        public BigDecimal subtotal4() { return subtotal4; }
        public BigDecimal cargosAdicionales() { return cargosAdicionales; }
        public BigDecimal precioVenta() { return precioVenta; }
    }
}
