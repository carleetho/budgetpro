package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla para entidades JPA: Deben terminar con el sufijo 'JpaEntity'.
 */
public class JpaEntityRule implements ValidationRule {

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        List<NamingViolation> violations = new ArrayList<>();

        if (!className.endsWith("JpaEntity")) {
            violations.add(new NamingViolation(filePath, className, className + "JpaEntity", ViolationSeverity.WARNING,
                    "NAMING: Entidad JPA sin sufijo 'JpaEntity'",
                    "Las entidades de persistencia JPA deben terminar con el sufijo 'JpaEntity' para diferenciarlas del dominio."));
        }

        return violations;
    }
}
