package com.budgetpro.domain.finanzas.cronograma.service;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Dominio para generar snapshots del cronograma en formato JSON.
 * 
 * Responsabilidad:
 * - Serializar datos temporales del cronograma a JSON
 * - Capturar fechas, duraciones, secuencia y calendarios
 * - Producir JSON válido y parseable para almacenamiento en JSONB
 * 
 * **Formato de Snapshot:**
 * - fechasJson: Fechas del programa y todas las actividades
 * - duracionesJson: Duraciones del programa y actividades
 * - secuenciaJson: Secuencia y dependencias entre actividades
 * - calendariosJson: Calendarios y restricciones temporales (inicialmente vacío)
 */
public class SnapshotGeneratorService {

    private final ObjectMapper objectMapper;

    public SnapshotGeneratorService() {
        this.objectMapper = new ObjectMapper();
        // Configurar ObjectMapper para manejar LocalDate correctamente
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * Genera el JSON de fechas del cronograma.
     * 
     * Incluye:
     * - Fechas del programa (fechaInicio, fechaFinEstimada)
     * - Fechas de todas las actividades (id, fechaInicio, fechaFin)
     * 
     * @param programaObra El programa de obra
     * @param actividades Lista de actividades programadas
     * @return JSON string con datos de fechas
     * @throws RuntimeException si hay error en la serialización
     */
    public String generarFechasJson(ProgramaObra programaObra, List<ActividadProgramada> actividades) {
        Objects.requireNonNull(programaObra, "El programa de obra no puede ser nulo");
        Objects.requireNonNull(actividades, "La lista de actividades no puede ser nula");

        Map<String, Object> fechasData = new HashMap<>();
        
        // Fechas del programa
        Map<String, Object> programaFechas = new HashMap<>();
        programaFechas.put("fechaInicio", programaObra.getFechaInicio() != null ? programaObra.getFechaInicio().toString() : null);
        programaFechas.put("fechaFinEstimada", programaObra.getFechaFinEstimada() != null ? programaObra.getFechaFinEstimada().toString() : null);
        fechasData.put("programa", programaFechas);
        
        // Fechas de actividades
        List<Map<String, Object>> actividadesFechas = new ArrayList<>();
        for (ActividadProgramada actividad : actividades) {
            Map<String, Object> actividadFechas = new HashMap<>();
            actividadFechas.put("id", actividad.getId().getValue().toString());
            actividadFechas.put("partidaId", actividad.getPartidaId().toString());
            actividadFechas.put("fechaInicio", actividad.getFechaInicio() != null ? actividad.getFechaInicio().toString() : null);
            actividadFechas.put("fechaFin", actividad.getFechaFin() != null ? actividad.getFechaFin().toString() : null);
            actividadesFechas.add(actividadFechas);
        }
        fechasData.put("actividades", actividadesFechas);

        return serializarAJson(fechasData);
    }

    /**
     * Genera el JSON de duraciones del cronograma.
     * 
     * Incluye:
     * - Duración total del programa (duracionTotalDias)
     * - Duraciones de todas las actividades (id, duracionDias)
     * 
     * @param programaObra El programa de obra
     * @param actividades Lista de actividades programadas
     * @return JSON string con datos de duraciones
     * @throws RuntimeException si hay error en la serialización
     */
    public String generarDuracionesJson(ProgramaObra programaObra, List<ActividadProgramada> actividades) {
        Objects.requireNonNull(programaObra, "El programa de obra no puede ser nulo");
        Objects.requireNonNull(actividades, "La lista de actividades no puede ser nula");

        Map<String, Object> duracionesData = new HashMap<>();
        
        // Duración del programa
        duracionesData.put("duracionTotalDias", programaObra.getDuracionTotalDias());
        
        // Duraciones de actividades
        List<Map<String, Object>> actividadesDuraciones = new ArrayList<>();
        for (ActividadProgramada actividad : actividades) {
            Map<String, Object> actividadDuracion = new HashMap<>();
            actividadDuracion.put("id", actividad.getId().getValue().toString());
            actividadDuracion.put("partidaId", actividad.getPartidaId().toString());
            actividadDuracion.put("duracionDias", actividad.getDuracionDias());
            actividadesDuraciones.add(actividadDuracion);
        }
        duracionesData.put("actividades", actividadesDuraciones);

        return serializarAJson(duracionesData);
    }

    /**
     * Genera el JSON de secuencia y dependencias del cronograma.
     * 
     * Incluye:
     * - Secuencia de actividades (orden)
     * - Dependencias entre actividades (predecesoras)
     * 
     * @param actividades Lista de actividades programadas
     * @return JSON string con datos de secuencia y dependencias
     * @throws RuntimeException si hay error en la serialización
     */
    public String generarSecuenciaJson(List<ActividadProgramada> actividades) {
        Objects.requireNonNull(actividades, "La lista de actividades no puede ser nula");

        Map<String, Object> secuenciaData = new HashMap<>();
        
        // Secuencia y dependencias de actividades
        List<Map<String, Object>> actividadesSecuencia = new ArrayList<>();
        for (ActividadProgramada actividad : actividades) {
            Map<String, Object> actividadSecuencia = new HashMap<>();
            actividadSecuencia.put("id", actividad.getId().getValue().toString());
            actividadSecuencia.put("partidaId", actividad.getPartidaId().toString());
            
            // Predecesoras (dependencias)
            List<String> predecesorasIds = new ArrayList<>();
            for (UUID predecesoraId : actividad.getPredecesoras()) {
                predecesorasIds.add(predecesoraId.toString());
            }
            actividadSecuencia.put("predecesoras", predecesorasIds);
            
            actividadesSecuencia.add(actividadSecuencia);
        }
        secuenciaData.put("actividades", actividadesSecuencia);

        return serializarAJson(secuenciaData);
    }

    /**
     * Genera el JSON de calendarios y restricciones temporales.
     * 
     * Inicialmente retorna un JSON vacío, ya que los calendarios no están implementados aún.
     * Este campo está preparado para futuras extensiones.
     * 
     * @return JSON string con datos de calendarios (inicialmente vacío)
     * @throws RuntimeException si hay error en la serialización
     */
    public String generarCalendariosJson() {
        Map<String, Object> calendariosData = new HashMap<>();
        calendariosData.put("calendarios", new ArrayList<>());
        calendariosData.put("diasFestivos", new ArrayList<>());
        calendariosData.put("restricciones", new ArrayList<>());
        
        return serializarAJson(calendariosData);
    }

    /**
     * Serializa un objeto a JSON string.
     * 
     * @param data El objeto a serializar
     * @return JSON string
     * @throws RuntimeException si hay error en la serialización
     */
    private String serializarAJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar datos del snapshot a JSON: " + e.getMessage(), e);
        }
    }
}
