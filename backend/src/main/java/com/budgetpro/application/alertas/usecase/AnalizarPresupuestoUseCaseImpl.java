package com.budgetpro.application.alertas.usecase;

import com.budgetpro.application.alertas.dto.AnalisisPresupuestoResponse;
import com.budgetpro.application.alertas.port.in.AnalizarPresupuestoUseCase;
import com.budgetpro.domain.finanzas.alertas.model.AlertaParametrica;
import com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto;
import com.budgetpro.domain.finanzas.alertas.port.out.AnalisisPresupuestoRepository;
import com.budgetpro.domain.finanzas.alertas.service.AnalizadorParametricoService;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para analizar presupuestos.
 */
@Service
public class AnalizarPresupuestoUseCaseImpl implements AnalizarPresupuestoUseCase {
    
    private final AnalizadorParametricoService analizadorService;
    private final AnalisisPresupuestoRepository analisisRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final ConsultaDatosAnalisis consultaDatosAnalisis;
    
    public AnalizarPresupuestoUseCaseImpl(
            AnalizadorParametricoService analizadorService,
            AnalisisPresupuestoRepository analisisRepository,
            PresupuestoRepository presupuestoRepository,
            ConsultaDatosAnalisis consultaDatosAnalisis) {
        this.analizadorService = analizadorService;
        this.analisisRepository = analisisRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.consultaDatosAnalisis = consultaDatosAnalisis;
    }
    
    @Override
    @Transactional
    public AnalisisPresupuestoResponse analizar(UUID presupuestoId) {
        // Validar que el presupuesto existe
        presupuestoRepository.findById(PresupuestoId.from(presupuestoId))
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado: " + presupuestoId));
        
        // Consultar datos necesarios para el análisis
        AnalizadorParametricoService.DatosAnalisis datosAnalisis = 
                consultaDatosAnalisis.consultarDatos(presupuestoId);
        
        // Ejecutar análisis
        AnalisisPresupuesto analisis = analizadorService.analizar(presupuestoId, datosAnalisis);
        
        // Persistir análisis
        analisisRepository.guardar(analisis);
        
        // Mapear a DTO de respuesta
        return mapearAResponse(analisis);
    }
    
    private AnalisisPresupuestoResponse mapearAResponse(AnalisisPresupuesto analisis) {
        List<AnalisisPresupuestoResponse.AlertaParametricaResponse> alertasResponse = 
                analisis.getAlertas().stream()
                        .map(this::mapearAlerta)
                        .collect(Collectors.toList());
        
        return new AnalisisPresupuestoResponse(
                analisis.getId(),
                analisis.getPresupuestoId(),
                analisis.getFechaAnalisis(),
                analisis.getTotalAlertas(),
                analisis.getTotalAlertasCriticas(),
                analisis.getTotalAlertasWarning(),
                analisis.getTotalAlertasInfo(),
                alertasResponse
        );
    }
    
    private AnalisisPresupuestoResponse.AlertaParametricaResponse mapearAlerta(AlertaParametrica alerta) {
        return new AnalisisPresupuestoResponse.AlertaParametricaResponse(
                alerta.getId(),
                alerta.getTipoAlerta().name(),
                alerta.getNivel().name(),
                alerta.getPartidaId(),
                alerta.getRecursoId(),
                alerta.getMensaje(),
                alerta.getValorDetectado() != null ? alerta.getValorDetectado().doubleValue() : null,
                alerta.getValorEsperadoMin() != null ? alerta.getValorEsperadoMin().doubleValue() : null,
                alerta.getValorEsperadoMax() != null ? alerta.getValorEsperadoMax().doubleValue() : null,
                alerta.getSugerencia()
        );
    }
    
    /**
     * Interfaz para consultar datos necesarios para el análisis.
     * Se implementará en la capa de infraestructura.
     */
    public interface ConsultaDatosAnalisis {
        AnalizadorParametricoService.DatosAnalisis consultarDatos(UUID presupuestoId);
    }
}
