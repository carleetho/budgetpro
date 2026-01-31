package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.config.ValidationConfig;
import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase base para reglas que utilizan configuración externa.
 */
public abstract class BaseConfigurableRule implements ValidationRule {
    protected final ValidationConfig.RuleConfig config;

    protected BaseConfigurableRule(ValidationConfig.RuleConfig config) {
        this.config = config != null ? config : createDefaultConfig();
    }

    protected abstract ValidationConfig.RuleConfig createDefaultConfig();

    protected ViolationSeverity getSeverity() {
        if (config == null || config.severity() == null) {
            return ViolationSeverity.BLOCKING;
        }
        return "WARNING".equalsIgnoreCase(config.severity()) ? ViolationSeverity.WARNING : ViolationSeverity.BLOCKING;
    }

    protected List<NamingViolation> checkForbiddenSuffixes(Path filePath, String className, List<String> forbidden) {
        List<NamingViolation> violations = new ArrayList<>();
        if (forbidden == null)
            return violations;

        String longestMatch = null;
        for (String suffix : forbidden) {
            if (className.endsWith(suffix)) {
                if (longestMatch == null || suffix.length() > longestMatch.length()) {
                    longestMatch = suffix;
                }
            }
        }

        if (longestMatch != null) {
            String expectedName = className.substring(0, className.length() - longestMatch.length());
            violations.add(new NamingViolation(filePath, className, expectedName, getSeverity(),
                    "NAMING: Sufijo prohibido detectado",
                    "La clase no debe terminar en '" + longestMatch + "'. Sugerencia: '" + expectedName + "'"));
        }
        return violations;
    }
}
