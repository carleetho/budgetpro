package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.cronograma.service.CronogramaService;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.presupuesto.exception.PresupuestoSinCronogramaException;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Dominio para orquestar operaciones de aprobación del presupuesto
 * con acoplamiento temporal al cronograma.
 * 
 * Responsabilidad:
 * - Orquestar la aprobación del presupuesto
 * - Enforzar el acoplamiento temporal: Presupuesto + Cronograma deben congelarse juntos
 * - Validar prerrequisitos antes de la aprobación
 * - Garantizar consistencia transaccional entre presupuesto y cronograma
 * 
 * **Principio de Baseline:**
 * Cuando un presupuesto se congela (aprobado), el cronograma asociado también
 * debe congelarse simultáneamente. Esto establece el baseline del proyecto:
 * - Presupuesto (CONGELADO) → Baseline financiero
 * - Cronograma (CONGELADO) → Baseline temporal
 * 
 * **Cadena de Dependencias:**
 * Proyecto → Presupuesto (CONGELADO) → Tiempo (CONGELADO)
 * 
 * **Acoplamiento Temporal:**
 * - Si el cronograma no existe, el presupuesto NO puede aprobarse
 * - Si el cronograma no puede congelarse, el presupuesto NO se aprueba (rollback)
 * - Ambas operaciones deben ser atómicas (mismo contexto transaccional)
 * 
 * **Nota sobre Transacciones:**
 * La atomicidad debe manejarse en la capa de aplicación mediante @Transactional.
 * Este servicio asume que todas las operaciones se ejecutan en el mismo contexto transaccional.
 * 
 * Contexto: Presupuesto & Baseline
 */
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final ProgramaObraRepository programaObraRepository;
    private final CronogramaService cronogramaService;
    private final IntegrityHashService integrityHashService;

    public PresupuestoService(
            PresupuestoRepository presupuestoRepository,
            ProgramaObraRepository programaObraRepository,
            CronogramaService cronogramaService,
            IntegrityHashService integrityHashService) {
        this.presupuestoRepository = Objects.requireNonNull(presupuestoRepository, "El repositorio de presupuesto no puede ser nulo");
        this.programaObraRepository = Objects.requireNonNull(programaObraRepository, "El repositorio de programa de obra no puede ser nulo");
        this.cronogramaService = Objects.requireNonNull(cronogramaService, "El servicio de cronograma no puede ser nulo");
        this.integrityHashService = Objects.requireNonNull(integrityHashService, "El servicio de hash de integridad no puede ser nulo");
    }

    /**
     * Aprueba el presupuesto y congela simultáneamente el cronograma asociado.
     * 
     * Este método implementa el acoplamiento temporal que establece el baseline del proyecto:
     * 
     * 1. Valida que existe un ProgramaObra para el proyecto
     * 2. Aprueba el presupuesto (genera hashes criptográficos)
     * 3. Congela el cronograma y genera snapshot (baseline temporal)
     * 4. Persiste ambos estados
     * 
     * **Validaciones:**
     * - El presupuesto debe existir
     * - El presupuesto debe estar en estado BORRADOR
     * - Debe existir un ProgramaObra para el proyecto
     * - El ProgramaObra debe tener fechas válidas
     * 
     * **Acoplamiento Temporal:**
     * Si cualquiera de las operaciones falla (presupuesto o cronograma),
     * toda la transacción debe hacer rollback. No se permite estado parcial.
     * 
     * **Nota sobre Transacciones:**
     * Este método debe ejecutarse dentro de un contexto transaccional (@Transactional)
     * en la capa de aplicación para garantizar atomicidad.
     * 
     * @param presupuestoId ID del presupuesto a aprobar
     * @param approvedBy ID del usuario que aprueba el presupuesto
     * @return El snapshot del cronograma generado durante el congelamiento
     * @throws IllegalArgumentException si algún parámetro es nulo
     * @throws IllegalStateException si el presupuesto no existe o no está en estado BORRADOR
     * @throws PresupuestoSinCronogramaException si no existe ProgramaObra para el proyecto
     * @throws IllegalStateException si el cronograma no puede congelarse (fechas inválidas, etc.)
     */
    public CronogramaSnapshot aprobar(PresupuestoId presupuestoId, UUID approvedBy) {
        Objects.requireNonNull(presupuestoId, "El ID del presupuesto no puede ser nulo");
        Objects.requireNonNull(approvedBy, "El ID del usuario aprobador no puede ser nulo");

        // 1. Obtener el presupuesto
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No existe un presupuesto con ID %s", presupuestoId.getValue())));

        // 2. Validar que el presupuesto está en estado BORRADOR
        if (presupuesto.isAprobado()) {
            throw new IllegalStateException(
                    String.format("El presupuesto %s ya está aprobado (congelado)", presupuestoId.getValue()));
        }

        // 3. Validar que existe un ProgramaObra para el proyecto
        UUID proyectoId = presupuesto.getProyectoId();
        boolean existeProgramaObra = programaObraRepository.findByProyectoId(proyectoId).isPresent();
        
        if (!existeProgramaObra) {
            throw new PresupuestoSinCronogramaException(presupuestoId, proyectoId);
        }

        // 4. Aprobar el presupuesto (genera hashes criptográficos)
        // Esta operación modifica el estado del presupuesto pero no lo persiste aún
        presupuesto.aprobar(approvedBy, integrityHashService);

        // 5. Congelar el cronograma y generar snapshot (baseline temporal)
        // Esta operación también debe ser atómica con la aprobación del presupuesto
        // Si falla, toda la transacción debe hacer rollback
        CronogramaSnapshot snapshot = cronogramaService.congelarPorPresupuesto(
                proyectoId,
                presupuestoId,
                approvedBy
        );

        // 6. Persistir el presupuesto aprobado
        // Nota: El cronograma ya fue persistido por cronogramaService.congelarPorPresupuesto()
        // La atomicidad debe manejarse en la capa de aplicación mediante @Transactional
        presupuestoRepository.save(presupuesto);

        return snapshot;
    }

    /**
     * Busca el presupuesto de un proyecto.
     * 
     * @param proyectoId ID del proyecto
     * @return Optional con el Presupuesto si existe, vacío en caso contrario
     * @throws IllegalArgumentException si proyectoId es nulo
     */
    public java.util.Optional<Presupuesto> findByProyectoId(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        return presupuestoRepository.findByProyectoId(proyectoId);
    }

    /**
     * Verifica si el presupuesto de un proyecto está aprobado (congelado).
     * 
     * @param proyectoId ID del proyecto
     * @return true si el presupuesto está aprobado, false en caso contrario
     * @throws IllegalArgumentException si proyectoId es nulo
     */
    public boolean estaAprobado(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        
        return presupuestoRepository.findByProyectoId(proyectoId)
                .map(Presupuesto::isAprobado)
                .orElse(false);
    }
}
