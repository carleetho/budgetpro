package com.budgetpro.application.ordencambio;

import com.budgetpro.domain.finanzas.ordencambio.exception.OrdenCambioException;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambio;
import com.budgetpro.domain.finanzas.ordencambio.model.OrdenCambioId;
import com.budgetpro.domain.finanzas.ordencambio.port.OrdenCambioRepository;
import com.budgetpro.domain.finanzas.ordencambio.service.PresupuestoVersionService;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AprobarOrdenCambioUseCase {

        private final OrdenCambioRepository ordenCambioRepository;
        private final PresupuestoRepository presupuestoRepository;
        private final PresupuestoVersionService presupuestoVersionService;
        private final IntegrityHashService integrityHashService;

        public AprobarOrdenCambioUseCase(OrdenCambioRepository ordenCambioRepository,
                        PresupuestoRepository presupuestoRepository,
                        PresupuestoVersionService presupuestoVersionService,
                        IntegrityHashService integrityHashService) {
                this.ordenCambioRepository = ordenCambioRepository;
                this.presupuestoRepository = presupuestoRepository;
                this.presupuestoVersionService = presupuestoVersionService;
                this.integrityHashService = integrityHashService;
        }

        @Transactional
        public OrdenCambio ejecutar(UUID ordenCambioId, UUID aprobadorId, Integer impactoCronogramaDias,
                        boolean requiereAdenda, String numeroAdenda) {

                OrdenCambio orden = ordenCambioRepository.findById(OrdenCambioId.from(ordenCambioId)).orElseThrow(
                                () -> new OrdenCambioException("Orden de cambio no encontrada: " + ordenCambioId));

                Presupuesto presupuestoBase = presupuestoRepository
                                .findActiveByProyectoId(orden.getProyectoId().getValue())
                                .orElseThrow(() -> new OrdenCambioException(
                                                "No se encontró presupuesto activo para el proyecto"));

                // Aprobar orden y generar nueva versión
                Presupuesto nuevaVersion = orden.aprobar(aprobadorId, impactoCronogramaDias, requiereAdenda,
                                numeroAdenda, presupuestoVersionService, presupuestoBase, integrityHashService);

                // Persistir la nueva versión y la orden actualizada
                presupuestoRepository.save(nuevaVersion);
                return ordenCambioRepository.save(orden);
        }
}
