package com.budgetpro.domain.finanzas.alertas.service;

import com.budgetpro.domain.finanzas.alertas.model.AlertaParametrica;
import com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto;
import com.budgetpro.domain.finanzas.alertas.model.NivelAlerta;
import com.budgetpro.domain.finanzas.alertas.model.TipoAlertaParametrica;
import com.budgetpro.domain.shared.model.TipoRecurso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de Dominio para análisis paramétrico de presupuestos.
 * 
 * Implementa las reglas de negocio (Hard Rules) basadas en metodología Suárez Salazar:
 * - Cap. 2.1340: Alertas Paramétricas
 * 
 * Reglas implementadas:
 * 1. Maquinaria: Si costo_horario == 0 en equipo propio → ALERTA CRÍTICA
 * 2. Acero: Ratio Kg Acero / m3 Concreto fuera de rango 80-150 kg/m3 → WARNING
 * 3. Concreto: Si agregado > 1/5 del ancho del elemento → ALERTA TÉCNICA
 * 
 * No persiste, solo analiza y genera alertas.
 */
public class AnalizadorParametricoService {
    
    // Constantes para validaciones
    private static final BigDecimal RATIO_ACERO_MIN = new BigDecimal("80");
    private static final BigDecimal RATIO_ACERO_MAX = new BigDecimal("150");
    private static final BigDecimal FACTOR_AGREGADO_MAX = new BigDecimal("0.2"); // 1/5 = 0.2
    
    /**
     * Analiza un presupuesto y genera alertas paramétricas.
     * 
     * @param presupuestoId ID del presupuesto a analizar
     * @param datosAnalisis Datos necesarios para el análisis (APUs, recursos, partidas)
     * @return AnalisisPresupuesto con las alertas generadas
     */
    public AnalisisPresupuesto analizar(UUID presupuestoId, DatosAnalisis datosAnalisis) {
        AnalisisPresupuesto analisis = AnalisisPresupuesto.crear(presupuestoId);
        
        // Aplicar reglas de negocio
        analizarMaquinaria(analisis, datosAnalisis);
        analizarAceroConcreto(analisis, datosAnalisis);
        analizarTamanoAgregado(analisis, datosAnalisis);
        
        return analisis;
    }
    
    /**
     * Regla 1: Maquinaria sin costo horario.
     * 
     * Si un recurso es de tipo EQUIPO (equipo propio) y tiene costo_horario == 0,
     * genera ALERTA CRÍTICA: "Descapitalización inminente. Sugerir depreciación"
     * 
     * Referencia: Suárez Salazar Pág. 174 PDF
     */
    private void analizarMaquinaria(AnalisisPresupuesto analisis, DatosAnalisis datos) {
        for (DatosRecurso recurso : datos.getRecursos()) {
            if (recurso.getTipo() == TipoRecurso.EQUIPO) {
                // Obtener costo_horario de los atributos
                BigDecimal costoHorario = obtenerCostoHorario(recurso.getAtributos());
                
                // REGLA-026
                if (costoHorario == null || costoHorario.compareTo(BigDecimal.ZERO) == 0) {
                    AlertaParametrica alerta = AlertaParametrica.crear(
                        TipoAlertaParametrica.MAQUINARIA_COSTO_HORARIO,
                        NivelAlerta.CRITICA,
                        null, // No está asociada a una partida específica
                        recurso.getId(),
                        "Descapitalización inminente. El equipo '" + recurso.getNombre() + 
                        "' (EQUIPO) tiene costo_horario = 0. Esto puede llevar a pérdidas por no considerar la depreciación del activo.",
                        BigDecimal.ZERO,
                        "Sugerir configurar un costo horario basado en depreciación del activo. " +
                        "Fórmula sugerida: (Valor de adquisición - Valor residual) / (Vida útil en horas)"
                    );
                    analisis.agregarAlerta(alerta);
                }
            }
        }
    }
    
    /**
     * Regla 2: Ratio Acero/Concreto fuera de rango.
     * 
     * Calcula el ratio Kg Acero / m3 Concreto para partidas de concreto.
     * Si está fuera del rango 80-150 kg/m3 (para estructuras estándar) → WARNING.
     */
    private void analizarAceroConcreto(AnalisisPresupuesto analisis, DatosAnalisis datos) {
        for (DatosPartida partida : datos.getPartidas()) {
            // Buscar si la partida tiene concreto y acero
            BigDecimal cantidadConcreto = buscarCantidadRecurso(partida, "CONCRETO", TipoRecurso.MATERIAL);
            BigDecimal cantidadAcero = buscarCantidadRecurso(partida, "ACERO", TipoRecurso.MATERIAL);
            
            if (cantidadConcreto != null && cantidadConcreto.compareTo(BigDecimal.ZERO) > 0 &&
                cantidadAcero != null && cantidadAcero.compareTo(BigDecimal.ZERO) > 0) {
                
                // Calcular ratio: kg acero / m3 concreto
                // Asumiendo que cantidadAcero está en kg y cantidadConcreto en m3
                BigDecimal ratio = cantidadAcero.divide(cantidadConcreto, 4, RoundingMode.HALF_UP);
                
                // REGLA-027
                if (ratio.compareTo(RATIO_ACERO_MIN) < 0 || ratio.compareTo(RATIO_ACERO_MAX) > 0) {
                    AlertaParametrica alerta = AlertaParametrica.crear(
                        TipoAlertaParametrica.ACERO_RATIO_CONCRETO,
                        NivelAlerta.WARNING,
                        partida.getId(),
                        null,
                        String.format(
                            "Ratio Acero/Concreto fuera de rango estándar. Partida '%s': %.2f kg/m³ (rango esperado: 80-150 kg/m³)",
                            partida.getDescripcion(),
                            ratio
                        ),
                        ratio,
                        RATIO_ACERO_MIN,
                        RATIO_ACERO_MAX,
                        "Verificar que las cantidades de acero y concreto sean correctas. " +
                        "Para estructuras estándar, el ratio debería estar entre 80-150 kg/m³."
                    );
                    analisis.agregarAlerta(alerta);
                }
            }
        }
    }
    
    /**
     * Regla 3: Tamaño de agregado inadecuado.
     * 
     * Si el agregado (grava) > 1/5 del ancho del elemento (ej: grava 1.5" en muro de 15cm)
     * → ALERTA TÉCNICA (Riesgo de colado).
     */
    private void analizarTamanoAgregado(AnalisisPresupuesto analisis, DatosAnalisis datos) {
        for (DatosPartida partida : datos.getPartidas()) {
            // Buscar agregado/grava en la partida
            BigDecimal tamanoAgregado = buscarTamanoAgregado(partida);
            BigDecimal anchoElemento = obtenerAnchoElemento(partida);
            
            if (tamanoAgregado != null && anchoElemento != null && 
                anchoElemento.compareTo(BigDecimal.ZERO) > 0) {
                
                BigDecimal factor = tamanoAgregado.divide(anchoElemento, 4, RoundingMode.HALF_UP);
                
                // REGLA-028
                if (factor.compareTo(FACTOR_AGREGADO_MAX) > 0) {
                    AlertaParametrica alerta = AlertaParametrica.crear(
                        TipoAlertaParametrica.CONCRETO_TAMANO_AGREGADO,
                        NivelAlerta.WARNING,
                        partida.getId(),
                        null,
                        String.format(
                            "Tamaño de agregado inadecuado. Partida '%s': agregado %.2f cm en elemento de %.2f cm de ancho (máximo recomendado: %.0f%% del ancho)",
                            partida.getDescripcion(),
                            tamanoAgregado,
                            anchoElemento,
                            FACTOR_AGREGADO_MAX.multiply(new BigDecimal("100"))
                        ),
                        factor,
                        null,
                        FACTOR_AGREGADO_MAX,
                        "El tamaño del agregado no debe exceder 1/5 (20%) del ancho del elemento estructural. " +
                        "Riesgo de problemas en el colado del concreto."
                    );
                    analisis.agregarAlerta(alerta);
                }
            }
        }
    }
    
    // Métodos auxiliares
    
    private BigDecimal obtenerCostoHorario(Map<String, Object> atributos) {
        if (atributos == null) {
            return null;
        }
        Object costoHorario = atributos.get("costo_horario");
        if (costoHorario instanceof Number) {
            return BigDecimal.valueOf(((Number) costoHorario).doubleValue());
        }
        return null;
    }
    
    private BigDecimal buscarCantidadRecurso(DatosPartida partida, String nombreRecurso, TipoRecurso tipo) {
        for (DatosApuInsumo insumo : partida.getInsumos()) {
            if (insumo.getRecursoNombre().toUpperCase().contains(nombreRecurso) &&
                insumo.getRecursoTipo() == tipo) {
                return insumo.getCantidad();
            }
        }
        return null;
    }
    
    private BigDecimal buscarTamanoAgregado(DatosPartida partida) {
        for (DatosApuInsumo insumo : partida.getInsumos()) {
            String nombre = insumo.getRecursoNombre().toUpperCase();
            if (nombre.contains("GRAVA") || nombre.contains("AGREGADO")) {
                // Intentar extraer el tamaño del nombre o atributos
                Map<String, Object> atributos = insumo.getRecursoAtributos();
                if (atributos != null && atributos.containsKey("tamano_cm")) {
                    Object tamano = atributos.get("tamano_cm");
                    if (tamano instanceof Number) {
                        return BigDecimal.valueOf(((Number) tamano).doubleValue());
                    }
                }
            }
        }
        return null;
    }
    
    private BigDecimal obtenerAnchoElemento(DatosPartida partida) {
        // Intentar obtener del nombre o descripción de la partida
        // En una implementación real, esto podría venir de atributos de la partida
        // Por ahora, retornamos null si no se puede determinar
        return null; // Se puede mejorar extrayendo de descripción o atributos
    }
    
    // Clases DTO internas para datos de análisis
    
    /**
     * DTO para datos necesarios para el análisis.
     */
    public static class DatosAnalisis {
        private final List<DatosRecurso> recursos;
        private final List<DatosPartida> partidas;
        
        public DatosAnalisis(List<DatosRecurso> recursos, List<DatosPartida> partidas) {
            this.recursos = recursos != null ? new ArrayList<>(recursos) : new ArrayList<>();
            this.partidas = partidas != null ? new ArrayList<>(partidas) : new ArrayList<>();
        }
        
        public List<DatosRecurso> getRecursos() {
            return List.copyOf(recursos);
        }
        
        public List<DatosPartida> getPartidas() {
            return List.copyOf(partidas);
        }
    }
    
    /**
     * DTO para datos de un recurso.
     */
    public static class DatosRecurso {
        private final UUID id;
        private final String nombre;
        private final TipoRecurso tipo;
        private final Map<String, Object> atributos;
        
        public DatosRecurso(UUID id, String nombre, TipoRecurso tipo, Map<String, Object> atributos) {
            this.id = id;
            this.nombre = nombre;
            this.tipo = tipo;
            this.atributos = atributos;
        }
        
        public UUID getId() { return id; }
        public String getNombre() { return nombre; }
        public TipoRecurso getTipo() { return tipo; }
        public Map<String, Object> getAtributos() { return atributos; }
    }
    
    /**
     * DTO para datos de una partida con sus insumos.
     */
    public static class DatosPartida {
        private final UUID id;
        private final String descripcion;
        private final List<DatosApuInsumo> insumos;
        
        public DatosPartida(UUID id, String descripcion, List<DatosApuInsumo> insumos) {
            this.id = id;
            this.descripcion = descripcion;
            this.insumos = insumos != null ? new ArrayList<>(insumos) : new ArrayList<>();
        }
        
        public UUID getId() { return id; }
        public String getDescripcion() { return descripcion; }
        public List<DatosApuInsumo> getInsumos() { return List.copyOf(insumos); }
    }
    
    /**
     * DTO para datos de un insumo de APU.
     */
    public static class DatosApuInsumo {
        private final UUID recursoId;
        private final String recursoNombre;
        private final TipoRecurso recursoTipo;
        private final BigDecimal cantidad;
        private final Map<String, Object> recursoAtributos;
        
        public DatosApuInsumo(UUID recursoId, String recursoNombre, TipoRecurso recursoTipo,
                            BigDecimal cantidad, Map<String, Object> recursoAtributos) {
            this.recursoId = recursoId;
            this.recursoNombre = recursoNombre;
            this.recursoTipo = recursoTipo;
            this.cantidad = cantidad;
            this.recursoAtributos = recursoAtributos;
        }
        
        public UUID getRecursoId() { return recursoId; }
        public String getRecursoNombre() { return recursoNombre; }
        public TipoRecurso getRecursoTipo() { return recursoTipo; }
        public BigDecimal getCantidad() { return cantidad; }
        public Map<String, Object> getRecursoAtributos() { return recursoAtributos; }
    }
}
