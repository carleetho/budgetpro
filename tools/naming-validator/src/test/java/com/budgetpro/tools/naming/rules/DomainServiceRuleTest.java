package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainServiceRuleTest {

    private final DomainServiceRule rule = new DomainServiceRule();

    @Test
    void testValidDomainService() {
        assertTrue(rule.validate(Paths.get("domain/service/PresupuestoService.java"), "PresupuestoService").isEmpty());
    }

    @Test
    void testInvalidDomainServiceLocation() {
        List<NamingViolation> violations = rule.validate(Paths.get("domain/PresupuestoService.java"),
                "PresupuestoService");
        assertFalse(violations.isEmpty());
        assertTrue(violations.get(0).message().contains("/service/"));
    }
}
