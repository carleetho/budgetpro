package com.budgetpro.shared;

import java.util.UUID;

/**
 * Identificadores técnicos de actores de sistema usados por flujos de integración.
 */
public final class SystemActorIds {

    public static final String EVENT_INFRA_SYSTEM_USER_UUID_TEXT = "00000000-0000-0000-0000-000000000001";
    public static final UUID EVENT_INFRA_SYSTEM_USER_UUID = UUID.fromString(EVENT_INFRA_SYSTEM_USER_UUID_TEXT);

    private SystemActorIds() {
    }
}
