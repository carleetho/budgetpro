package com.budgetpro.tools.naming;

import com.budgetpro.tools.naming.config.ConfigLoader;
import com.budgetpro.tools.naming.config.ValidationConfig;
import com.budgetpro.tools.naming.engine.ValidationEngine;
import com.budgetpro.tools.naming.engine.ValidationResult;
import com.budgetpro.tools.naming.layer.ArchitecturalLayer;
import com.budgetpro.tools.naming.layer.LayerDetector;
import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;
import com.budgetpro.tools.naming.rules.*;
import com.budgetpro.tools.naming.scanner.ClassDeclarationExtractor;
import com.budgetpro.tools.naming.scanner.JavaFileScanner;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "naming-validator", mixinStandardHelpOptions = true, version = "1.0.0", description = "Validador de convenciones de nombres para BudgetPro.")
public class NamingValidatorCLI implements Callable<Integer> {

    @Parameters(index = "0", description = "Ruta al directorio de fuentes a validar.")
    private Path sourcePath;

    @Option(names = { "-c", "--config" }, description = "Ruta al archivo de configuración (YAML).")
    private Path configPath;

    @Override
    public Integer call() throws Exception {
        // Log: Starting validation in sourcePath

        // 1. Cargar Configuración
        ValidationConfig config = loadConfig();

        // 2. Configurar reglas por capa
        Map<ArchitecturalLayer, ValidationRule> rules = new HashMap<>();
        rules.put(ArchitecturalLayer.DOMAIN_ENTITY, new DomainEntityRule(getRuleConfig(config, "DOMAIN_ENTITY")));
        rules.put(ArchitecturalLayer.JPA_ENTITY, new JpaEntityRule(getRuleConfig(config, "JPA_ENTITY")));
        rules.put(ArchitecturalLayer.MAPPER, new MapperRule(getRuleConfig(config, "MAPPER")));
        rules.put(ArchitecturalLayer.VALUE_OBJECT, new ValueObjectRule(getRuleConfig(config, "VALUE_OBJECT")));
        rules.put(ArchitecturalLayer.DOMAIN_SERVICE, new DomainServiceRule(getRuleConfig(config, "DOMAIN_SERVICE")));

        // 3. Inicializar motor
        ValidationEngine engine = new ValidationEngine(new JavaFileScanner(), new ClassDeclarationExtractor(),
                new LayerDetector(config), rules);

        // 4. Ejecutar validación
        ValidationResult result = engine.validate(sourcePath);

        // 5. Reportar resultados
        for (NamingViolation violation : result.getAllViolations()) {
            String prefix = violation.severity() == ViolationSeverity.BLOCKING ? "[BLOCKING]" : "[WARNING]";
            // Log violation: prefix + filePath + message
            // Log suggestion: suggestion
        }

        if (result.hasBlockingViolations()) {
            // Log: Validation failed with blocking violations
            return 1;
        }

        if (result.getTotalViolationCount() > 0) {
            // Validation completed successfully
        } else {
            // No violations found
        }

        return 0;
    }

    private ValidationConfig loadConfig() {
        if (configPath != null) {
            try {
                return new ConfigLoader().load(configPath);
            } catch (IOException e) {
                // Return default config if loading fails
            }
        }
        return createDefaultConfig();
    }

    private ValidationConfig.RuleConfig getRuleConfig(ValidationConfig config, String layerName) {
        if (config == null || config.rules() == null)
            return null;
        return config.rules().get(layerName);
    }

    private ValidationConfig createDefaultConfig() {
        // Configuración de capas por defecto
        Map<String, ValidationConfig.LayerPatterns> layers = new HashMap<>();
        layers.put("JPA_ENTITY",
                new ValidationConfig.LayerPatterns(List.of("/infrastructure/persistence/entity/"), null));
        layers.put("MAPPER", new ValidationConfig.LayerPatterns(List.of("/mapper/"), List.of("Mapper")));
        layers.put("VALUE_OBJECT", new ValidationConfig.LayerPatterns(List.of("/domain/", "/valueobjects/"), null));
        layers.put("DOMAIN_SERVICE", new ValidationConfig.LayerPatterns(List.of("/domain/"), List.of("Service")));
        layers.put("DOMAIN_ENTITY",
                new ValidationConfig.LayerPatterns(List.of("/domain/"), List.of("/entities/", "/model/")));

        return new ValidationConfig(layers, new HashMap<>(), List.of());
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new NamingValidatorCLI()).execute(args);
        System.exit(exitCode);
    }
}
