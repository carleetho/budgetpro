package com.budgetpro.infrastructure.rest.produccion.controller;

import com.budgetpro.application.produccion.service.ProduccionService;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.rest.produccion.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para Reportes de Producción (RPC).
 */
@RestController
@RequestMapping("/api/v1/produccion/reportes")
@SuppressWarnings("null")
public class ReporteProduccionController {

    private final ProduccionService produccionService;
    private final PartidaJpaRepository partidaJpaRepository;

    public ReporteProduccionController(ProduccionService produccionService,
                                       PartidaJpaRepository partidaJpaRepository) {
        this.produccionService = produccionService;
        this.partidaJpaRepository = partidaJpaRepository;
    }

    @PostMapping
    public ResponseEntity<ReporteProduccionResponse> crear(@Valid @RequestBody CrearReporteProduccionRequest request) {
        ReporteProduccionEntity entity = mapToEntity(request);
        ReporteProduccionEntity saved = produccionService.crearReporte(entity);
        return ResponseEntity
                .created(URI.create("/api/v1/produccion/reportes/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    @PutMapping("/{reporteId}")
    public ResponseEntity<ReporteProduccionResponse> actualizar(@PathVariable UUID reporteId,
                                                                @Valid @RequestBody ActualizarReporteProduccionRequest request) {
        ReporteProduccionEntity entity = mapToEntity(request);
        ReporteProduccionEntity saved = produccionService.actualizarReporte(reporteId, entity);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PostMapping("/{reporteId}/aprobar")
    public ResponseEntity<ReporteProduccionResponse> aprobar(@PathVariable UUID reporteId,
                                                             @Valid @RequestBody AprobarReporteRequest request) {
        ReporteProduccionEntity saved = produccionService.aprobarReporte(reporteId, request.aprobadorId());
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @PostMapping("/{reporteId}/rechazar")
    public ResponseEntity<ReporteProduccionResponse> rechazar(@PathVariable UUID reporteId,
                                                              @Valid @RequestBody RechazarReporteRequest request) {
        ReporteProduccionEntity saved = produccionService.rechazarReporte(
                reporteId,
                request.aprobadorId(),
                request.motivo()
        );
        return ResponseEntity.ok(mapToResponse(saved));
    }

    @DeleteMapping("/{reporteId}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID reporteId) {
        produccionService.eliminarReporte(reporteId);
        return ResponseEntity.noContent().build();
    }

    private ReporteProduccionEntity mapToEntity(CrearReporteProduccionRequest request) {
        ReporteProduccionEntity entity = new ReporteProduccionEntity();
        entity.setId(UUID.randomUUID());
        entity.setFechaReporte(request.fechaReporte());
        entity.setResponsableId(request.responsableId());
        entity.setComentario(request.comentario());
        entity.setUbicacionGps(request.ubicacionGps());

        List<DetalleRPCEntity> detalles = request.detalles().stream().map(det -> {
            DetalleRPCEntity d = new DetalleRPCEntity();
            d.setId(UUID.randomUUID());
            d.setReporteProduccion(entity);
            PartidaEntity partida = partidaJpaRepository.findById(det.partidaId())
                    .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
            d.setPartida(partida);
            d.setCantidadReportada(det.cantidadReportada());
            return d;
        }).toList();
        entity.setDetalles(detalles);
        return entity;
    }

    private ReporteProduccionEntity mapToEntity(ActualizarReporteProduccionRequest request) {
        ReporteProduccionEntity entity = new ReporteProduccionEntity();
        entity.setFechaReporte(request.fechaReporte());
        entity.setResponsableId(request.responsableId());
        entity.setComentario(request.comentario());
        entity.setUbicacionGps(request.ubicacionGps());

        List<DetalleRPCEntity> detalles = request.detalles().stream().map(det -> {
            DetalleRPCEntity d = new DetalleRPCEntity();
            d.setId(UUID.randomUUID());
            PartidaEntity partida = partidaJpaRepository.findById(det.partidaId())
                    .orElseThrow(() -> new IllegalArgumentException("Partida no encontrada"));
            d.setPartida(partida);
            d.setCantidadReportada(det.cantidadReportada());
            return d;
        }).toList();
        entity.setDetalles(detalles);
        return entity;
    }

    private ReporteProduccionResponse mapToResponse(ReporteProduccionEntity entity) {
        List<DetalleRPCResponse> detalles = entity.getDetalles().stream()
                .map(d -> new DetalleRPCResponse(d.getId(), d.getPartida().getId(), d.getCantidadReportada()))
                .toList();

        return new ReporteProduccionResponse(
                entity.getId(),
                entity.getFechaReporte(),
                entity.getResponsableId(),
                entity.getAprobadorId(),
                entity.getEstado() != null ? entity.getEstado().name() : null,
                entity.getComentario(),
                entity.getUbicacionGps(),
                detalles
        );
    }
}
