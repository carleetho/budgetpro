package com.budgetpro.infrastructure.rest.controller;

import com.budgetpro.application.produccion.exception.BusinessRuleException;
import com.budgetpro.application.produccion.service.ProduccionService;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.produccion.ReporteProduccionJpaRepository;
import com.budgetpro.infrastructure.rest.dto.produccion.CrearReporteRequest;
import com.budgetpro.infrastructure.rest.dto.produccion.RechazarReporteRequest;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.AuditorAware;
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
    public ResponseEntity<com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse> crearReporte(
            @PathVariable UUID proyectoId,
            @Valid @RequestBody CrearReporteRequest request) {
        ReporteProduccionEntity entity = mapToEntity(proyectoId, request);
        ReporteProduccionEntity saved = produccionService.crearReporte(entity);
        return ResponseEntity
                .created(URI.create("/api/v1/produccion/" + saved.getId()))
                .body(mapToUnifiedResponse(saved));
    }

    /**
     * GET /proyectos/{proyectoId}/produccion
     */
    @GetMapping("/proyectos/{proyectoId}/produccion")
    public ResponseEntity<List<com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse>> listarReportes(
            @PathVariable UUID proyectoId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size <= 0 || size > 200) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }

        var estadoEnum = (estado == null || estado.isBlank())
                ? null
                : com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion.valueOf(estado.trim().toUpperCase());

        List<ReporteProduccionEntity> all = reporteProduccionJpaRepository.findByProyectoId(proyectoId);
        List<ReporteProduccionEntity> filtered = all.stream()
                .filter(r -> estadoEnum == null || r.getEstado() == estadoEnum)
                .filter(r -> startDate == null || (r.getFechaReporte() != null && !r.getFechaReporte().isBefore(startDate)))
                .filter(r -> endDate == null || (r.getFechaReporte() != null && !r.getFechaReporte().isAfter(endDate)))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());

        return ResponseEntity.ok(filtered.subList(from, to).stream().map(this::mapToUnifiedResponse).toList());
    }

    /**
     * GET /produccion/{id}
     */
    @GetMapping("/produccion/{id}")
    public ResponseEntity<com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse> obtenerDetalle(@PathVariable UUID id) {
        ReporteProduccionEntity reporte = reporteProduccionJpaRepository.findWithDetallesById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reporte no encontrado."));
        return ResponseEntity.ok(mapToUnifiedResponse(reporte));
    }

    /**
     * PATCH /produccion/{id}/aprobar
     */
    @PatchMapping("/produccion/{id}/aprobar")
    public ResponseEntity<com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse> aprobar(@PathVariable UUID id) {
        UUID aprobadorId = auditorAware.getCurrentAuditor().orElse(null);
        if (aprobadorId == null) {
            throw new BusinessRuleException("No se pudo determinar el aprobador actual.");
        }
        ReporteProduccionEntity saved = produccionService.aprobarReporte(id, aprobadorId);
        return ResponseEntity.ok(mapToUnifiedResponse(saved));
    }

    /**
     * PATCH /produccion/{id}/rechazar
     */
    @PatchMapping("/produccion/{id}/rechazar")
    public ResponseEntity<com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse> rechazar(@PathVariable UUID id,
                                                                                                            @Valid @RequestBody RechazarReporteRequest request) {
        UUID aprobadorId = auditorAware.getCurrentAuditor().orElse(null);
        if (aprobadorId == null) {
            throw new BusinessRuleException("No se pudo determinar el aprobador actual.");
        }
        ReporteProduccionEntity saved = produccionService.rechazarReporte(id, aprobadorId, request.motivo());
        return ResponseEntity.ok(mapToUnifiedResponse(saved));
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

    private com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse mapToUnifiedResponse(ReporteProduccionEntity entity) {
        var detalles = entity.getDetalles().stream()
                .map(d -> new com.budgetpro.infrastructure.rest.produccion.dto.DetalleRPCResponse(
                        d.getId(),
                        d.getPartida().getId(),
                        d.getCantidadReportada()
                ))
                .toList();

        return new com.budgetpro.infrastructure.rest.produccion.dto.ReporteProduccionResponse(
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

    // Nota: el endpoint legacy ya no expone "nombre"; el contrato unificado devuelve IDs.
}
