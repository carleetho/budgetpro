package com.budgetpro.domain.shared;

import java.util.UUID;

/**
 * Identificadores de arrendamiento (tenant) usados en persistencia y API.
 */
public final class TenancyConstants {

    /** Tenant por defecto para datos legados y entornos single-tenant. */
    public static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private TenancyConstants() {
    }
}
