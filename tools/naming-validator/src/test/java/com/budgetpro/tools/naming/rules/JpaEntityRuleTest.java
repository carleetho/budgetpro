package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ViolationSeverity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JpaEntityRuleTest {

    private final JpaEntityRule rule = new JpaEntityRule(null);
    private final Path testPath = Paths.get("infrastructure/persistence/entity/PresupuestoJpaEntity.java");

    @Test
    void validate_ValidJpaEntity_NoViolations() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoJpaEntity");
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_InvalidJpaEntity_ReturnsViolation() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoEntity");
        assertFalse(violations.isEmpty());
        assertEquals("PresupuestoEntityJpaEntity", violations.get(0).expectedName());
        assertEquals(ViolationSeverity.BLOCKING, violations.get(0).severity());
    }
}
