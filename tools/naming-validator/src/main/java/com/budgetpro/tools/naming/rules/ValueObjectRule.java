package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.config.ValidationConfig;
import com.budgetpro.tools.naming.model.NamingViolation;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Regla para Value Objects: No deben tener sufijos como 'VO' o 'ValueObject'.
 */
public class ValueObjectRule extends BaseConfigurableRule {

    public ValueObjectRule(ValidationConfig.RuleConfig config) {
        super(config);
    }

    @Override
    protected ValidationConfig.RuleConfig createDefaultConfig() {
        return new ValidationConfig.RuleConfig(true, null, null, "BLOCKING", List.of("VO", "ValueObject"));
    }

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(className, "Class name cannot be null");
        return checkForbiddenSuffixes(filePath, className, config.forbiddenSuffixes());
    }
}
