package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainServiceRuleTest {

    private final DomainServiceRule rule = new DomainServiceRule(null);
    private final Path testPath = Paths.get("domain/service/PresupuestoService.java");

    @Test
    void validate_ValidService_NoViolations() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoService");
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_InvalidService_ReturnsViolation() {
        List<NamingViolation> violations = rule.validate(testPath, "PresupuestoManager");
        assertFalse(violations.isEmpty());
        assertEquals("PresupuestoManagerService", violations.get(0).expectedName());
    }
}
