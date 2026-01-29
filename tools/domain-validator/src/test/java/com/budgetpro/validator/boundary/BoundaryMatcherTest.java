package com.budgetpro.validator.boundary;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BoundaryMatcherTest {

    @Test
    void shouldIdentifyForbiddenImports() {
        List<String> forbidden = List.of("org.springframework.*", "com.budgetpro.infrastructure.PersistenceAdapter");
        BoundaryMatcher matcher = new BoundaryMatcher(forbidden, List.of("java.*"));

        assertTrue(matcher.isForbidden("org.springframework.stereotype.Service"));
        assertTrue(matcher.isForbidden("com.budgetpro.infrastructure.PersistenceAdapter"));
        assertFalse(matcher.isForbidden("java.util.List"));
        assertFalse(matcher.isForbidden("com.budgetpro.domain.Presupuesto"));
    }

    @Test
    void shouldAllowExplicitlyPermittedPatterns() {
        List<String> forbidden = List.of("javax.*");
        List<String> allowed = List.of("javax.validation.*");
        BoundaryMatcher matcher = new BoundaryMatcher(forbidden, allowed);

        assertTrue(matcher.isForbidden("javax.persistence.Entity"));
        assertFalse(matcher.isForbidden("javax.validation.constraints.NotNull"));
    }

    @Test
    void shouldHandleWildcardAtPackageLevel() {
        BoundaryMatcher matcher = new BoundaryMatcher(List.of("org.springframework.*"), null);
        
        assertTrue(matcher.isForbidden("org.springframework.context.ApplicationContext"));
        assertTrue(matcher.isForbidden("org.springframework"));
        assertFalse(matcher.isForbidden("org.hibernate.Session"));
    }
}
