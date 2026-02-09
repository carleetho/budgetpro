package com.budgetpro.domain.catalogo.service;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.ComposicionCuadrillaSnapshot;
import com.budgetpro.domain.shared.model.TipoRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.List;

/**
 * Servicio de dominio para cálculo dinámico de costos de APU usando fórmulas de
 * ingeniería civil.
 * 
 * Implementa las fórmulas según el tipo de recurso: - MATERIAL: Precio × Aporte
 * × (1 + Desperdicio) × TipoCambio - MANO_OBRA: (CostoDíaCuadrilla /
 * Rendimiento) × Aporte - EQUIPO_MAQUINA: CostoHora × (HorasUso / Rendimiento)
 * - EQUIPO_HERRAMIENTA: CostoTotalMO × Porcentaje
 * 
 * Respetando el orden de dependencias: MATERIAL, MANO_OBRA, EQUIPO_MAQUINA →
 * EQUIPO_HERRAMIENTA
 */

public class CalculoApuDinamicoService {

    private static final int PRECISION_INTERMEDIA = 10;
    private static final int PRECISION_FINAL = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calcula el costo unitario de un insumo según su tipo de recurso.
     * 
     * @param insumo             El insumo snapshot con todos los datos necesarios
     * @param rendimientoVigente El rendimiento vigente del APU (editable por el
     *                           ingeniero)
     * @param costoTotalMO       El costo total de mano de obra del APU (necesario
     *                           para herramientas)
     * @param monedaProyecto     La moneda del proyecto para normalización
     * @return El costo unitario calculado en la moneda del proyecto
     */
    public BigDecimal calcularCostoInsumo(APUInsumoSnapshot insumo, BigDecimal rendimientoVigente,
            BigDecimal costoTotalMO, String monedaProyecto) {
        if (insumo == null) {
            throw new IllegalArgumentException("El insumo no puede ser nulo");
        }
        if (rendimientoVigente == null || rendimientoVigente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El rendimiento vigente debe ser positivo");
        }
        if (monedaProyecto == null || monedaProyecto.isBlank()) {
            throw new IllegalArgumentException("La moneda del proyecto no puede estar vacía");
        }

        TipoRecurso tipoRecurso = insumo.getTipoRecurso();
        if (tipoRecurso == null) {
            // Backward compatibility: si no tiene tipo, usar cálculo simple
            return insumo.getSubtotal();
        }

        BigDecimal costo;
        switch (tipoRecurso) {
        case MATERIAL:
            costo = calcularCostoMaterial(insumo, monedaProyecto);
            break;
        case MANO_OBRA:
            costo = calcularCostoManoObra(insumo, rendimientoVigente, monedaProyecto);
            break;
        case EQUIPO_MAQUINA:
            costo = calcularCostoEquipoMaquina(insumo, rendimientoVigente, monedaProyecto);
            break;
        case EQUIPO_HERRAMIENTA:
            costo = calcularCostoEquipoHerramienta(insumo, costoTotalMO);
            break;
        case EQUIPO: // Deprecated: backward compatibility
            // EQUIPO deprecated se trata como EQUIPO_MAQUINA
            costo = calcularCostoEquipoMaquina(insumo, rendimientoVigente, monedaProyecto);
            break;
        case SUBCONTRATO:
            // SUBCONTRATO usa precio fijo, normalizado a moneda del proyecto
            BigDecimal precio = insumo.getPrecioPuestoEnObra() != null ? insumo.getPrecioPuestoEnObra()
                    : insumo.getPrecioUnitario();
            costo = normalizarPrecioAMonedaProyecto(precio, insumo.getMoneda(), insumo.getTipoCambioSnapshot(),
                    monedaProyecto);
            break;
        default:
            throw new IllegalArgumentException("Tipo de recurso no soportado: " + tipoRecurso);
        }

        return costo.setScale(PRECISION_FINAL, ROUNDING_MODE);
    }

    /**
     * Calcula el costo de MATERIAL usando la fórmula: Precio Puesto en Obra ×
     * Aporte Unitario × (1 + Desperdicio) × TipoCambio
     */
    private BigDecimal calcularCostoMaterial(APUInsumoSnapshot insumo, String monedaProyecto) {
        BigDecimal precioPuestoEnObra = insumo.getPrecioPuestoEnObra();
        if (precioPuestoEnObra == null) {
            precioPuestoEnObra = insumo.getPrecioUnitario();
        }

        BigDecimal aporteUnitario = insumo.getAporteUnitario();
        if (aporteUnitario == null) {
            aporteUnitario = insumo.getCantidad();
        }

        BigDecimal desperdicio = insumo.getDesperdicio();
        if (desperdicio == null) {
            desperdicio = BigDecimal.ZERO;
        }

        // Normalizar precio a moneda del proyecto
        BigDecimal precioNormalizado = normalizarPrecioAMonedaProyecto(precioPuestoEnObra, insumo.getMoneda(),
                insumo.getTipoCambioSnapshot(), monedaProyecto);

        // Fórmula: Precio × Aporte × (1 + Desperdicio)
        BigDecimal factorDesperdicio = BigDecimal.ONE.add(desperdicio);
        return precioNormalizado.multiply(aporteUnitario).multiply(factorDesperdicio).setScale(PRECISION_INTERMEDIA,
                ROUNDING_MODE);
    }

    /**
     * Calcula el costo de MANO_OBRA usando la fórmula: (Costo Día Cuadrilla /
     * Rendimiento Vigente) × Aporte Unitario
     */
    private BigDecimal calcularCostoManoObra(APUInsumoSnapshot insumo, BigDecimal rendimientoVigente,
            String monedaProyecto) {
        BigDecimal costoDiaCuadrilla = calcularCostoDiaCuadrilla(insumo.getComposicionCuadrilla(), monedaProyecto);

        if (costoDiaCuadrilla == null) {
            // Si no hay composición de cuadrilla, usar costo calculado almacenado
            costoDiaCuadrilla = insumo.getCostoDiaCuadrillaCalculado();
            if (costoDiaCuadrilla == null) {
                throw new IllegalArgumentException(
                        "No se puede calcular costo de mano de obra sin composición de cuadrilla o costo día cuadrilla calculado");
            }
        }

        BigDecimal aporteUnitario = insumo.getAporteUnitario();
        if (aporteUnitario == null) {
            aporteUnitario = insumo.getCantidad();
        }

        // Fórmula: (Costo Día Cuadrilla / Rendimiento) × Aporte
        return costoDiaCuadrilla.divide(rendimientoVigente, PRECISION_INTERMEDIA, ROUNDING_MODE)
                .multiply(aporteUnitario).setScale(PRECISION_INTERMEDIA, ROUNDING_MODE);
    }

    /**
     * Calcula el costo día cuadrilla como suma de: Σ(cantidad × costoDia ×
     * tipoCambio)
     */
    public BigDecimal calcularCostoDiaCuadrilla(List<ComposicionCuadrillaSnapshot> composicionCuadrilla,
            String monedaProyecto) {
        if (composicionCuadrilla == null || composicionCuadrilla.isEmpty()) {
            return null;
        }

        return composicionCuadrilla.stream().map(comp -> {
            BigDecimal cantidad = comp.cantidad();
            BigDecimal costoDia = comp.costoDia();
            BigDecimal tipoCambio = BigDecimal.ONE; // TODO: obtener tipo de cambio real si es necesario

            // Normalizar costo a moneda del proyecto
            BigDecimal costoNormalizado = normalizarPrecioAMonedaProyecto(costoDia, comp.moneda(), tipoCambio,
                    monedaProyecto);

            return cantidad.multiply(costoNormalizado);
        }).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(PRECISION_INTERMEDIA, ROUNDING_MODE);
    }

    /**
     * Calcula el costo de EQUIPO_MAQUINA usando la fórmula: Costo Hora Máquina ×
     * (Horas Uso / Rendimiento Vigente)
     */
    private BigDecimal calcularCostoEquipoMaquina(APUInsumoSnapshot insumo, BigDecimal rendimientoVigente,
            String monedaProyecto) {
        BigDecimal costoHoraMaquina = insumo.getCostoHoraMaquina();
        if (costoHoraMaquina == null) {
            throw new IllegalArgumentException("No se puede calcular costo de equipo maquinaria sin costoHoraMaquina");
        }

        BigDecimal horasUso = insumo.getHorasUso();
        if (horasUso == null) {
            throw new IllegalArgumentException("No se puede calcular costo de equipo maquinaria sin horasUso");
        }

        // Normalizar costo a moneda del proyecto
        BigDecimal costoNormalizado = normalizarPrecioAMonedaProyecto(costoHoraMaquina, insumo.getMoneda(),
                insumo.getTipoCambioSnapshot(), monedaProyecto);

        // Fórmula: Costo Hora × (Horas Uso / Rendimiento)
        return costoNormalizado.multiply(horasUso).divide(rendimientoVigente, PRECISION_INTERMEDIA, ROUNDING_MODE)
                .setScale(PRECISION_INTERMEDIA, ROUNDING_MODE);
    }

    /**
     * Calcula el costo de EQUIPO_HERRAMIENTA usando la fórmula: Costo Total Mano de
     * Obra × Porcentaje
     */
    private BigDecimal calcularCostoEquipoHerramienta(APUInsumoSnapshot insumo, BigDecimal costoTotalMO) {
        if (costoTotalMO == null) {
            throw new IllegalArgumentException(
                    "No se puede calcular costo de herramienta sin costo total de mano de obra");
        }

        BigDecimal porcentajeManoObra = insumo.getPorcentajeManoObra();
        if (porcentajeManoObra == null) {
            throw new IllegalArgumentException("No se puede calcular costo de herramienta sin porcentajeManoObra");
        }

        // Fórmula: Costo Total MO × Porcentaje
        return costoTotalMO.multiply(porcentajeManoObra).setScale(PRECISION_INTERMEDIA, ROUNDING_MODE);
    }

    /**
     * Normaliza un precio de una moneda origen a la moneda del proyecto usando tipo
     * de cambio.
     */
    public BigDecimal normalizarPrecioAMonedaProyecto(BigDecimal precio, String monedaOrigen, BigDecimal tipoCambio,
            String monedaProyecto) {
        if (precio == null) {
            return BigDecimal.ZERO;
        }
        if (monedaOrigen == null || monedaOrigen.isBlank()) {
            return precio; // Asumir misma moneda si no se especifica
        }
        if (monedaProyecto == null || monedaProyecto.isBlank()) {
            return precio; // Asumir misma moneda si no se especifica
        }
        if (monedaOrigen.equals(monedaProyecto)) {
            return precio; // Misma moneda, no necesita conversión
        }
        if (tipoCambio == null || tipoCambio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Tipo de cambio inválido para convertir de " + monedaOrigen + " a " + monedaProyecto);
        }

        return precio.multiply(tipoCambio).setScale(PRECISION_INTERMEDIA, ROUNDING_MODE);
    }

    /**
     * Calcula el costo total del APU respetando el orden de dependencias: 1.
     * MATERIAL (independiente) 2. MANO_OBRA (independiente) 3. EQUIPO_MAQUINA
     * (independiente) 4. EQUIPO_HERRAMIENTA (depende de paso 2)
     * 
     * @param apuSnapshot    El snapshot del APU con todos sus insumos
     * @param monedaProyecto La moneda del proyecto
     * @return El costo total calculado
     */
    public BigDecimal calcularCostoTotalAPU(APUSnapshot apuSnapshot, String monedaProyecto) {
        if (apuSnapshot == null) {
            throw new IllegalArgumentException("El APUSnapshot no puede ser nulo");
        }
        if (monedaProyecto == null || monedaProyecto.isBlank()) {
            throw new IllegalArgumentException("La moneda del proyecto no puede estar vacía");
        }

        BigDecimal rendimientoVigente = apuSnapshot.getRendimientoVigente();
        List<APUInsumoSnapshot> insumos = apuSnapshot.getInsumos();

        // Primera pasada: calcular independientes (MATERIAL, MANO_OBRA, EQUIPO_MAQUINA)
        BigDecimal costoTotalMaterial = BigDecimal.ZERO;
        BigDecimal costoTotalMO = BigDecimal.ZERO;
        BigDecimal costoTotalEquipoMaquina = BigDecimal.ZERO;

        for (APUInsumoSnapshot insumo : insumos) {
            TipoRecurso tipoRecurso = insumo.getTipoRecurso();
            if (tipoRecurso == null) {
                // Backward compatibility: insumos legacy se suman directamente
                costoTotalMaterial = costoTotalMaterial.add(insumo.getSubtotal());
                continue;
            }

            switch (tipoRecurso) {
            case MATERIAL:
                costoTotalMaterial = costoTotalMaterial
                        .add(calcularCostoInsumo(insumo, rendimientoVigente, BigDecimal.ZERO, monedaProyecto));
                break;
            case MANO_OBRA: {
                BigDecimal costoMO = calcularCostoInsumo(insumo, rendimientoVigente, BigDecimal.ZERO, monedaProyecto);
                costoTotalMO = costoTotalMO.add(costoMO);
                break;
            }
            case EQUIPO_MAQUINA: {
                BigDecimal costoEquipo = calcularCostoInsumo(insumo, rendimientoVigente, BigDecimal.ZERO,
                        monedaProyecto);
                costoTotalEquipoMaquina = costoTotalEquipoMaquina.add(costoEquipo);
                break;
            }
            case EQUIPO: // Deprecated: backward compatibility
                // EQUIPO deprecated se trata como EQUIPO_MAQUINA
                BigDecimal costoEquipo = calcularCostoInsumo(insumo, rendimientoVigente, BigDecimal.ZERO,
                        monedaProyecto);
                costoTotalEquipoMaquina = costoTotalEquipoMaquina.add(costoEquipo);
                break;
            case SUBCONTRATO: {
                BigDecimal costoSub = calcularCostoInsumo(insumo, rendimientoVigente, BigDecimal.ZERO, monedaProyecto);
                costoTotalMaterial = costoTotalMaterial.add(costoSub);
                break;
            }
            // EQUIPO_HERRAMIENTA se calcula en segunda pasada
            }
        }

        // Segunda pasada: calcular dependientes (EQUIPO_HERRAMIENTA)
        BigDecimal costoTotalHerramienta = BigDecimal.ZERO;
        for (APUInsumoSnapshot insumo : insumos) {
            TipoRecurso tipoRecurso = insumo.getTipoRecurso();
            if (tipoRecurso == TipoRecurso.EQUIPO_HERRAMIENTA) {
                BigDecimal costoHerramienta = calcularCostoInsumo(insumo, rendimientoVigente, costoTotalMO,
                        monedaProyecto);
                costoTotalHerramienta = costoTotalHerramienta.add(costoHerramienta);
            }
        }

        // Sumar todos los costos
        return costoTotalMaterial.add(costoTotalMO).add(costoTotalEquipoMaquina).add(costoTotalHerramienta)
                .setScale(PRECISION_FINAL, ROUNDING_MODE);
    }
}
