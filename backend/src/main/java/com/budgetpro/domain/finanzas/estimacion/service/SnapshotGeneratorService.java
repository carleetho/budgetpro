package com.budgetpro.domain.finanzas.estimacion.service;

import com.budgetpro.domain.finanzas.estimacion.model.Estimacion;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshot;
import com.budgetpro.domain.shared.port.out.JsonSerializerPort;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SnapshotGeneratorService {

    private final JsonSerializerPort jsonSerializer;

    public SnapshotGeneratorService(JsonSerializerPort jsonSerializer) {
        this.jsonSerializer = Objects.requireNonNull(jsonSerializer, "El serializador JSON no puede ser nulo");
    }

    public EstimacionSnapshot generarSnapshot(Estimacion estimacion) {
        Objects.requireNonNull(estimacion, "La estimación no puede ser nula");

        String itemsJson = generarItemsJson(estimacion);
        String totalesJson = generarTotalesJson(estimacion);
        String metadataJson = generarMetadataJson(estimacion);

        return EstimacionSnapshot.crear(estimacion.getId(), itemsJson, totalesJson, metadataJson);
    }

    private String generarItemsJson(Estimacion estimacion) {
        // Delegate to shared serializer to convert list of items to JSON string
        return jsonSerializer.toJson(estimacion.getItems());
    }

    private String generarTotalesJson(Estimacion estimacion) {
        Map<String, Object> totalesMap = new HashMap<>();
        totalesMap.put("totalEstimado", estimacion.calcularTotalEstimado().getValueForPersistence());
        totalesMap.put("retencion", estimacion.calcularRetencion().getValueForPersistence());
        totalesMap.put("totalPagar", estimacion.calcularTotalPagar().getValueForPersistence());
        totalesMap.put("porcentajeRetencion", estimacion.getRetencionPorcentaje().getValue());

        return jsonSerializer.toJson(totalesMap);
    }

    private String generarMetadataJson(Estimacion estimacion) {
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("periodoInicio", estimacion.getPeriodo().getFechaInicio());
        metadataMap.put("periodoFin", estimacion.getPeriodo().getFechaFin());
        metadataMap.put("presupuestoId", estimacion.getPresupuestoId().getValue());
        metadataMap.put("generatedAt", LocalDateTime.now());

        return jsonSerializer.toJson(metadataMap);
    }
}
