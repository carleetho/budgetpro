package com.budgetpro.domain.finanzas.control.service;

import com.budgetpro.domain.finanzas.apu.model.APU;
import com.budgetpro.domain.finanzas.apu.port.out.ApuRepository;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.partida.model.Partida;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de Dominio para agregar datos de control de costos (Plan vs Real).
 * 
 * Responsabilidad:
 * - Calcular valores PLAN (presupuesto) y REAL (ejecutado) por partida
 * - Agregar valores jerárquicamente (partidas padre suman hijos)
 * - Calcular desviaciones (saldo y porcentaje de ejecución)
 * 
 * No persiste, solo calcula y agrega datos.
 */
public class AgregacionControlCostosService {

    private final ApuRepository apuRepository;

    public AgregacionControlCostosService(ApuRepository apuRepository) {
        this.apuRepository = apuRepository;
    }

    /**
     * Agrega los datos de control de costos para todas las partidas.
     * 
     * @param partidas Lista de todas las partidas del presupuesto
     * @param consumos Lista de todos los consumos del presupuesto (o proyecto)
     * @return Mapa de partidaId -> datos agregados (plan, real, saldo, porcentaje)
     */
    public Map<UUID, DatosControlPartida> agregarDatosControl(
            List<Partida> partidas, List<ConsumoPartida> consumos) {
        
        // Crear mapa de consumos por partidaId para acceso rápido
        Map<UUID, List<ConsumoPartida>> consumosPorPartida = consumos.stream()
                .collect(Collectors.groupingBy(ConsumoPartida::getPartidaId));
        
        // Crear mapa de partidas por ID para acceso rápido
        Map<UUID, Partida> partidasPorId = partidas.stream()
                .collect(Collectors.toMap(p -> p.getId().getValue(), p -> p));
        
        // Crear mapa de hijos por padreId
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> p.getPadreId() != null)
                .collect(Collectors.groupingBy(Partida::getPadreId));
        
        // Calcular datos para cada partida (primero las hojas, luego los padres)
        Map<UUID, DatosControlPartida> datosPorPartida = new HashMap<>();
        
        // Procesar partidas hoja primero (sin hijos)
        for (Partida partida : partidas) {
            if (!hijosPorPadre.containsKey(partida.getId().getValue())) {
                // Es una partida hoja
                DatosControlPartida datos = calcularDatosPartidaHoja(partida, consumosPorPartida);
                datosPorPartida.put(partida.getId().getValue(), datos);
            }
        }
        
        // Procesar partidas padre (suman hijos)
        // Ordenar por nivel descendente para procesar desde las hojas hacia arriba
        List<Partida> partidasOrdenadas = partidas.stream()
                .sorted(Comparator.comparing(Partida::getNivel).reversed())
                .collect(Collectors.toList());
        
        for (Partida partida : partidasOrdenadas) {
            if (hijosPorPadre.containsKey(partida.getId().getValue())) {
                // Es una partida padre (tiene hijos)
                DatosControlPartida datos = calcularDatosPartidaPadre(
                        partida, hijosPorPadre.get(partida.getId().getValue()), datosPorPartida);
                datosPorPartida.put(partida.getId().getValue(), datos);
            }
        }
        
        return datosPorPartida;
    }

    /**
     * Calcula los datos de control para una partida hoja (sin hijos).
     */
    private DatosControlPartida calcularDatosPartidaHoja(
            Partida partida, Map<UUID, List<ConsumoPartida>> consumosPorPartida) {
        
        // PLAN: Metrado * Precio Unitario (del APU)
        BigDecimal metrado = partida.getMetrado() != null ? partida.getMetrado() : BigDecimal.ZERO;
        BigDecimal precioUnitario = BigDecimal.ZERO;
        BigDecimal parcialPlan = BigDecimal.ZERO;
        
        // Buscar APU de la partida
        Optional<APU> apuOpt = apuRepository.findByPartidaId(partida.getId().getValue());
        if (apuOpt.isPresent()) {
            APU apu = apuOpt.get();
            precioUnitario = apu.calcularCostoTotal(); // Costo unitario del APU
            parcialPlan = metrado.multiply(precioUnitario);
        }
        
        // REAL: Suma de consumos
        List<ConsumoPartida> consumosPartida = consumosPorPartida.getOrDefault(
                partida.getId().getValue(), Collections.emptyList());
        BigDecimal gastoAcumulado = consumosPartida.stream()
                .map(ConsumoPartida::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // DESVIACIÓN
        BigDecimal saldo = parcialPlan.subtract(gastoAcumulado);
        BigDecimal porcentajeEjecucion = BigDecimal.ZERO;
        if (parcialPlan.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecucion = gastoAcumulado
                    .divide(parcialPlan, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        return new DatosControlPartida(metrado, precioUnitario, parcialPlan,
                                      gastoAcumulado, saldo, porcentajeEjecucion);
    }

    /**
     * Calcula los datos de control para una partida padre (suma de hijos).
     */
    private DatosControlPartida calcularDatosPartidaPadre(
            Partida partida, List<Partida> hijos, Map<UUID, DatosControlPartida> datosPorPartida) {
        
        // Sumar valores de hijos
        BigDecimal parcialPlan = BigDecimal.ZERO;
        BigDecimal gastoAcumulado = BigDecimal.ZERO;
        
        for (Partida hijo : hijos) {
            DatosControlPartida datosHijo = datosPorPartida.get(hijo.getId().getValue());
            if (datosHijo != null) {
                parcialPlan = parcialPlan.add(datosHijo.parcialPlan());
                gastoAcumulado = gastoAcumulado.add(datosHijo.gastoAcumulado());
            }
        }
        
        // Para partidas padre, metrado y precio unitario no aplican directamente
        BigDecimal metrado = BigDecimal.ZERO;
        BigDecimal precioUnitario = BigDecimal.ZERO;
        
        // DESVIACIÓN
        BigDecimal saldo = parcialPlan.subtract(gastoAcumulado);
        BigDecimal porcentajeEjecucion = BigDecimal.ZERO;
        if (parcialPlan.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecucion = gastoAcumulado
                    .divide(parcialPlan, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        return new DatosControlPartida(metrado, precioUnitario, parcialPlan,
                                      gastoAcumulado, saldo, porcentajeEjecucion);
    }

    /**
     * Clase interna para almacenar datos agregados de una partida.
     */
    public static class DatosControlPartida {
        private final BigDecimal metrado;
        private final BigDecimal precioUnitario;
        private final BigDecimal parcialPlan;
        private final BigDecimal gastoAcumulado;
        private final BigDecimal saldo;
        private final BigDecimal porcentajeEjecucion;

        public DatosControlPartida(BigDecimal metrado, BigDecimal precioUnitario,
                                   BigDecimal parcialPlan, BigDecimal gastoAcumulado,
                                   BigDecimal saldo, BigDecimal porcentajeEjecucion) {
            this.metrado = metrado;
            this.precioUnitario = precioUnitario;
            this.parcialPlan = parcialPlan;
            this.gastoAcumulado = gastoAcumulado;
            this.saldo = saldo;
            this.porcentajeEjecucion = porcentajeEjecucion;
        }

        public BigDecimal metrado() { return metrado; }
        public BigDecimal precioUnitario() { return precioUnitario; }
        public BigDecimal parcialPlan() { return parcialPlan; }
        public BigDecimal gastoAcumulado() { return gastoAcumulado; }
        public BigDecimal saldo() { return saldo; }
        public BigDecimal porcentajeEjecucion() { return porcentajeEjecucion; }
    }
}
