package com.budgetpro.domain.logistica.consumo;

import com.budgetpro.domain.finanzas.compra.Cantidad;
import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.recurso.model.RecursoId;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado CONSUMO.
 * 
 * Representa el consumo real de recursos asignado a una partida.
 * 
 * Invariantes Clave:
 * 1. Todo consumo pertenece a una partida
 * 2. El costo real nace aquí, no en la compra
 * 3. No puede exceder disponibilidad válida (validado externamente antes de crear)
 * 4. Un consumo es inmutable una vez registrado (sin métodos de modificación)
 * 
 * Contexto: Logística & Costos
 * 
 * REGLA DE NEGOCIO: El costo nace en el consumo, no en la compra.
 * El consumo vincula Compra + Inventario + Partida.
 */
public final class ConsumoPartida {

    private final ConsumoId id;
    private final UUID proyectoId;
    private final PartidaId partidaId;
    private final RecursoId recursoId;
    private final Cantidad cantidad;
    private final Monto costoUnitario;
    private final Monto costoTotal;
    private final Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ConsumoPartida(ConsumoId id, UUID proyectoId, PartidaId partidaId, RecursoId recursoId,
                          Cantidad cantidad, Monto costoUnitario, Monto costoTotal, Long version, boolean validar) {
        this.id = Objects.requireNonNull(id, "El ID del consumo no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "El partidaId no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El recursoId no puede ser nulo");
        this.cantidad = Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
        this.costoUnitario = Objects.requireNonNull(costoUnitario, "El costo unitario no puede ser nulo");
        this.costoTotal = Objects.requireNonNull(costoTotal, "El costo total no puede ser nulo");
        this.version = version != null ? version : 0L;

        if (validar) {
            validarInvariantes();
        }
    }

    /**
     * Factory method para crear un nuevo ConsumoPartida.
     * 
     * El costo total es derivado (cantidad * costo unitario).
     * 
     * @param proyectoId El ID del proyecto
     * @param partidaId El ID de la partida
     * @param recursoId El ID del recurso consumido
     * @param cantidad La cantidad consumida
     * @param costoUnitario El costo unitario del recurso
     */
    public static ConsumoPartida crear(UUID proyectoId, PartidaId partidaId, RecursoId recursoId,
                                      Cantidad cantidad, Monto costoUnitario) {
        // Calcular costo total derivado
        java.math.BigDecimal costoTotalValue = cantidad.getValue().multiply(costoUnitario.getValue());
        Monto costoTotal = Monto.of(costoTotalValue);

        return new ConsumoPartida(
            ConsumoId.generate(),
            proyectoId,
            partidaId,
            recursoId,
            cantidad,
            costoUnitario,
            costoTotal,
            0L,
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para crear un ConsumoPartida con ID específico.
     */
    public static ConsumoPartida crear(ConsumoId id, UUID proyectoId, PartidaId partidaId, RecursoId recursoId,
                                      Cantidad cantidad, Monto costoUnitario) {
        // Calcular costo total derivado
        java.math.BigDecimal costoTotalValue = cantidad.getValue().multiply(costoUnitario.getValue());
        Monto costoTotal = Monto.of(costoTotalValue);

        return new ConsumoPartida(
            id,
            proyectoId,
            partidaId,
            recursoId,
            cantidad,
            costoUnitario,
            costoTotal,
            0L,
            true // Validar invariantes en creación
        );
    }

    /**
     * Factory method para reconstruir un ConsumoPartida desde persistencia.
     * NO valida invariantes de creación (permite estados que podrían ser inválidos al crear).
     */
    public static ConsumoPartida reconstruir(ConsumoId id, UUID proyectoId, PartidaId partidaId, RecursoId recursoId,
                                            Cantidad cantidad, Monto costoUnitario, Monto costoTotal, Long version) {
        return new ConsumoPartida(
            id,
            proyectoId,
            partidaId,
            recursoId,
            cantidad,
            costoUnitario,
            costoTotal,
            version,
            false // NO validar invariantes en reconstrucción
        );
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes() {
        // INVARIANTE: El costo unitario debe ser positivo
        if (costoUnitario.esMenorOIgualQue(Monto.cero())) {
            throw new IllegalArgumentException("El costo unitario debe ser positivo");
        }

        // INVARIANTE: El costo total debe ser positivo
        if (costoTotal.esMenorOIgualQue(Monto.cero())) {
            throw new IllegalArgumentException("El costo total debe ser positivo");
        }

        // INVARIANTE: El costo total debe ser consistente con cantidad * costo unitario
        java.math.BigDecimal costoTotalEsperado = cantidad.getValue().multiply(costoUnitario.getValue());
        Monto costoTotalCalculado = Monto.of(costoTotalEsperado);
        if (!costoTotal.equals(costoTotalCalculado)) {
            throw new IllegalStateException(
                String.format("El costo total no es consistente. Esperado: %s, Actual: %s", 
                    costoTotalCalculado, costoTotal)
            );
        }
    }

    // Getters (el consumo es inmutable, no hay setters)

    public ConsumoId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public PartidaId getPartidaId() {
        return partidaId;
    }

    public RecursoId getRecursoId() {
        return recursoId;
    }

    public Cantidad getCantidad() {
        return cantidad;
    }

    public Monto getCostoUnitario() {
        return costoUnitario;
    }

    public Monto getCostoTotal() {
        return costoTotal;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumoPartida that = (ConsumoPartida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ConsumoPartida{" +
                "id=" + id +
                ", proyectoId=" + proyectoId +
                ", partidaId=" + partidaId +
                ", recursoId=" + recursoId +
                ", cantidad=" + cantidad +
                ", costoTotal=" + costoTotal +
                '}';
    }
}
