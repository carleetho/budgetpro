package com.budgetpro.validator.boundary.report;

import com.budgetpro.validator.boundary.BoundaryMatcher;
import com.budgetpro.validator.boundary.config.BoundaryConfig;
import com.budgetpro.validator.boundary.scanner.DomainScanner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Orquestador de la validación de fronteras y reporte de violaciones.
 */
public class ViolationReporter {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ViolationReporter.class.getName());
    private final DomainScanner simpleScanner;
    private final BoundaryMatcher matcher;
    private final BoundaryConfig config;

    public ViolationReporter(DomainScanner scanner, BoundaryConfig config) {
        this.simpleScanner = scanner;
        this.config = config;
        this.matcher = new BoundaryMatcher(config.forbiddenImports(), config.allowedStandardLibs());
    }

    /**
     * Valida la capa de dominio en el directorio especificado.
     */
    public List<BoundaryViolation> validateDomain(Path domainRoot) throws IOException {
        List<BoundaryViolation> violations = new ArrayList<>();
        if (!config.enabled()) {
            return violations;
        }

        List<Path> javaFiles = simpleScanner.scanJavaFiles(domainRoot);

        for (Path file : javaFiles) {
            Collection<String> itemsToValidate = simpleScanner.extractImports(file);

            for (String item : itemsToValidate) {
                if (matcher.isForbidden(item)) {
                    violations.add(new BoundaryViolation(file, item,
                            "Violación de frontera arquitectónica detectada. La capa de dominio debe ser independiente.",
                            config.severity()));
                }
            }
        }

        return violations;
    }

    /**
     * Imprime el informe de violaciones en la consola.
     */
    public void reportViolations(List<BoundaryViolation> violations) {
        if (violations.isEmpty()) {
            LOGGER.info("✅ Capa de dominio limpia. No se detectaron violaciones de fronteras.");
            return;
        }

        String severityLabel = "[" + config.severity() + "]";
        LOGGER.severe("❌ Se detectaron " + violations.size() + " violaciones de fronteras arquitectónicas:");
        for (BoundaryViolation v : violations) {
            LOGGER.severe(String.format("  - %s [%s]: %s -> %s%n", severityLabel, v.file().toString(),
                    v.forbiddenImport(), v.message()));
        }
    }
}
