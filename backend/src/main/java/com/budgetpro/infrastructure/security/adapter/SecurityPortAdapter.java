package com.budgetpro.infrastructure.security.adapter;

import com.budgetpro.domain.shared.port.out.SecurityPort;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SecurityPortAdapter implements SecurityPort {

    // Default mock user for development/startup if no context exists
    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public UUID getCurrentUserId() {
        // TODO: Implement actual security context retrieval (e.g., from
        // SecurityContextHolder)
        return DEFAULT_USER_ID;
    }

    @Override
    public boolean hasRole(String roleName) {
        // TODO: Implement actual role checking
        return true; // Permissive by default for dev startup
    }

    @Override
    public boolean hasAnyRole(Set<String> roleNames) {
        // TODO: Implement actual role checking
        return true; // Permissive by default for dev startup
    }
}
