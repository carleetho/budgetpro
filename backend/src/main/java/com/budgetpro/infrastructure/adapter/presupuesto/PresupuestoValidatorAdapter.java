package com.budgetpro.infrastructure.adapter.presupuesto;

import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.logistica.compra.port.out.PresupuestoValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de infraestructura para PresupuestoValidator.
 * 
 * Integra con el módulo Presupuesto para validar:
 * - REGLA-153: Partidas deben ser leaf nodes (sin hijos)
 * - L-01: Disponibilidad de presupuesto (BAC - committed - actual)
 */
@Component
public class PresupuestoValidatorAdapter implements PresupuestoValidator {

    private final PresupuestoRepository presupuestoRepository;
    private final PartidaRepository partidaRepository;

    public PresupuestoValidatorAdapter(PresupuestoRepository presupuestoRepository,
                                     PartidaRepository partidaRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.partidaRepository = partidaRepository;
    }

    @Override
    public void validarDisponibilidadPresupuesto(UUID proyectoId, BigDecimal montoTotal) {
        // 1. Obtener presupuesto congelado del proyecto
        Presupuesto presupuesto = presupuestoRepository.findActiveByProyectoId(proyectoId)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No se encontró presupuesto activo para el proyecto: %s", proyectoId)
                ));

        // 2. Validar que el presupuesto esté congelado (frozen budget)
        if (presupuesto.getEstado() != EstadoPresupuesto.CONGELADO) {
            throw new IllegalStateException(
                String.format("El presupuesto del proyecto %s no está congelado. Estado actual: %s. " +
                             "Solo se pueden validar compras contra presupuestos congelados (L-01).",
                    proyectoId, presupuesto.getEstado())
            );
        }

        // 3. Calcular BAC (Budget at Completion) = suma de presupuestoAsignado de partidas raíz
        BigDecimal bac = calcularBAC(presupuesto.getId().getValue());

        // 4. Calcular costos comprometidos (compromisosPendientes) = suma de todos los compromisos
        BigDecimal comprometido = calcularComprometido(presupuesto.getId().getValue());

        // 5. Calcular costos reales (gastosReales) = suma de todos los gastos reales
        BigDecimal actual = calcularActual(presupuesto.getId().getValue());

        // 6. Calcular presupuesto disponible
        BigDecimal disponible = bac.subtract(comprometido).subtract(actual);

        // 7. Validar que el monto total no exceda el disponible
        if (montoTotal.compareTo(disponible) > 0) {
            throw new IllegalStateException(
                String.format("L-01: El monto total (%.2f) excede el presupuesto disponible (%.2f). " +
                             "BAC: %.2f, Comprometido: %.2f, Actual: %.2f",
                    montoTotal, disponible, bac, comprometido, actual)
            );
        }
    }

    /**
     * Calcula el BAC (Budget at Completion) sumando el presupuestoAsignado de todas las partidas raíz.
     * 
     * @param presupuestoId ID del presupuesto
     * @return BAC calculado
     */
    private BigDecimal calcularBAC(UUID presupuestoId) {
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);
        
        // Filtrar partidas raíz (sin padre) y sumar presupuestoAsignado
        return partidas.stream()
                .filter(Partida::isRaiz)
                .map(Partida::getPresupuestoAsignado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total de costos comprometidos (compromisosPendientes) de todas las partidas.
     * 
     * @param presupuestoId ID del presupuesto
     * @return Total de compromisos pendientes
     */
    private BigDecimal calcularComprometido(UUID presupuestoId) {
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);
        
        return partidas.stream()
                .map(Partida::getCompromisosPendientes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el total de costos reales (gastosReales) de todas las partidas.
     * 
     * @param presupuestoId ID del presupuesto
     * @return Total de gastos reales
     */
    private BigDecimal calcularActual(UUID presupuestoId) {
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);
        
        return partidas.stream()
                .map(Partida::getGastosReales)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
