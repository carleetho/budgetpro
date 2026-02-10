package com.budgetpro.application.produccion.validation;

import com.budgetpro.application.produccion.exception.BusinessRuleException;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.DetalleRPCEntity;
import com.budgetpro.infrastructure.persistence.entity.produccion.EstadoReporteProduccion;
import com.budgetpro.infrastructure.persistence.entity.produccion.ReporteProduccionEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.produccion.DetalleRPCJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Validador de reglas de negocio para Reportes de Producción (RPC).
 */
@Component
@SuppressWarnings("null")
public class ProduccionValidator {

    private static final String MENSAJE_EXCESO =
            // REGLA-004
            "La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio.";

    private final PartidaJpaRepository partidaJpaRepository;
    private final ProyectoJpaRepository proyectoJpaRepository;
    private final DetalleRPCJpaRepository detalleRPCJpaRepository;

    public ProduccionValidator(PartidaJpaRepository partidaJpaRepository,
                               ProyectoJpaRepository proyectoJpaRepository,
                               DetalleRPCJpaRepository detalleRPCJpaRepository) {
        this.partidaJpaRepository = partidaJpaRepository;
        this.proyectoJpaRepository = proyectoJpaRepository;
        this.detalleRPCJpaRepository = detalleRPCJpaRepository;
    }

    public void validarEditable(ReporteProduccionEntity reporte) {
        if (reporte.getEstado() == EstadoReporteProduccion.APROBADO
                || reporte.getEstado() == EstadoReporteProduccion.RECHAZADO) {
            throw new BusinessRuleException(
                    // REGLA-001
                    "Un reporte aprobado es inmutable. Debe crear una Nota de Crédito o un Reporte Deductivo para corregir."
            );
        }
    }

    public void validarFechaNoFutura(LocalDate fechaReporte) {
        if (fechaReporte == null) {
            throw new BusinessRuleException("La fecha del reporte es obligatoria.");
        }
        if (fechaReporte.isAfter(LocalDate.now())) {
            // REGLA-002
            throw new BusinessRuleException("La fecha del reporte no puede ser futura.");
        }
    }

    public void validarProyectoEnEjecucion(UUID partidaId) {
        PartidaEntity partida = partidaJpaRepository.findById(partidaId)
                .orElseThrow(() -> new BusinessRuleException("Partida no encontrada."));

        UUID proyectoId = partida.getPresupuesto() != null
                ? partida.getPresupuesto().getProyectoId()
                : null;

        if (proyectoId == null) {
            throw new BusinessRuleException("Proyecto no encontrado para la partida.");
        }

        ProyectoEntity proyecto = proyectoJpaRepository.findById(proyectoId)
                .orElseThrow(() -> new BusinessRuleException("Proyecto no encontrado."));

        // REGLA-003
        if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
            throw new BusinessRuleException("No se puede reportar avance en un proyecto que no está en ACTIVO.");
        }
    }

    public void validarNoExcesoMetrado(UUID partidaId, BigDecimal cantidadNueva, UUID reporteIdExcluir) {
        if (cantidadNueva == null) {
            throw new BusinessRuleException("La cantidad reportada es obligatoria.");
        }

        PartidaEntity partida = partidaJpaRepository.findById(partidaId)
                .orElseThrow(() -> new BusinessRuleException("Partida no encontrada."));

        BigDecimal metradoVigente = partida.getMetradoVigente();
        if (metradoVigente == null) {
            throw new BusinessRuleException("La partida no tiene metrado vigente.");
        }

        BigDecimal acumuladoAprobado = detalleRPCJpaRepository.sumarCantidadAprobadaPorPartida(
                partidaId,
                EstadoReporteProduccion.APROBADO,
                reporteIdExcluir
        );

        BigDecimal avanceTotal = acumuladoAprobado.add(cantidadNueva);
        if (avanceTotal.compareTo(metradoVigente) > 0) {
            throw new BusinessRuleException(MENSAJE_EXCESO);
        }
    }

    public void validarDetalle(DetalleRPCEntity detalle, UUID reporteIdExcluir) {
        Objects.requireNonNull(detalle, "Detalle RPC es obligatorio.");
        UUID partidaId = detalle.getPartida().getId();
        validarProyectoEnEjecucion(partidaId);
        validarNoExcesoMetrado(partidaId, detalle.getCantidadReportada(), reporteIdExcluir);
    }
}
