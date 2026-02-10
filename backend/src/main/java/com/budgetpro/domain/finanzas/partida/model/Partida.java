package com.budgetpro.domain.finanzas.partida.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PARTIDA.
 * 
 * Representa una partida presupuestaria con estructura jerárquica (WBS - Work
 * Breakdown Structure).
 */
public final class Partida {

    private final PartidaId id;
    private final UUID presupuestoId;
    private final UUID padreId; // Opcional, para jerarquía recursiva
    private final String item; // Código WBS: "01.01", "02.01.05"
    private final String descripcion;
    private final String unidad; // Opcional si es título
    private final BigDecimal metrado; // Cantidad presupuestada. 0 si es título
    private final BigDecimal presupuestoAsignado;
    private final BigDecimal gastosReales;
    private final BigDecimal compromisosPendientes;
    private final Integer nivel; // Profundidad en el árbol: 1, 2, 3...
    private final Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Partida(PartidaId id, UUID presupuestoId, UUID padreId, String item, String descripcion, String unidad,
            BigDecimal metrado, BigDecimal presupuestoAsignado, BigDecimal gastosReales,
            BigDecimal compromisosPendientes, Integer nivel, Long version) {
        validarInvariantes(presupuestoId, item, descripcion, metrado, nivel);

        this.id = Objects.requireNonNull(id, "El ID de la partida no puede ser nulo");
        this.presupuestoId = Objects.requireNonNull(presupuestoId, "El presupuestoId no puede ser nulo");
        // REGLA-038
        this.padreId = padreId;
        this.item = normalizarItem(item);
        this.descripcion = normalizarDescripcion(descripcion);
        this.unidad = unidad != null ? unidad.trim() : null;
        this.metrado = metrado != null ? metrado : BigDecimal.ZERO;
        this.presupuestoAsignado = presupuestoAsignado != null ? presupuestoAsignado : BigDecimal.ZERO;
        this.gastosReales = gastosReales != null ? gastosReales : BigDecimal.ZERO;
        this.compromisosPendientes = compromisosPendientes != null ? compromisosPendientes : BigDecimal.ZERO;
        this.nivel = Objects.requireNonNull(nivel, "El nivel no puede ser nulo");
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva Partida raíz (sin padre).
     */
    public static Partida crearRaiz(PartidaId id, UUID presupuestoId, String item, String descripcion, String unidad,
            BigDecimal metrado) {
        return new Partida(id, presupuestoId, null, item, descripcion, unidad, metrado, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 1, 0L);
    }

    /**
     * Factory method para crear una nueva Partida hija (con padre).
     */
    public static Partida crearHija(PartidaId id, UUID presupuestoId, UUID padreId, String item, String descripcion,
            String unidad, BigDecimal metrado, Integer nivel) {
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, nivel, 0L);
    }

    /**
     * Factory method para reconstruir una Partida desde persistencia.
     */
    public static Partida reconstruir(PartidaId id, UUID presupuestoId, UUID padreId, String item, String descripcion,
            String unidad, BigDecimal metrado, BigDecimal presupuestoAsignado, BigDecimal gastosReales,
            BigDecimal compromisosPendientes, Integer nivel, Long version) {
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, presupuestoAsignado,
                gastosReales, compromisosPendientes, nivel, version);
    }

    private void validarInvariantes(UUID presupuestoId, String item, String descripcion, BigDecimal metrado,
            Integer nivel) {
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
            // REGLA-037
            throw new IllegalArgumentException("El nivel debe ser >= 1");
        }
    }

    private String normalizarItem(String item) {
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("El item (código WBS) no puede estar vacío");
        }
        return item.trim();
    }

    private String normalizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }
        return descripcion.trim();
    }

    public Partida actualizarItem(String nuevoItem) {
        return new Partida(id, presupuestoId, padreId, nuevoItem, descripcion, unidad, metrado, presupuestoAsignado,
                gastosReales, compromisosPendientes, nivel, version);
    }

    public Partida actualizarDescripcion(String nuevaDescripcion) {
        return new Partida(id, presupuestoId, padreId, item, nuevaDescripcion, unidad, metrado, presupuestoAsignado,
                gastosReales, compromisosPendientes, nivel, version);
    }

    public Partida actualizarUnidad(String nuevaUnidad) {
        return new Partida(id, presupuestoId, padreId, item, descripcion, nuevaUnidad, metrado, presupuestoAsignado,
                gastosReales, compromisosPendientes, nivel, version);
    }

    public Partida actualizarMetrado(BigDecimal nuevoMetrado) {
        BigDecimal m = nuevoMetrado != null ? nuevoMetrado : BigDecimal.ZERO;
        if (m.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El metrado no puede ser negativo");
        }
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, m, presupuestoAsignado, gastosReales,
                compromisosPendientes, nivel, version);
    }

    public Partida actualizarPresupuestoAsignado(BigDecimal nuevoPresupuestoAsignado) {
        if (nuevoPresupuestoAsignado == null || nuevoPresupuestoAsignado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El presupuesto asignado no puede ser nulo ni negativo");
        }
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, nuevoPresupuestoAsignado,
                gastosReales, compromisosPendientes, nivel, version);
    }

    public Partida actualizarGastosReales(BigDecimal nuevosGastosReales) {
        if (nuevosGastosReales == null || nuevosGastosReales.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los gastos reales no pueden ser nulos ni negativos");
        }
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, presupuestoAsignado,
                nuevosGastosReales, compromisosPendientes, nivel, version);
    }

    public Partida actualizarCompromisosPendientes(BigDecimal nuevosCompromisosPendientes) {
        if (nuevosCompromisosPendientes == null || nuevosCompromisosPendientes.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Los compromisos pendientes no pueden ser nulos ni negativos");
        }
        return new Partida(id, presupuestoId, padreId, item, descripcion, unidad, metrado, presupuestoAsignado,
                gastosReales, nuevosCompromisosPendientes, nivel, version);
    }

    public BigDecimal getSaldoDisponible() {
        return presupuestoAsignado.subtract(gastosReales.add(compromisosPendientes));
    }

    public Partida reservarSaldo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a reservar debe ser positivo");
        }
        BigDecimal disponible = getSaldoDisponible();
        if (disponible.compareTo(monto) < 0) {
            throw new IllegalStateException(
                    String.format("Saldo disponible insuficiente en la partida %s. Disponible: %s, Monto: %s",
                            this.id.getValue(), disponible, monto));
        }
        return actualizarCompromisosPendientes(this.compromisosPendientes.add(monto));
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

    public boolean isRaiz() {
        return padreId == null;
    }

    public boolean isTitulo() {
        return metrado.compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Partida partida = (Partida) o;
        return Objects.equals(id, partida.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Partida{id=%s, item='%s', descripcion='%s', nivel=%d}", id, item, descripcion, nivel);
    }
}
