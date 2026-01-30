package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperRuleTest {

    private final MapperRule rule = new MapperRule();

    @Test
    void testValidMapper() {
        assertTrue(rule.validate(Paths.get("test/Mapper.java"), "PresupuestoMapper").isEmpty());
    }

    @Test
    void testInvalidMapper() {
        List<NamingViolation> violations = rule.validate(Paths.get("test/Map.java"), "PresupuestoMap");
        assertFalse(violations.isEmpty());
        assertEquals("PresupuestoMapMapper", violations.get(0).expectedName());
    }
}
