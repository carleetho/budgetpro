package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.shared.model.TipoRecurso;

import java.util.Objects;

/**
 * Criterios de búsqueda para recursos en catálogo externo.
 * Inmutable y creado mediante builder.
 */
public final class RecursoSearchCriteria {

    private final String query;
    private final TipoRecurso tipo;
    private final String unidad;
    private final Integer limit;
    private final Integer offset;

    private RecursoSearchCriteria(Builder builder) {
        this.query = builder.query;
        this.tipo = builder.tipo;
        this.unidad = builder.unidad;
        this.limit = builder.limit;
        this.offset = builder.offset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQuery() {
        return query;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public String getUnidad() {
        return unidad;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public static final class Builder {
        private String query;
        private TipoRecurso tipo;
        private String unidad;
        private Integer limit;
        private Integer offset;

        private Builder() {
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder tipo(TipoRecurso tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder unidad(String unidad) {
            this.unidad = unidad;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public RecursoSearchCriteria build() {
            validar();
            return new RecursoSearchCriteria(this);
        }

        private void validar() {
            if (query != null && query.isBlank()) {
                throw new IllegalArgumentException("El query no puede estar vacío");
            }
            if (unidad != null && unidad.isBlank()) {
                throw new IllegalArgumentException("La unidad no puede estar vacía");
            }
            if (limit != null && limit <= 0) {
                throw new IllegalArgumentException("El limit debe ser positivo");
            }
            if (offset != null && offset < 0) {
                throw new IllegalArgumentException("El offset no puede ser negativo");
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecursoSearchCriteria that = (RecursoSearchCriteria) o;
        return Objects.equals(query, that.query)
                && tipo == that.tipo
                && Objects.equals(unidad, that.unidad)
                && Objects.equals(limit, that.limit)
                && Objects.equals(offset, that.offset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, tipo, unidad, limit, offset);
    }

    @Override
    public String toString() {
        return "RecursoSearchCriteria{" +
                "query='" + query + '\'' +
                ", tipo=" + tipo +
                ", unidad='" + unidad + '\'' +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}
