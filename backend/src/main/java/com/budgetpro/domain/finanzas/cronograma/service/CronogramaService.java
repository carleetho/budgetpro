package com.budgetpro.domain.finanzas.cronograma.service;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Dominio para orquestar operaciones de congelamiento del cronograma
 * y generación de snapshots (baseline temporal).
 * 
 * Responsabilidad:
 * - Orquestar la operación de congelamiento del ProgramaObra
 * - Generar snapshots inmutables del estado temporal
 * - Validar prerrequisitos antes del congelamiento
 * - Persistir el estado congelado y el snapshot de forma atómica
 * - Proporcionar métodos de consulta para cronogramas congelados
 * 
 * **Operaciones:**
 * - congelarPorPresupuesto(): Congela el cronograma y genera snapshot
 * - findByProyectoId(): Busca el cronograma de un proyecto
 * - estaCongelado(): Verifica si un cronograma está congelado
 * 
 * **Validaciones:**
 * - El ProgramaObra debe existir para el proyecto
 * - El ProgramaObra debe tener fechaInicio y fechaFinEstimada
 * - El PresupuestoId debe ser válido
 * 
 * Contexto: Cronograma & Baseline
 */
public class CronogramaService {

    private final ProgramaObraRepository programaObraRepository;
    private final ActividadProgramadaRepository actividadProgramadaRepository;
    private final CronogramaSnapshotRepository snapshotRepository;
    private final SnapshotGeneratorService snapshotGeneratorService;

    public CronogramaService(
            ProgramaObraRepository programaObraRepository,
            ActividadProgramadaRepository actividadProgramadaRepository,
            CronogramaSnapshotRepository snapshotRepository,
            SnapshotGeneratorService snapshotGeneratorService) {
        this.programaObraRepository = Objects.requireNonNull(programaObraRepository, "El repositorio de programa de obra no puede ser nulo");
        this.actividadProgramadaRepository = Objects.requireNonNull(actividadProgramadaRepository, "El repositorio de actividades no puede ser nulo");
        this.snapshotRepository = Objects.requireNonNull(snapshotRepository, "El repositorio de snapshots no puede ser nulo");
        this.snapshotGeneratorService = Objects.requireNonNull(snapshotGeneratorService, "El servicio de generación de snapshots no puede ser nulo");
    }

    /**
     * Congela el cronograma de un proyecto y genera un snapshot inmutable del baseline temporal.
     * 
     * Este método orquesta la operación completa de congelamiento:
     * 1. Valida que el ProgramaObra exista para el proyecto
     * 2. Valida que el ProgramaObra tenga fechas válidas
     * 3. Congela el ProgramaObra (marca como congelado)
     * 4. Obtiene todas las actividades del cronograma
     * 5. Genera el snapshot con todos los datos temporales
     * 6. Persiste tanto el ProgramaObra congelado como el snapshot
     * 
     * **Nota sobre transacciones:**
     * La persistencia atómica (freeze + snapshot) debe manejarse en la capa de aplicación
     * o infraestructura mediante transacciones. Este servicio asume que ambas operaciones
     * se ejecutan en el mismo contexto transaccional.
     * 
     * @param proyectoId ID del proyecto
     * @param presupuestoId ID del presupuesto asociado
     * @param approvedBy ID del usuario que aprueba el congelamiento
     * @return El snapshot generado
     * @throws IllegalArgumentException si algún parámetro es nulo
     * @throws IllegalStateException si el ProgramaObra no existe o no tiene fechas válidas
     */
    public CronogramaSnapshot congelarPorPresupuesto(UUID proyectoId, PresupuestoId presupuestoId, UUID approvedBy) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        Objects.requireNonNull(approvedBy, "El ID del usuario aprobador no puede ser nulo");

        // 1. Validar que el ProgramaObra existe para el proyecto
        ProgramaObra programaObra = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No existe un programa de obra para el proyecto %s", proyectoId)));

        // 2. Validar que el ProgramaObra tiene fechas válidas
        if (programaObra.getFechaInicio() == null) {
            throw new IllegalStateException(
                    String.format("No se puede congelar el cronograma del proyecto %s: falta fecha de inicio", proyectoId));
        }
        if (programaObra.getFechaFinEstimada() == null) {
            throw new IllegalStateException(
                    String.format("No se puede congelar el cronograma del proyecto %s: falta fecha de fin estimada", proyectoId));
        }

        // 3. Validar que el ProgramaObra no esté ya congelado
        if (programaObra.estaCongelado()) {
            throw new IllegalStateException(
                    String.format("El cronograma del proyecto %s ya está congelado", proyectoId));
        }

        // 4. Congelar el ProgramaObra
        programaObra.congelar(approvedBy);

        // 5. Obtener todas las actividades del cronograma
        List<ActividadProgramada> actividades = actividadProgramadaRepository.findByProgramaObraId(programaObra.getId().getValue());

        // 6. Generar los datos JSON del snapshot
        String fechasJson = snapshotGeneratorService.generarFechasJson(programaObra, actividades);
        String duracionesJson = snapshotGeneratorService.generarDuracionesJson(programaObra, actividades);
        String secuenciaJson = snapshotGeneratorService.generarSecuenciaJson(actividades);
        String calendariosJson = snapshotGeneratorService.generarCalendariosJson();

        // 7. Crear el snapshot
        CronogramaSnapshotId snapshotId = CronogramaSnapshotId.nuevo();
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObra.getId(),
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        // 8. Persistir el ProgramaObra congelado y el snapshot
        // Nota: La atomicidad debe manejarse en la capa de aplicación/infraestructura
        programaObraRepository.save(programaObra);
        snapshotRepository.save(snapshot);

        return snapshot;
    }

    /**
     * Busca el programa de obra de un proyecto.
     * 
     * @param proyectoId ID del proyecto
     * @return Optional con el ProgramaObra si existe, vacío en caso contrario
     * @throws IllegalArgumentException si proyectoId es nulo
     */
    public java.util.Optional<ProgramaObra> findByProyectoId(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        return programaObraRepository.findByProyectoId(proyectoId);
    }

    /**
     * Verifica si el cronograma de un proyecto está congelado.
     * 
     * @param proyectoId ID del proyecto
     * @return true si el cronograma está congelado, false en caso contrario
     * @throws IllegalArgumentException si proyectoId es nulo
     */
    public boolean estaCongelado(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        
        return programaObraRepository.findByProyectoId(proyectoId)
                .map(ProgramaObra::estaCongelado)
                .orElse(false);
    }

    /**
     * Busca el snapshot asociado a un programa de obra.
     * 
     * @param proyectoId ID del proyecto
     * @return Optional con el CronogramaSnapshot si existe, vacío en caso contrario
     * @throws IllegalArgumentException si proyectoId es nulo
     */
    public java.util.Optional<CronogramaSnapshot> findSnapshotByProyectoId(UUID proyectoId) {
        Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        
        return programaObraRepository.findByProyectoId(proyectoId)
                .map(ProgramaObra::getId)
                .flatMap(snapshotRepository::findByProgramaObraId);
    }
}
