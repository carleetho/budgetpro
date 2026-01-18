package com.budgetpro.infrastructure.rest.controller;

import com.budgetpro.application.produccion.exception.BusinessRuleException;
import com.budgetpro.application.produccion.service.ProduccionService;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.produccion.ReporteProduccionJpaRepository;
import com.budgetpro.infrastructure.rest.dto.produccion.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para Reportes de Producción (RPC).
 */
@RestController
@RequestMapping("/api/v1")
@SuppressWarnings("null")
public class ProduccionController {

    private final ProduccionService produccionService;
    private final ReporteProduccionJpaRepository reporteProduccionJpaRepository;
    private final PartidaJpaRepository partidaJpaRepository;
    private final AuditorAware<UUID> auditorAware;

    public ProduccionController(ProduccionService produccionService,
                                ReporteProduccionJpaRepository reporteProduccionJpaRepository,
                                PartidaJpaRepository partidaJpaRepository,
                                AuditorAware<UUID> auditorAware) {
        this.produccionService = produccionService;
        this.reporteProduccionJpaRepository = reporteProduccionJpaRepository;
        this.partidaJpaRepository = partidaJpaRepository;
        this.auditorAware = auditorAware;
    }

    /**
     * POST /proyectos/{proyectoId}/produccion
     */
    @PostMapping("/proyectos/{proyectoId}/produccion")
    public ResponseEntity<ReporteResponse> crearReporte(@PathVariable UUID proyectoId,
                                                        @Valid @RequestBody CrearReporteRequest request) {
        ReporteProduccionEntity entity = mapToEntity(proyectoId, request);
        ReporteProduccionEntity saved = produccionService.crearReporte(entity);
        return ResponseEntity
                .created(URI.create("/api/v1/produccion/" + saved.getId()))
                .body(mapToResponse(saved));
    }

    /**
     * GET /proyectos/{proyectoId}/produccion
     */
    @GetMapping("/proyectos/{proyectoId}/produccion")
    public ResponseEntity<List<ReporteResumenResponse>> listarReportes(@PathVariable UUID proyectoId) {
        List<ReporteResumenResponse> resumen = reporteProduccionJpaRepository.findByProyectoId(proyectoId)
                .stream()
                .map(this::mapToResumen)
                .toList();
        return ResponseEntity.ok(resumen);
    }

    /**
     * GET /produccion/{id}
     */
    @GetMapping("/produccion/{id}")
    public ResponseEntity<ReporteResponse> obtenerDetalle(@PathVariable UUID id) {
        ReporteProduccionEntity reporte = reporteProduccionJpaRepository.findWithDetallesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));
        return ResponseEntity.ok(mapToResponse(reporte));
    }

    /**
     * PATCH /produccion/{id}/aprobar
     */
    @PatchMapping("/produccion/{id}/aprobar")
    public ResponseEntity<ReporteResponse> aprobar(@PathVariable UUID id) {
        UUID aprobadorId = auditorAware.getCurrentAuditor().orElse(null);
        if (aprobadorId == null) {
            throw new BusinessRuleException("No se pudo determinar el aprobador actual.");
        }
        ReporteProduccionEntity saved = produccionService.aprobarReporte(id, aprobadorId);
        return ResponseEntity.ok(mapToResponse(saved));
    }

    /**
     * PATCH /produccion/{id}/rechazar
     */
    @PatchMapping("/produccion/{id}/rechazar")
    public ResponseEntity<ReporteResponse> rechazar(@PathVariable UUID id,
                                                    @Valid @RequestBody RechazarReporteRequest request) {
        UUID aprobadorId = auditorAware.getCurrentAuditor().orElse(null);
        if (aprobadorId == null) {
            throw new BusinessRuleException("No se pudo determinar el aprobador actual.");
        }
        ReporteProduccionEntity saved = produccionService.rechazarReporte(id, aprobadorId, request.motivo());
        return ResponseEntity.ok(mapToResponse(saved));
    }

    private ReporteProduccionEntity mapToEntity(UUID proyectoId, CrearReporteRequest request) {
        ReporteProduccionEntity entity = new ReporteProduccionEntity();
        entity.setId(UUID.randomUUID());
        entity.setFechaReporte(request.fechaReporte());
        entity.setResponsableId(auditorAware.getCurrentAuditor()
                .orElseThrow(() -> new BusinessRuleException("No se pudo determinar el responsable actual.")));

        List<DetalleRPCEntity> detalles = request.items().stream().map(item -> {
            PartidaEntity partida = partidaJpaRepository.findById(item.partidaId())
                    .orElseThrow(() -> new EntityNotFoundException("Partida no encontrada."));
            if (!proyectoId.equals(partida.getPresupuesto().getProyectoId())) {
                throw new BusinessRuleException("La partida no pertenece al proyecto indicado.");
            }
            DetalleRPCEntity d = new DetalleRPCEntity();
            d.setId(UUID.randomUUID());
            d.setReporteProduccion(entity);
            d.setPartida(partida);
            d.setCantidadReportada(item.cantidad());
            return d;
        }).toList();

        entity.setDetalles(detalles);
        return entity;
    }

    private ReporteResumenResponse mapToResumen(ReporteProduccionEntity entity) {
        return new ReporteResumenResponse(
                entity.getId(),
                entity.getFechaReporte(),
                entity.getEstado() != null ? entity.getEstado().name() : null,
                entity.getResponsableId(),
                resolveNombre(entity.getResponsableId()),
                entity.getAprobadorId(),
                resolveNombre(entity.getAprobadorId()),
                entity.getDetalles() != null ? entity.getDetalles().size() : 0
        );
    }

    private ReporteResponse mapToResponse(ReporteProduccionEntity entity) {
        List<DetalleItemResponse> detalles = entity.getDetalles().stream()
                .map(d -> new DetalleItemResponse(
                        d.getPartida().getId(),
                        d.getPartida().getDescripcion(),
                        d.getCantidadReportada()
                ))
                .toList();

        return new ReporteResponse(
                entity.getId(),
                entity.getFechaReporte(),
                entity.getEstado() != null ? entity.getEstado().name() : null,
                entity.getResponsableId(),
                resolveNombre(entity.getResponsableId()),
                entity.getAprobadorId(),
                resolveNombre(entity.getAprobadorId()),
                entity.getComentario(),
                entity.getUbicacionGps(),
                detalles
        );
    }

    private String resolveNombre(UUID usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        String shortId = usuarioId.toString().substring(0, 8);
        return "Usuario " + shortId;
    }
}
