package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperRuleTest {

    private final MapperRule rule = new MapperRule(null);
    private final Path testPath = Paths.get("application/mapper/UserMapper.java");

    @Test
    void validate_ValidMapper_NoViolations() {
        List<NamingViolation> violations = rule.validate(testPath, "UserMapper");
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_InvalidMapper_ReturnsViolation() {
        List<NamingViolation> violations = rule.validate(testPath, "UserConverter");
        assertFalse(violations.isEmpty());
        assertEquals("UserConverterMapper", violations.get(0).expectedName());
    }
}
