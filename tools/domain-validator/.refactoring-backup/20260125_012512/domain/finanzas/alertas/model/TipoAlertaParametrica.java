package com.budgetpro.domain.finanzas.alertas.model;

/**
 * Enum que representa el tipo de alerta paramétrica.
 * Basado en metodología Suárez Salazar (Cap. 2.1340 - Alertas Paramétricas).
 */
public enum TipoAlertaParametrica {
    /**
     * Alerta: Maquinaria sin costo horario.
     * Detecta equipos propios (ACTIVO) con costo_horario = 0.
     * Sugiere depreciación para evitar descapitalización.
     */
    MAQUINARIA_COSTO_HORARIO,
    
    /**
     * Alerta: Ratio Acero/Concreto fuera de rango.
     * Valida que el ratio Kg Acero / m3 Concreto esté entre 80-150 kg/m3 para estructuras estándar.
     */
    ACERO_RATIO_CONCRETO,
    
    /**
     * Alerta: Tamaño de agregado inadecuado.
     * Valida que el agregado (grava) no exceda 1/5 del ancho del elemento estructural.
     */
    CONCRETO_TAMANO_AGREGADO
}
