package com.budgetpro.infrastructure.adapter.stub;

import com.budgetpro.domain.shared.port.out.SecurityPort;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SecurityPortStub implements SecurityPort {
    @Override
    public UUID getCurrentUserId() {
        return UUID.randomUUID();
    }

    @Override
    public boolean hasRole(String roleName) {
        return true;
    }

    @Override
    public boolean hasAnyRole(Set<String> roleNames) {
        return true;
    }
}
