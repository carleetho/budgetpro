package com.budgetpro.domain.finanzas.cronograma.model;

import com.budgetpro.domain.finanzas.cronograma.exception.CronogramaCongeladoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raíz que representa el Programa de Obra de un Proyecto.
 * 
 * Relación 1:1 con Proyecto.
 * 
 * Responsabilidad: - Gestionar la programación temporal del proyecto - Calcular
 * la duración total del proyecto - Coordinar las actividades programadas
 * 
 * Invariantes: - El proyectoId es obligatorio - La fechaFinEstimada no puede
 * ser menor a fechaInicio - La duracionTotalDias debe ser consistente con las
 * fechas - Un cronograma congelado no puede ser modificado (freeze guard)
 * 
 * **Patrón de Freeze:** Una vez que un cronograma es congelado mediante el
 * método congelar(), todas las operaciones de modificación de fechas quedan
 * bloqueadas para preservar la integridad del baseline establecido.
 */
public final class ProgramaObra {

    private final ProgramaObraId id;
    private final UUID proyectoId;
    private final LocalDate fechaInicio;
    private final LocalDate fechaFinEstimada;
    private final Integer duracionTotalDias;
    private final Long version;

    // Freeze state fields
    /**
     * Indica si el cronograma ha sido congelado (baseline establecido). Una vez
     * congelado, no se pueden modificar las fechas.
     */
    private final Boolean congelado;

    /**
     * Timestamp de cuando se congeló el cronograma.
     */
    private final LocalDateTime congeladoAt;

    /**
     * ID del usuario que congeló el cronograma.
     */
    private final UUID congeladoBy;

    /**
     * Versión del algoritmo usado para generar el snapshot del cronograma. Permite
     * migración futura a algoritmos diferentes sin romper compatibilidad.
     */
    private final String snapshotAlgorithm;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ProgramaObra(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio, LocalDate fechaFinEstimada,
            Integer duracionTotalDias, Long version, Boolean congelado, LocalDateTime congeladoAt, UUID congeladoBy,
            String snapshotAlgorithm) {
        validarInvariantes(proyectoId, fechaInicio, fechaFinEstimada);

        this.id = Objects.requireNonNull(id, "El ID del programa de obra no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.duracionTotalDias = duracionTotalDias != null ? duracionTotalDias
                : calcularDuracion(fechaInicio, fechaFinEstimada);
        this.version = version != null ? version : 0L;

        // Freeze state fields (nullable until freeze)
        this.congelado = congelado != null ? congelado : false;
        this.congeladoAt = congeladoAt;
        this.congeladoBy = congeladoBy;
        this.snapshotAlgorithm = snapshotAlgorithm;
    }

    /**
     * Factory method para crear un nuevo ProgramaObra. Los campos de freeze se
     * inicializan como false/null hasta el congelamiento.
     */
    public static ProgramaObra crear(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio,
            LocalDate fechaFinEstimada) {
        return new ProgramaObra(id, proyectoId, fechaInicio, fechaFinEstimada, null, 0L, false, null, null, null);
    }

    /**
     * Factory method para reconstruir un ProgramaObra desde persistencia (firma
     * simplificada). Los campos de freeze se establecen como false/null (para
     * compatibilidad con código existente).
     * 
     * @deprecated Use la firma completa con campos de freeze cuando estén
     *             disponibles en la persistencia.
     */
    @Deprecated
    public static ProgramaObra reconstruir(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio,
            LocalDate fechaFinEstimada, Integer duracionTotalDias, Long version) {
        return new ProgramaObra(id, proyectoId, fechaInicio, fechaFinEstimada, duracionTotalDias, version, false, null,
                null, null);
    }

    /**
     * Factory method para reconstruir un ProgramaObra desde persistencia. Incluye
     * todos los campos de freeze si el cronograma fue congelado.
     */
    public static ProgramaObra reconstruir(ProgramaObraId id, UUID proyectoId, LocalDate fechaInicio,
            LocalDate fechaFinEstimada, Integer duracionTotalDias, Long version, Boolean congelado,
            LocalDateTime congeladoAt, UUID congeladoBy, String snapshotAlgorithm) {
        return new ProgramaObra(id, proyectoId, fechaInicio, fechaFinEstimada, duracionTotalDias, version, congelado,
                congeladoAt, congeladoBy, snapshotAlgorithm);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (fechaInicio != null && fechaFinEstimada != null) {
            if (fechaFinEstimada.isBefore(fechaInicio)) {
                // REGLA-021
                throw new IllegalArgumentException("La fecha de fin estimada no puede ser menor a la fecha de inicio");
            }
        }
    }

    /**
     * Calcula la duración en días entre dos fechas.
     */
    private Integer calcularDuracion(LocalDate fechaInicio, LocalDate fechaFinEstimada) {
        if (fechaInicio == null || fechaFinEstimada == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFinEstimada) + 1; // +1 para incluir
                                                                                                    // ambos días
    }

    /**
     * Actualiza las fechas del programa y recalcula la duración.
     * 
     * **Freeze Guard:** Si el cronograma está congelado, cualquier modificación
     * lanza CronogramaCongeladoException para proteger la integridad.
     * 
     * @param nuevaFechaInicio      Nueva fecha de inicio
     * @param nuevaFechaFinEstimada Nueva fecha de fin estimada
     * @return Nuevo ProgramaObra con las fechas actualizadas
     * @throws IllegalArgumentException     si las fechas son inválidas
     * @throws CronogramaCongeladoException si el cronograma está congelado
     */
    public ProgramaObra actualizarFechas(LocalDate nuevaFechaInicio, LocalDate nuevaFechaFinEstimada) {
        // Freeze guard: no se puede modificar un cronograma congelado
        if (estaCongelado()) {
            throw new CronogramaCongeladoException(this.id, "actualizarFechas");
        }

        return new ProgramaObra(this.id, this.proyectoId, nuevaFechaInicio, nuevaFechaFinEstimada, null, // recalcula en
                                                                                                         // constructor
                this.version, this.congelado, this.congeladoAt, this.congeladoBy, this.snapshotAlgorithm);
    }

    /**
     * Actualiza la fecha de fin estimada basándose en la fecha de fin más tardía de
     * las actividades.
     * 
     * **Freeze Guard:** Si el cronograma está congelado, cualquier modificación
     * lanza CronogramaCongeladoException para proteger la integridad.
     * 
     * @param fechaFinMasTardia La fecha de fin más tardía de todas las actividades
     * @return Nuevo ProgramaObra con la fecha de fin actualizada
     * @throws IllegalStateException        si no hay fecha de inicio
     * @throws IllegalArgumentException     si la fecha de fin es inválida
     * @throws CronogramaCongeladoException si el cronograma está congelado
     */
    public ProgramaObra actualizarFechaFinDesdeActividades(LocalDate fechaFinMasTardia) {
        // Freeze guard: no se puede modificar un cronograma congelado
        if (estaCongelado()) {
            throw new CronogramaCongeladoException(this.id, "actualizarFechaFinDesdeActividades");
        }

        if (fechaFinMasTardia == null) {
            return this;
        }
        if (this.fechaInicio == null) {
            // REGLA-022
            throw new IllegalStateException("No se puede actualizar la fecha de fin sin fecha de inicio");
        }
        if (fechaFinMasTardia.isBefore(this.fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin más tardía no puede ser menor a la fecha de inicio");
        }

        return new ProgramaObra(this.id, this.proyectoId, this.fechaInicio, fechaFinMasTardia, null, // recalcula en
                                                                                                     // constructor
                this.version, this.congelado, this.congeladoAt, this.congeladoBy, this.snapshotAlgorithm);
    }

    // Getters

    public ProgramaObraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public Integer getDuracionTotalDias() {
        return duracionTotalDias;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Congela el cronograma estableciendo el baseline.
     * 
     * Este método implementa el patrón de "freeze" para prevenir modificaciones
     * después de establecer el baseline del cronograma:
     * 
     * 1. Valida que el cronograma tenga fechaInicio y fechaFinEstimada 2. Marca el
     * cronograma como congelado 3. Registra metadata de cuándo y quién congeló el
     * cronograma 4. Establece la versión del algoritmo de snapshot
     * 
     * **Una vez congelado, el cronograma no puede ser modificado.**
     * 
     * @param approvedBy ID del usuario que congela el cronograma
     * @return Nuevo ProgramaObra congelado
     * @throws IllegalArgumentException si approvedBy es nulo
     * @throws IllegalStateException    si el cronograma no tiene fechaInicio o
     *                                  fechaFinEstimada
     */
    public ProgramaObra congelar(UUID approvedBy) {
        if (approvedBy == null) {
            throw new IllegalArgumentException("El ID del usuario que congela el cronograma no puede ser nulo");
        }

        // Validación: no se puede congelar sin fechas válidas
        if (this.fechaInicio == null) {
            throw new IllegalStateException("No se puede congelar el cronograma sin fecha de inicio");
        }
        if (this.fechaFinEstimada == null) {
            throw new IllegalStateException("No se puede congelar el cronograma sin fecha de fin estimada");
        }

        return new ProgramaObra(this.id, this.proyectoId, this.fechaInicio, this.fechaFinEstimada,
                this.duracionTotalDias, this.version, true, LocalDateTime.now(), approvedBy, "v1");
    }

    /**
     * Verifica si el cronograma está congelado.
     * 
     * @return true si el cronograma está congelado, false en caso contrario
     */
    public boolean estaCongelado() {
        return congelado != null && congelado;
    }

    /**
     * Obtiene el timestamp de cuando se congeló el cronograma.
     * 
     * @return La fecha y hora de congelamiento o null si no está congelado
     */
    public LocalDateTime getCongeladoAt() {
        return congeladoAt;
    }

    public UUID getCongeladoBy() {
        return congeladoBy;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProgramaObra that = (ProgramaObra) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "ProgramaObra{id=%s, proyectoId=%s, fechaInicio=%s, fechaFinEstimada=%s, duracionTotalDias=%d, congelado=%s}",
                id, proyectoId, fechaInicio, fechaFinEstimada, duracionTotalDias, estaCongelado());
    }
}
