package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ViolationSeverity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainEntityRuleTest {

    private final DomainEntityRule rule = new DomainEntityRule();
    private final Path testPath = Paths.get("domain/model/Presupuesto.java");

    @Test
    void testValidDomainEntity() {
        List<NamingViolation> violations = rule.validate(testPath, "Presupuesto");
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSuffixEntity() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoEntity");
        assertEquals(1, violations.size());
        NamingViolation violation = violations.get(0);
        assertEquals(ViolationSeverity.BLOCKING, violation.severity());
        assertEquals("Presupuesto", violation.expectedName());
        assertTrue(violation.message().contains("sufijo incorrecto"));
    }

    @Test
    void testInvalidSuffixJpaEntity() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoJpaEntity");
        assertEquals(1, violations.size());
        NamingViolation violation = violations.get(0);
        assertEquals("Presupuesto", violation.expectedName());
    }
}
