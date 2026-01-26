package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;

/**
 * Servicio de dominio para calcular hashes criptográficos de integridad de presupuestos.
 * 
 * **Patrón Dual-Hash:**
 * - Hash de Aprobación: Captura la estructura inmutable del presupuesto
 * - Hash de Ejecución: Captura el estado financiero dinámico
 */
public interface IntegrityHashService {

    /**
     * Calcula el hash criptográfico de aprobación basado en la estructura del presupuesto.
     * 
     * Este hash debe incluir:
     * - ID del presupuesto
     * - Nombre
     * - Estado
     * - Partidas y su estructura jerárquica (Merkle tree)
     * - APUs asociados
     * - Cualquier otro campo estructural que no deba cambiar después de aprobación
     * 
     * @param presupuesto El presupuesto del cual calcular el hash
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     */
    String calculateApprovalHash(Presupuesto presupuesto);

    /**
     * Calcula el hash criptográfico de ejecución basado en el estado financiero actual.
     * 
     * Este hash debe incluir:
     * - Hash de aprobación (encadenado)
     * - Saldos disponibles por partida
     * - Consumos registrados
     * - Compromisos pendientes
     * - Estado de ejecución financiera
     * 
     * @param presupuesto El presupuesto del cual calcular el hash
     * @return Hash SHA-256 de 64 caracteres hexadecimales
     */
    String calculateExecutionHash(Presupuesto presupuesto);
}
