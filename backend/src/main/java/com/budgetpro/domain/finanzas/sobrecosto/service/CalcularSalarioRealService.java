package com.budgetpro.domain.finanzas.sobrecosto.service;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.ConfiguracionLaboralRepository;
import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.shared.model.TipoRecurso;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Servicio de Dominio para calcular el Salario Real de un recurso de Mano de Obra.
 * 
 * Implementa la metodología de Suárez Salazar (Pág. 118-121) de forma configurable.
 * 
 * Fórmula del libro: FSR = TotalTrabajado / TotalPagado
 * 
 * Donde:
 * - TotalTrabajado = Días trabajados efectivos
 * - TotalPagado = Días trabajados + Días no trabajados pagados + Prestaciones
 * 
 * SalarioReal = SalarioBase × FSR
 * 
 * No persiste, solo calcula usando parámetros configurables.
 */
public class CalcularSalarioRealService {

    private final ConfiguracionLaboralRepository configuracionLaboralRepository;

    public CalcularSalarioRealService(ConfiguracionLaboralRepository configuracionLaboralRepository) {
        this.configuracionLaboralRepository = configuracionLaboralRepository;
    }

    /**
     * Calcula el Factor Salario Real (FSR) para un recurso de Mano de Obra.
     * 
     * @param recurso El recurso de tipo MANO_OBRA
     * @param proyectoId El ID del proyecto (opcional, para configuración por proyecto)
     * @return El FSR calculado
     */
    public BigDecimal calcularFSR(Recurso recurso, UUID proyectoId) {
        if (recurso.getTipo() != TipoRecurso.MANO_OBRA) {
            throw new IllegalArgumentException("El recurso debe ser de tipo MANO_OBRA para calcular FSR");
        }

        // Buscar configuración laboral (por proyecto o global)
        ConfiguracionLaboral configuracion = null;
        if (proyectoId != null) {
            configuracion = configuracionLaboralRepository.findByProyectoId(proyectoId)
                    .orElse(null);
        }

        // Si no hay configuración por proyecto, usar la global
        if (configuracion == null) {
            configuracion = configuracionLaboralRepository.findGlobal()
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe configuración laboral. Debe configurarse antes de calcular FSR."));
        }

        return configuracion.calcularFSR();
    }

    /**
     * Calcula el Salario Real a partir de un salario base.
     * 
     * @param salarioBase El salario base (cuota diaria)
     * @param recurso El recurso de tipo MANO_OBRA
     * @param proyectoId El ID del proyecto (opcional)
     * @return El salario real calculado
     */
    public BigDecimal calcularSalarioReal(BigDecimal salarioBase, Recurso recurso, UUID proyectoId) {
        if (salarioBase == null || salarioBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El salario base debe ser positivo");
        }

        BigDecimal fsr = calcularFSR(recurso, proyectoId);
        return salarioBase.multiply(fsr).setScale(4, java.math.RoundingMode.HALF_UP);
    }
}
