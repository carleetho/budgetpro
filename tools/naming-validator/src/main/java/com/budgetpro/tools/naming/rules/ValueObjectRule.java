package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla para Value Objects: No deben tener sufijos como 'VO' o 'ValueObject'.
 */
public class ValueObjectRule implements ValidationRule {

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        List<NamingViolation> violations = new ArrayList<>();

        if (className.endsWith("VO") || className.endsWith("ValueObject")) {
            String expectedName = className.replaceAll("(?:VO|ValueObject)$", "");
            violations.add(new NamingViolation(filePath, className, expectedName, ViolationSeverity.WARNING,
                    "NAMING: Value Object con sufijo innecesario",
                    "Los Value Objects no deben tener sufijo 'VO' o 'ValueObject'. Usa solo el nombre del concepto (ej: '"
                            + expectedName + "')."));
        }

        return violations;
    }
}
