package com.budgetpro.domain.finanzas.partida.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PARTIDA.
 * 
 * Representa una partida presupuestaria con estructura jerárquica (WBS - Work Breakdown Structure).
 * 
 * Invariantes:
 * - El presupuestoId es obligatorio
 * - El metrado no puede ser negativo
 * - Si tiene padreId, debe pertenecer al mismo presupuestoId (validado a nivel de aplicación)
 * - El item (código WBS) no puede estar vacío
 * - La descripción no puede estar vacía
 * - El nivel debe ser >= 1
 * 
 * Contexto: Presupuestos & APUs
 */
public final class Partida {

    private final PartidaId id;
    private final UUID presupuestoId;
    // nosemgrep
    private UUID padreId; // Opcional, para jerarquía recursiva
    // nosemgrep
    private String item; // Código WBS: "01.01", "02.01.05"
    // nosemgrep
    private String descripcion;
    // nosemgrep
    private String unidad; // Opcional si es título
    // nosemgrep
    private BigDecimal metrado; // Cantidad presupuestada. 0 si es título
    // nosemgrep
    private BigDecimal presupuestoAsignado;
    // nosemgrep
    private BigDecimal gastosReales;
    // nosemgrep
    private BigDecimal compromisosPendientes;
    // nosemgrep
    private Integer nivel; // Profundidad en el árbol: 1, 2, 3...
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Partida(PartidaId id, UUID presupuestoId, UUID padreId, String item, 
                   String descripcion, String unidad, BigDecimal metrado, Integer nivel, Long version) {
        validarInvariantes(presupuestoId, item, descripcion, metrado, nivel);
        
        this.id = Objects.requireNonNull(id, "El ID de la partida no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        this.padreId = padreId; // Puede ser null (partida raíz)
        this.item = normalizarItem(item);
        this.descripcion = normalizarDescripcion(descripcion);
        this.unidad = unidad != null ? unidad.trim() : null;
        this.metrado = metrado != null ? metrado : BigDecimal.ZERO;
        this.presupuestoAsignado = BigDecimal.ZERO;
        this.gastosReales = BigDecimal.ZERO;
        this.compromisosPendientes = BigDecimal.ZERO;
        this.nivel = Objects.requireNonNull(nivel, "El nivel no puede ser nulo");
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva Partida raíz (sin padre).
     */
    public static Partida crearRaiz(PartidaId id, UUID presupuestoId, String item, 
                                   String descripcion, String unidad, BigDecimal metrado) {
        return new Partida(id, presupuestoId, null, item, descripcion, unidad, metrado, 1, 0L);
    }

    /**
     * Factory method para crear una nueva Partida hija (con padre).
     */
    public static Partida crearHija(PartidaId id, UUID presupuestoId, UUID padreId, 
                                    String item, String descripcion, String unidad, 
                                    BigDecimal metrado, Integer nivel) {
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, nivel, 0L);
    }

    /**
     * Factory method para reconstruir una Partida desde persistencia.
     */
    public static Partida reconstruir(PartidaId id, UUID presupuestoId, UUID padreId,
                                    String item, String descripcion, String unidad,
                                    BigDecimal metrado, Integer nivel, Long version) {
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, nivel, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID presupuestoId, String item, String descripcion, 
                                   BigDecimal metrado, Integer nivel) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("El presupuestoId no puede ser nulo");
        }
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("El item (código WBS) no puede estar vacío");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        if (metrado != null && metrado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El metrado no puede ser negativo");
        }
        if (nivel == null || nivel < 1) {
            throw new IllegalArgumentException("El nivel debe ser >= 1");
        }
    }

    /**
     * Normaliza el item (código WBS).
     */
    private String normalizarItem(String item) {
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("El item (código WBS) no puede estar vacío");
        }
        return item.trim();
    }

    /**
     * Normaliza la descripción.
     */
    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        return descripcion.trim();
    }

    /**
     * Actualiza el item (código WBS).
     */
    public void actualizarItem(String nuevoItem) {
        this.item = normalizarItem(nuevoItem);
    }

    /**
     * Actualiza la descripción.
     */
    public void actualizarDescripcion(String nuevaDescripcion) {
        this.descripcion = normalizarDescripcion(nuevaDescripcion);
    }

    /**
     * Actualiza la unidad.
     */
    public void actualizarUnidad(String nuevaUnidad) {
        this.unidad = nuevaUnidad != null ? nuevaUnidad.trim() : null;
    }

    /**
     * Actualiza el metrado.
     */
    public void actualizarMetrado(BigDecimal nuevoMetrado) {
        if (nuevoMetrado == null) {
            this.metrado = BigDecimal.ZERO;
        } else if (nuevoMetrado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El metrado no puede ser negativo");
        } else {
            this.metrado = nuevoMetrado;
        }
    }

    public void actualizarPresupuestoAsignado(BigDecimal presupuestoAsignado) {
        if (presupuestoAsignado == null || presupuestoAsignado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El presupuesto asignado no puede ser nulo ni negativo");
        }
        this.presupuestoAsignado = presupuestoAsignado;
    }

    public void actualizarGastosReales(BigDecimal gastosReales) {
        if (gastosReales == null || gastosReales.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los gastos reales no pueden ser nulos ni negativos");
        }
        this.gastosReales = gastosReales;
    }

    public void actualizarCompromisosPendientes(BigDecimal compromisosPendientes) {
        if (compromisosPendientes == null || compromisosPendientes.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los compromisos pendientes no pueden ser nulos ni negativos");
        }
        this.compromisosPendientes = compromisosPendientes;
    }

    public BigDecimal getSaldoDisponible() {
        BigDecimal asignado = presupuestoAsignado != null ? presupuestoAsignado : BigDecimal.ZERO;
        BigDecimal reales = gastosReales != null ? gastosReales : BigDecimal.ZERO;
        BigDecimal pendientes = compromisosPendientes != null ? compromisosPendientes : BigDecimal.ZERO;
        return asignado.subtract(reales.add(pendientes));
    }

    public void reservarSaldo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a reservar debe ser positivo");
        }
        BigDecimal disponible = getSaldoDisponible();
        if (disponible.compareTo(monto) < 0) {
            throw new IllegalStateException(String.format(
                    "Saldo disponible insuficiente en la partida %s. Disponible: %s, Monto: %s",
                    this.id != null ? this.id.getValue() : "N/A",
                    disponible,
                    monto
            ));
        }
        this.compromisosPendientes = (this.compromisosPendientes != null ? this.compromisosPendientes : BigDecimal.ZERO)
                .add(monto);
    }

    // Getters

    public PartidaId getId() {
        return id;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public UUID getPadreId() {
        return padreId;
    }

    public String getItem() {
        return item;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public BigDecimal getMetrado() {
        return metrado;
    }

    public BigDecimal getPresupuestoAsignado() {
        return presupuestoAsignado;
    }

    public BigDecimal getGastosReales() {
        return gastosReales;
    }

    public BigDecimal getCompromisosPendientes() {
        return compromisosPendientes;
    }

    public Integer getNivel() {
        return nivel;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Verifica si la partida es raíz (no tiene padre).
     */
    public boolean isRaiz() {
        return padreId == null;
    }

    /**
     * Verifica si la partida es título (metrado = 0).
     */
    public boolean isTitulo() {
        return metrado.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partida partida = (Partida) o;
        return Objects.equals(id, partida.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Partida{id=%s, presupuestoId=%s, padreId=%s, item='%s', descripcion='%s', nivel=%d}", 
                           id, presupuestoId, padreId, item, descripcion, nivel);
    }
}
