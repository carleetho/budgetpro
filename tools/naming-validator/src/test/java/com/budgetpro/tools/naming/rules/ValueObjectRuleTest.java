package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectRuleTest {

    private final ValueObjectRule rule = new ValueObjectRule();

    @Test
    void testValidValueObject() {
        assertTrue(rule.validate(Paths.get("test/Money.java"), "Money").isEmpty());
    }

    @Test
    void testInvalidValueObjectVO() {
        List<NamingViolation> violations = rule.validate(Paths.get("test/MoneyVO.java"), "MoneyVO");
        assertEquals(1, violations.size());
        assertEquals("Money", violations.get(0).expectedName());
    }

    @Test
    void testInvalidValueObjectSuffix() {
        List<NamingViolation> violations = rule.validate(Paths.get("test/MoneyValueObject.java"), "MoneyValueObject");
        assertEquals("Money", violations.get(0).expectedName());
    }
}
