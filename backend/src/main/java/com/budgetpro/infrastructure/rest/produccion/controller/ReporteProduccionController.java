package com.budgetpro.infrastructure.rest.produccion.controller;

import com.budgetpro.application.produccion.service.ProduccionService;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.produccion.ReporteProduccionJpaRepository;
import com.budgetpro.infrastructure.rest.produccion.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
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
    private final ReporteProduccionJpaRepository reporteProduccionJpaRepository;

    public ReporteProduccionController(ProduccionService produccionService,
                                       PartidaJpaRepository partidaJpaRepository,
                                       ReporteProduccionJpaRepository reporteProduccionJpaRepository) {
        this.produccionService = produccionService;
        this.partidaJpaRepository = partidaJpaRepository;
        this.reporteProduccionJpaRepository = reporteProduccionJpaRepository;
    }

    @GetMapping("/{reporteId}")
    public ResponseEntity<ReporteProduccionResponse> obtener(@PathVariable UUID reporteId) {
        ReporteProduccionEntity reporte = reporteProduccionJpaRepository.findWithDetallesById(reporteId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Reporte no encontrado."));
        return ResponseEntity.ok(mapToResponse(reporte));
    }

    /**
     * Listado con filtros opcionales (estado y rango de fechas).
     * Si no se pasa proyectoId, retorna lista vacía.
     */
    @GetMapping
    public ResponseEntity<List<ReporteProduccionResponse>> listar(
            @RequestParam(required = false) UUID proyectoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (proyectoId == null) {
            return ResponseEntity.ok(List.of());
        }
        if (page < 0 || size <= 0 || size > 200) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }

        final EstadoReporteProduccion estadoEnum = (estado == null || estado.isBlank())
                ? null
                : EstadoReporteProduccion.valueOf(estado.trim().toUpperCase());

        List<ReporteProduccionEntity> all = reporteProduccionJpaRepository.findByProyectoId(proyectoId);

        List<ReporteProduccionEntity> filtered = all.stream()
                .filter(r -> estadoEnum == null || r.getEstado() == estadoEnum)
                .filter(r -> startDate == null || (r.getFechaReporte() != null && !r.getFechaReporte().isBefore(startDate)))
                .filter(r -> endDate == null || (r.getFechaReporte() != null && !r.getFechaReporte().isAfter(endDate)))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());

        return ResponseEntity.ok(filtered.subList(from, to).stream().map(this::mapToResponse).toList());
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
