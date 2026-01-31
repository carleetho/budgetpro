package com.budgetpro.tools.naming.config;

import java.util.List;
import java.util.Map;

/**
 * Configuración para el validador de nombres.
 */
public record ValidationConfig(Map<String, LayerPatterns> layers, Map<String, RuleConfig> rules,
                List<String> exclusions) {
        public record LayerPatterns(List<String> pathPatterns, List<String> classNamePatterns) {
                public LayerPatterns {
                        if (pathPatterns == null)
                                pathPatterns = List.of();
                        if (classNamePatterns == null)
                                classNamePatterns = List.of();
                }
        }

        public record RuleConfig(boolean enabled, String expectedSuffix, String expectedPackage, String severity,
                        List<String> forbiddenSuffixes) {
                public RuleConfig {
                        if (forbiddenSuffixes == null)
                                forbiddenSuffixes = List.of();
                }
        }
}
