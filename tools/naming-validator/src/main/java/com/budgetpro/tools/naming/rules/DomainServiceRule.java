package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.config.ValidationConfig;
import com.budgetpro.tools.naming.model.NamingViolation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Regla para servicios de dominio: Deben seguir convenciones de nomenclatura.
 */
public class DomainServiceRule extends BaseConfigurableRule {

    public DomainServiceRule(ValidationConfig.RuleConfig config) {
        super(config);
    }

    @Override
    protected ValidationConfig.RuleConfig createDefaultConfig() {
        return new ValidationConfig.RuleConfig(true, "Service", null, "BLOCKING", null);
    }

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(className, "Class name cannot be null");
        List<NamingViolation> violations = new ArrayList<>();
        String expectedSuffix = config.expectedSuffix();

        if (expectedSuffix != null && !className.endsWith(expectedSuffix)) {
            String expectedName = className + expectedSuffix;
            violations.add(new NamingViolation(filePath, className, expectedName, getSeverity(),
                    "NAMING: Servicio de dominio con nombre incorrecto", "Los servicios de dominio deben terminar en '"
                            + expectedSuffix + "'. Ej: '" + expectedName + "'."));
        }

        return violations;
    }
}
