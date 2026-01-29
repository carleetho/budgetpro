package com.budgetpro.validator.boundary.report;

import com.budgetpro.validator.boundary.BoundaryMatcher;
import com.budgetpro.validator.boundary.scanner.DomainScanner;
import com.budgetpro.validator.boundary.config.BoundaryConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador de la validación de fronteras y reporte de violaciones.
 */
public class ViolationReporter {

    private final DomainScanner scanner;
    private final BoundaryMatcher matcher;

    public ViolationReporter(DomainScanner scanner, BoundaryConfig config) {
        this.scanner = scanner;
        this.matcher = new BoundaryMatcher(config.forbiddenImports(), config.allowedStandardLibs());
    }

    /**
     * Valida la capa de dominio en el directorio especificado.
     * 
     * @param domainRoot Raíz de la capa de dominio.
     * @return Lista de violaciones encontradas.
     * @throws IOException Si hay error escaneando archivos.
     */
    public List<BoundaryViolation> validateDomain(Path domainRoot) throws IOException {
        List<BoundaryViolation> violations = new ArrayList<>();
        List<Path> javaFiles = scanner.scanJavaFiles(domainRoot);

        for (Path file : javaFiles) {
            List<String> imports = scanner.extractImports(file);
            for (String importPath : imports) {
                if (matcher.isForbidden(importPath)) {
                    violations.add(new BoundaryViolation(file, importPath,
                            "Elimina el import. Domain debe ser independiente de frameworks e infraestructura (Hexagonal Architecture)."));
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
            System.out.println("✅ Capa de dominio limpia. No se detectaron violaciones de fronteras.");
            return;
        }

        System.err.println("❌ Se detectaron " + violations.size() + " violaciones de fronteras arquitectónicas:");
        for (BoundaryViolation v : violations) {
            System.err.printf("  - [%s]: %s -> %s%n", v.file().toString(), v.forbiddenImport(), v.message());
        }
    }
}
