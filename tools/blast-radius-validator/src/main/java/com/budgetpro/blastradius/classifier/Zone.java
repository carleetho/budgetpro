package com.budgetpro.blastradius.classifier;

/**
 * Zonas de protección para archivos.
 * Define el nivel de protección de diferentes áreas del código.
 */
public enum Zone {
    /**
     * Zona roja: Áreas críticas que requieren aprobación especial.
     * Cambios aquí tienen el mayor impacto.
     */
    RED,
    
    /**
     * Zona amarilla: Áreas que requieren atención pero con límites más flexibles.
     * Cambios aquí tienen impacto moderado.
     */
    YELLOW,
    
    /**
     * Zona verde: Áreas sin restricciones especiales.
     * Cambios aquí tienen impacto bajo.
     */
    GREEN
}
