package com.budgetpro.domain.shared.port.out;

import java.util.Set;
import java.util.UUID;

public interface SecurityPort {
    UUID getCurrentUserId();

    boolean hasRole(String roleName);

    boolean hasAnyRole(Set<String> roleNames);
}
