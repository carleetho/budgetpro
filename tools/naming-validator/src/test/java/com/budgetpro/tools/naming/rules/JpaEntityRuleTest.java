package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ViolationSeverity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JpaEntityRuleTest {

    private final JpaEntityRule rule = new JpaEntityRule();
    private final Path testPath = Paths.get("infrastructure/persistence/entity/PresupuestoJpaEntity.java");

    @Test
    void testValidJpaEntity() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoJpaEntity");
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidJpaEntity() {
        List<NamingViolation> violations = rule.validate(testPath, "Presupuesto");
        assertEquals(1, violations.size());
        NamingViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.WARNING, violation.severity());
        assertEquals("PresupuestoJpaEntity", violation.expectedName());
    }
}
