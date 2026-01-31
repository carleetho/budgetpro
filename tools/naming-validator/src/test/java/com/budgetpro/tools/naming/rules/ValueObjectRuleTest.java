package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectRuleTest {

    private final ValueObjectRule rule = new ValueObjectRule(null);
    private final Path testPath = Paths.get("domain/valueobjects/Dinero.java");

    @Test
    void validate_ValidValueObject_NoViolations() {
        List<NamingViolation> violations = rule.validate(testPath, "Dinero");
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_InvalidValueObject_ReturnsViolation() {
        List<NamingViolation> violations = rule.validate(testPath, "DineroVO");
        assertFalse(violations.isEmpty());
        assertEquals("Dinero", violations.get(0).expectedName());
    }
}
