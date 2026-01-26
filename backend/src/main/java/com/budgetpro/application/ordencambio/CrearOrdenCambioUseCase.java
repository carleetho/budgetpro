package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.model.OrigenOrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.port.NumeroOrdenGenerator;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CrearOrdenCambioUseCase {

    private final OrdenCambioRepository ordenCambioRepository;
    private final NumeroOrdenGenerator numeroOrdenGenerator;
    private final ProyectoRepository proyectoRepository;

    public CrearOrdenCambioUseCase(OrdenCambioRepository ordenCambioRepository,
            NumeroOrdenGenerator numeroOrdenGenerator, ProyectoRepository proyectoRepository) {
        this.ordenCambioRepository = ordenCambioRepository;
        this.numeroOrdenGenerator = numeroOrdenGenerator;
        this.proyectoRepository = proyectoRepository;
    }

    @Transactional
    public OrdenCambio ejecutar(UUID proyectoId, OrigenOrdenCambio origen, String descripcion, UUID solicitanteId) {
        // Validar que el proyecto existe
        if (!proyectoRepository.existsById(ProyectoId.from(proyectoId))) {
            throw new IllegalArgumentException("El proyecto no existe: " + proyectoId);
        }

        // Generar número correlativo
        String numeroOrden = numeroOrdenGenerator.generarNumeroOrden(proyectoId);

        // Crear el agregado
        OrdenCambio ordenCambio = OrdenCambio.crear(OrdenCambioId.nuevo(), ProyectoId.from(proyectoId), numeroOrden,
                origen, descripcion, solicitanteId);

        // Persistir
        return ordenCambioRepository.save(ordenCambio);
    }
}
