package com.budgetpro.application.sobrecosto.usecase;

import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.sobrecosto.dto.AnalisisSobrecostoResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarSobrecostoCommand;
import com.budgetpro.application.sobrecosto.port.in.ConfigurarSobrecostoUseCase;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecosto;
import com.budgetpro.domain.finanzas.sobrecosto.model.AnalisisSobrecostoId;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.AnalisisSobrecostoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Implementación del caso de uso para configurar el análisis de sobrecosto.
 */
@Service
public class ConfigurarSobrecostoUseCaseImpl implements ConfigurarSobrecostoUseCase {

    private final PresupuestoRepository presupuestoRepository;
    private final AnalisisSobrecostoRepository analisisSobrecostoRepository;

    public ConfigurarSobrecostoUseCaseImpl(PresupuestoRepository presupuestoRepository,
                                          AnalisisSobrecostoRepository analisisSobrecostoRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.analisisSobrecostoRepository = analisisSobrecostoRepository;
    }

    @Override
    @Transactional
    public AnalisisSobrecostoResponse configurar(ConfigurarSobrecostoCommand command) {
        // 1. Validar que el presupuesto existe
        Presupuesto presupuesto = presupuestoRepository.findById(PresupuestoId.from(command.presupuestoId()))
                .orElseThrow(() -> new PresupuestoNoEncontradoException(command.presupuestoId()));

        // 2. Buscar o crear el análisis de sobrecosto
        AnalisisSobrecosto analisis = analisisSobrecostoRepository.findByPresupuestoId(command.presupuestoId())
                .orElseGet(() -> {
                    // Crear nuevo análisis
                    AnalisisSobrecostoId id = AnalisisSobrecostoId.nuevo();
                    return AnalisisSobrecosto.crear(id, command.presupuestoId());
                });

        // 3. Actualizar los porcentajes
        analisis.actualizarIndirectos(
                command.porcentajeIndirectosOficinaCentral() != null ? command.porcentajeIndirectosOficinaCentral() : BigDecimal.ZERO,
                command.porcentajeIndirectosOficinaCampo() != null ? command.porcentajeIndirectosOficinaCampo() : BigDecimal.ZERO
        );
        
        analisis.actualizarFinanciamiento(
                command.porcentajeFinanciamiento() != null ? command.porcentajeFinanciamiento() : BigDecimal.ZERO,
                command.financiamientoCalculado() != null ? command.financiamientoCalculado() : false
        );
        
        analisis.actualizarUtilidad(
                command.porcentajeUtilidad() != null ? command.porcentajeUtilidad() : BigDecimal.ZERO
        );
        
        analisis.actualizarCargosAdicionales(
                command.porcentajeFianzas() != null ? command.porcentajeFianzas() : BigDecimal.ZERO,
                command.porcentajeImpuestosReflejables() != null ? command.porcentajeImpuestosReflejables() : BigDecimal.ZERO
        );

        // 4. Persistir
        analisisSobrecostoRepository.save(analisis);

        // 5. Retornar respuesta
        return new AnalisisSobrecostoResponse(
            analisis.getId().getValue(),
            analisis.getPresupuestoId(),
            analisis.getPorcentajeIndirectosOficinaCentral(),
            analisis.getPorcentajeIndirectosOficinaCampo(),
            analisis.getPorcentajeIndirectosTotal(),
            analisis.getPorcentajeFinanciamiento(),
            analisis.getFinanciamientoCalculado(),
            analisis.getPorcentajeUtilidad(),
            analisis.getPorcentajeFianzas(),
            analisis.getPorcentajeImpuestosReflejables(),
            analisis.getPorcentajeCargosAdicionalesTotal(),
            analisis.getVersion().intValue()
        );
    }
}
