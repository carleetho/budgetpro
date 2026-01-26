package com.budgetpro.domain.catalogo.exception;

/**
 * Excepción lanzada cuando un recurso no existe en el catálogo externo.
 */
public class CatalogNotFoundException extends RuntimeException {

    private final String externalId;
    private final String catalogSource;

    public CatalogNotFoundException(String externalId, String catalogSource) {
        super(String.format("Recurso %s no encontrado en catálogo %s", externalId, catalogSource));
        this.externalId = externalId;
        this.catalogSource = catalogSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getCatalogSource() {
        return catalogSource;
    }
}
