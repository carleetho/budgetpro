package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla para entidades de dominio: No deben tener sufijos como 'Entity' o
 * 'JpaEntity'.
 */
public class DomainEntityRule implements ValidationRule {

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        List<NamingViolation> violations = new ArrayList<>();

        if (className.endsWith("Entity") || className.endsWith("JpaEntity")) {
            String expectedName = className.replaceAll("(?:Jpa)?Entity$", "");
            violations.add(new NamingViolation(filePath, className, expectedName, ViolationSeverity.BLOCKING,
                    "NAMING: Entidad de dominio con sufijo incorrecto",
                    "Las entidades de dominio no deben tener sufijo 'Entity' o 'JpaEntity'. Usa solo el nombre del concepto (ej: '"
                            + expectedName + "')."));
        }

        return violations;
    }
}
