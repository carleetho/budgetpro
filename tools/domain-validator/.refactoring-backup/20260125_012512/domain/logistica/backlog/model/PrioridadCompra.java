package com.budgetpro.domain.logistica.backlog.model;

/**
 * Enum que representa la prioridad de un RequerimientoCompra.
 */
public enum PrioridadCompra {
    /**
     * Prioridad urgente: necesidad inmediata, requiere atención prioritaria.
     * Usado cuando stock = 0 y hay requisiciones pendientes.
     */
    URGENTE,
    
    /**
     * Prioridad normal: necesidad planificada, proceso estándar.
     */
    NORMAL,
    
    /**
     * Prioridad baja: necesidad no crítica, puede esperar.
     */
    BAJA
}
