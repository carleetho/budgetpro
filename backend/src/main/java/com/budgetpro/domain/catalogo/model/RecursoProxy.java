package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Root del agregado RECURSO_PROXY.
 *
 * Representa un proxy de recurso para catálogos externos con semántica de snapshot.
 * Los campos snapshot capturan el estado del catálogo en un punto de tiempo
 * y no deben mutar una vez creado el proxy.
 *
 * Invariantes:
 * - externalId y catalogSource no pueden estar vacíos
 * - nombreSnapshot no puede estar vacío
 * - tipoSnapshot no puede ser nulo
 * - unidadSnapshot no puede estar vacía
 * - precioSnapshot no puede ser nulo ni negativo
 * - snapshotDate no puede ser nula
 */
public final class RecursoProxy {

    private final RecursoProxyId id;
    private final String externalId;
    private final String catalogSource;
    private final String nombreSnapshot;
    private final TipoRecurso tipoSnapshot;
    private final String unidadSnapshot;
    private final BigDecimal precioSnapshot;
    private final LocalDateTime snapshotDate;
    private EstadoProxy estado;
    private final Long version;

    private RecursoProxy(RecursoProxyId id,
                         String externalId,
                         String catalogSource,
                         String nombreSnapshot,
                         TipoRecurso tipoSnapshot,
                         String unidadSnapshot,
                         BigDecimal precioSnapshot,
                         LocalDateTime snapshotDate,
                         EstadoProxy estado,
                         Long version) {
        validarInvariantes(externalId, catalogSource, nombreSnapshot, tipoSnapshot, unidadSnapshot, precioSnapshot, snapshotDate);

        this.id = Objects.requireNonNull(id, "El ID del recurso proxy no puede ser nulo");
        this.externalId = externalId.trim();
        this.catalogSource = catalogSource.trim();
        this.nombreSnapshot = nombreSnapshot.trim();
        this.tipoSnapshot = tipoSnapshot;
        this.unidadSnapshot = unidadSnapshot.trim();
        this.precioSnapshot = precioSnapshot;
        this.snapshotDate = snapshotDate;
        this.estado = estado != null ? estado : EstadoProxy.ACTIVO;
        this.version = version != null ? version : 0L;
    }

    public static RecursoProxy crear(RecursoProxyId id,
                                     String externalId,
                                     String catalogSource,
                                     String nombreSnapshot,
                                     TipoRecurso tipoSnapshot,
                                     String unidadSnapshot,
                                     BigDecimal precioSnapshot,
                                     LocalDateTime snapshotDate) {
        return new RecursoProxy(
                id,
                externalId,
                catalogSource,
                nombreSnapshot,
                tipoSnapshot,
                unidadSnapshot,
                precioSnapshot,
                snapshotDate,
                EstadoProxy.ACTIVO,
                0L
        );
    }

    public static RecursoProxy reconstruir(RecursoProxyId id,
                                           String externalId,
                                           String catalogSource,
                                           String nombreSnapshot,
                                           TipoRecurso tipoSnapshot,
                                           String unidadSnapshot,
                                           BigDecimal precioSnapshot,
                                           LocalDateTime snapshotDate,
                                           EstadoProxy estado,
                                           Long version) {
        return new RecursoProxy(
                id,
                externalId,
                catalogSource,
                nombreSnapshot,
                tipoSnapshot,
                unidadSnapshot,
                precioSnapshot,
                snapshotDate,
                estado,
                version
        );
    }

    private void validarInvariantes(String externalId,
                                    String catalogSource,
                                    String nombreSnapshot,
                                    TipoRecurso tipoSnapshot,
                                    String unidadSnapshot,
                                    BigDecimal precioSnapshot,
                                    LocalDateTime snapshotDate) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("El externalId no puede estar vacío");
        }
        if (catalogSource == null || catalogSource.isBlank()) {
            throw new IllegalArgumentException("El catalogSource no puede estar vacío");
        }
        if (nombreSnapshot == null || nombreSnapshot.isBlank()) {
            throw new IllegalArgumentException("El nombreSnapshot no puede estar vacío");
        }
        if (tipoSnapshot == null) {
            throw new IllegalArgumentException("El tipoSnapshot no puede ser nulo");
        }
        if (unidadSnapshot == null || unidadSnapshot.isBlank()) {
            throw new IllegalArgumentException("La unidadSnapshot no puede estar vacía");
        }
        if (precioSnapshot == null || precioSnapshot.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precioSnapshot no puede ser nulo ni negativo");
        }
        if (snapshotDate == null) {
            throw new IllegalArgumentException("La snapshotDate no puede ser nula");
        }
    }

    /**
     * Marca el proxy como OBSOLETO cuando el recurso ya no existe en el catálogo externo.
     */
    public void marcarObsoleto() {
        this.estado = EstadoProxy.OBSOLETO;
    }

    // Getters

    public RecursoProxyId getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getCatalogSource() {
        return catalogSource;
    }

    public String getNombreSnapshot() {
        return nombreSnapshot;
    }

    public TipoRecurso getTipoSnapshot() {
        return tipoSnapshot;
    }

    public String getUnidadSnapshot() {
        return unidadSnapshot;
    }

    public BigDecimal getPrecioSnapshot() {
        return precioSnapshot;
    }

    public LocalDateTime getSnapshotDate() {
        return snapshotDate;
    }

    public EstadoProxy getEstado() {
        return estado;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isActivo() {
        return estado == EstadoProxy.ACTIVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecursoProxy that = (RecursoProxy) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RecursoProxy{" +
                "id=" + id +
                ", externalId='" + externalId + '\'' +
                ", catalogSource='" + catalogSource + '\'' +
                ", nombreSnapshot='" + nombreSnapshot + '\'' +
                ", tipoSnapshot=" + tipoSnapshot +
                ", unidadSnapshot='" + unidadSnapshot + '\'' +
                ", precioSnapshot=" + precioSnapshot +
                ", snapshotDate=" + snapshotDate +
                ", estado=" + estado +
                '}';
    }
}
