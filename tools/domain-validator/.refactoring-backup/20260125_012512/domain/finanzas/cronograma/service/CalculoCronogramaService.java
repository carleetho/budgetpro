package com.budgetpro.domain.finanzas.cronograma.service;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de Dominio para calcular la Ruta Crítica y duración del cronograma.
 * 
 * Implementa lógica básica de Ruta Crítica (Simplificada) según metodología de Suárez Salazar (Cap. 4).
 * 
 * Responsabilidad:
 * - Calcular la duración total del programa basándose en las actividades
 * - Determinar la fecha de fin más tardía de todas las actividades
 * - Calcular la duración en meses para el cálculo de financiamiento
 * 
 * No persiste, solo calcula.
 */
public class CalculoCronogramaService {

    /**
     * Calcula la duración total del programa basándose en las actividades.
     * 
     * La duración total es la diferencia entre la fecha de inicio más temprana
     * y la fecha de fin más tardía de todas las actividades.
     * 
     * @param programaObra El programa de obra
     * @param actividades Lista de actividades programadas
     * @return La duración total en días, o null si no hay actividades con fechas
     */
    public Integer calcularDuracionTotal(ProgramaObra programaObra, List<ActividadProgramada> actividades) {
        if (actividades == null || actividades.isEmpty()) {
            return programaObra.getDuracionTotalDias();
        }

        // Encontrar la fecha de inicio más temprana
        LocalDate fechaInicioMasTemprana = actividades.stream()
                .map(ActividadProgramada::getFechaInicio)
                .filter(java.util.Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(programaObra.getFechaInicio());

        // Si el programa tiene fecha de inicio, usarla como referencia
        if (programaObra.getFechaInicio() != null) {
            if (fechaInicioMasTemprana.isBefore(programaObra.getFechaInicio())) {
                fechaInicioMasTemprana = programaObra.getFechaInicio();
            }
        }

        // Encontrar la fecha de fin más tardía
        LocalDate fechaFinMasTardia = actividades.stream()
                .map(ActividadProgramada::getFechaFin)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(programaObra.getFechaFinEstimada());

        // Si el programa tiene fecha de fin estimada, comparar
        if (programaObra.getFechaFinEstimada() != null) {
            if (fechaFinMasTardia.isAfter(programaObra.getFechaFinEstimada())) {
                fechaFinMasTardia = programaObra.getFechaFinEstimada();
            }
        }

        if (fechaInicioMasTemprana == null || fechaFinMasTardia == null) {
            return null;
        }

        // Calcular duración en días (incluyendo ambos días)
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicioMasTemprana, fechaFinMasTardia) + 1;
    }

    /**
     * Encuentra la fecha de fin más tardía de todas las actividades.
     * 
     * @param actividades Lista de actividades programadas
     * @return La fecha de fin más tardía, o null si no hay actividades con fechas
     */
    public LocalDate encontrarFechaFinMasTardia(List<ActividadProgramada> actividades) {
        if (actividades == null || actividades.isEmpty()) {
            return null;
        }

        return actividades.stream()
                .map(ActividadProgramada::getFechaFin)
                .filter(java.util.Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * Calcula la duración en meses (redondeado hacia arriba) para el cálculo de financiamiento.
     * 
     * Fórmula: TC (Tiempo de Construcción) = DuraciónTotalDias / 30 (aproximado)
     * 
     * @param duracionTotalDias Duración total en días
     * @return Duración en meses (redondeado hacia arriba)
     */
    public Integer calcularDuracionMeses(Integer duracionTotalDias) {
        if (duracionTotalDias == null || duracionTotalDias <= 0) {
            return null;
        }
        // Redondear hacia arriba: (dias + 29) / 30
        return (duracionTotalDias + 29) / 30;
    }

    /**
     * Calcula la duración en meses a partir de un programa de obra y sus actividades.
     * 
     * Este método es usado por el Motor de Costos (Mov 9) para calcular el Financiamiento.
     * 
     * @param programaObra El programa de obra
     * @param actividades Lista de actividades programadas
     * @return Duración en meses, o null si no se puede calcular
     */
    public Integer calcularDuracionMeses(ProgramaObra programaObra, List<ActividadProgramada> actividades) {
        Integer duracionTotalDias = calcularDuracionTotal(programaObra, actividades);
        return calcularDuracionMeses(duracionTotalDias);
    }

    /**
     * Valida que las dependencias entre actividades sean consistentes.
     * 
     * Verifica que si una actividad B depende de A (Fin-Inicio), entonces:
     * - La fecha de inicio de B debe ser >= fecha de fin de A
     * 
     * @param actividades Lista de actividades programadas
     * @return true si las dependencias son válidas, false en caso contrario
     */
    public boolean validarDependencias(List<ActividadProgramada> actividades) {
        if (actividades == null || actividades.isEmpty()) {
            return true;
        }

        // Crear un mapa de actividades por ID para búsqueda rápida
        java.util.Map<UUID, ActividadProgramada> actividadesPorId = new java.util.HashMap<>();
        for (ActividadProgramada actividad : actividades) {
            actividadesPorId.put(actividad.getId().getValue(), actividad);
        }

        // Validar cada dependencia
        for (ActividadProgramada actividad : actividades) {
            LocalDate fechaInicioActividad = actividad.getFechaInicio();
            if (fechaInicioActividad == null) {
                continue; // Si no tiene fecha, no se puede validar
            }

            for (UUID predecesoraId : actividad.getPredecesoras()) {
                ActividadProgramada predecesora = actividadesPorId.get(predecesoraId);
                if (predecesora == null) {
                    continue; // Predecesora no encontrada (puede estar en otro contexto)
                }

                LocalDate fechaFinPredecesora = predecesora.getFechaFin();
                if (fechaFinPredecesora == null) {
                    continue; // Si no tiene fecha, no se puede validar
                }

                // Validar dependencia Fin-Inicio: fechaInicioActividad >= fechaFinPredecesora
                if (fechaInicioActividad.isBefore(fechaFinPredecesora)) {
                    return false; // Dependencia inválida
                }
            }
        }

        return true;
    }
}
