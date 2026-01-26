package com.budgetpro.domain.catalogo.exception;

/**
 * Excepción lanzada cuando el servicio de catálogo falla o no responde.
 */
public class CatalogServiceException extends RuntimeException {

    private final String catalogSource;

    public CatalogServiceException(String catalogSource, String message) {
        super(message);
        this.catalogSource = catalogSource;
    }

    public CatalogServiceException(String catalogSource, String message, Throwable cause) {
        super(message, cause);
        this.catalogSource = catalogSource;
    }

    public String getCatalogSource() {
        return catalogSource;
    }
}
