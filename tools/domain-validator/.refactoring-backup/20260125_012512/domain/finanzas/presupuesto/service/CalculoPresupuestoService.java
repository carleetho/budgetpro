package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Dominio para calcular costos de presupuestos.
 * 
 * No persiste, solo calcula.
 * 
 * Lógica de cálculo:
 * - Para cada partida:
 *   - Si es Título (tiene hijos): Costo = Suma de Costos de Hijos
 *   - Si es Hoja (tiene APU): Costo = Partida.metrado * APU.costoUnitario
 * - Costo Total Presupuesto = Suma de Costos de Partidas Raíz
 */
public class CalculoPresupuestoService {

    private final PartidaRepository partidaRepository;
    private final ApuRepository apuRepository;

    public CalculoPresupuestoService(PartidaRepository partidaRepository,
                                     ApuRepository apuRepository) {
        this.partidaRepository = partidaRepository;
        this.apuRepository = apuRepository;
    }

    /**
     * Calcula el costo total de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return El costo total calculado
     */
    public BigDecimal calcularCostoTotal(UUID presupuestoId) {
        // Obtener todas las partidas del presupuesto
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);

        if (partidas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Separar partidas raíz (sin padre) y construir mapa de hijos
        List<Partida> partidasRaiz = partidas.stream()
                .filter(Partida::isRaiz)
                .collect(Collectors.toList());

        // Construir mapa de hijos por padre
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> !p.isRaiz())
                .collect(Collectors.groupingBy(Partida::getPadreId));

        // Calcular costo total sumando costos de partidas raíz
        return partidasRaiz.stream()
                .map(partidaRaiz -> calcularCostoPartida(partidaRaiz, hijosPorPadre))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el costo de una partida recursivamente.
     * 
     * @param partida La partida a calcular
     * @param hijosPorPadre Mapa de hijos agrupados por padre
     * @return El costo de la partida
     */
    private BigDecimal calcularCostoPartida(Partida partida, Map<UUID, List<Partida>> hijosPorPadre) {
        List<Partida> hijos = hijosPorPadre.get(partida.getId().getValue());

        if (hijos != null && !hijos.isEmpty()) {
            // Es título: suma de costos de hijos
            return hijos.stream()
                    .map(hijo -> calcularCostoPartida(hijo, hijosPorPadre))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Es hoja: metrado * costo unitario del APU
            return calcularCostoPartidaHoja(partida);
        }
    }

    /**
     * Calcula el costo de una partida hoja (que tiene APU).
     * 
     * @param partida La partida hoja
     * @return El costo calculado: metrado * costo unitario del APU
     */
    private BigDecimal calcularCostoPartidaHoja(Partida partida) {
        // Buscar APU de la partida
        APU apu = apuRepository.findByPartidaId(partida.getId().getValue())
                .orElse(null);

        if (apu == null) {
            // Si no tiene APU, el costo es 0
            return BigDecimal.ZERO;
        }

        // Calcular: metrado * costo unitario del APU
        BigDecimal metrado = partida.getMetrado();
        BigDecimal costoUnitario = apu.calcularCostoTotal(); // Costo total del APU

        return metrado.multiply(costoUnitario);
    }

    /**
     * Verifica si todas las partidas hoja del presupuesto tienen APU.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return true si todas las partidas hoja tienen APU, false en caso contrario
     */
    public boolean todasLasPartidasHojaTienenAPU(UUID presupuestoId) {
        // Obtener todas las partidas del presupuesto
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);

        if (partidas.isEmpty()) {
            return true; // Sin partidas, se considera válido
        }

        // Construir mapa de hijos por padre
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> !p.isRaiz())
                .collect(Collectors.groupingBy(Partida::getPadreId));

        // Identificar partidas hoja (las que no tienen hijos)
        List<Partida> partidasHoja = partidas.stream()
                .filter(partida -> {
                    List<Partida> hijos = hijosPorPadre.get(partida.getId().getValue());
                    return hijos == null || hijos.isEmpty();
                })
                .collect(Collectors.toList());

        // Verificar que todas las partidas hoja tengan APU
        return partidasHoja.stream()
                .allMatch(partida -> apuRepository.existsByPartidaId(partida.getId().getValue()));
    }

    /**
     * Obtiene el costo de una partida específica.
     * 
     * @param partidaId El ID de la partida
     * @return El costo de la partida, o BigDecimal.ZERO si no existe o no tiene APU
     */
    public BigDecimal calcularCostoPartida(UUID partidaId) {
        Partida partida = partidaRepository.findById(partidaId)
                .orElse(null);

        if (partida == null) {
            return BigDecimal.ZERO;
        }

        // Obtener todas las partidas del presupuesto para construir el árbol
        List<Partida> partidas = partidaRepository.findByPresupuestoId(partida.getPresupuestoId());

        // Construir mapa de hijos por padre
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> !p.isRaiz())
                .collect(Collectors.groupingBy(Partida::getPadreId));

        return calcularCostoPartida(partida, hijosPorPadre);
    }
}
