package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla para Mappers: Deben terminar con el sufijo 'Mapper'.
 */
public class MapperRule implements ValidationRule {

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        List<NamingViolation> violations = new ArrayList<>();

        if (!className.endsWith("Mapper")) {
            violations.add(new NamingViolation(filePath, className, className + "Mapper", ViolationSeverity.WARNING,
                    "NAMING: Mapper sin sufijo 'Mapper'",
                    "Las clases que actúan como mappers deben terminar con el sufijo 'Mapper'."));
        }

        return violations;
    }
}
