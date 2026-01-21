package com.budgetpro.validator;

import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.model.ValidationStatus;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * CLI tool para validar el orden de desarrollo de módulos BudgetPro
 * contra el roadmap canónico del dominio.
 * 
 * Exit Codes:
 * - 0: Validación pasada sin violaciones
 * - 1: Violaciones críticas detectadas (bloquea CI/CD)
 * - 2: Advertencias detectadas (requiere revisión)
 * - 3: Error durante el análisis (estructura inválida)
 */
@Command(
    name = "domain-validator",
    mixinStandardHelpOptions = true,
    version = "Domain Validator 1.0.0",
    description = "Valida el orden de desarrollo de módulos BudgetPro contra el roadmap canónico",
    subcommands = {
        ValidateCommand.class,
        GenerateRoadmapCommand.class,
        CheckModuleCommand.class
    }
)
public class DomainValidator implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DomainValidator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // Si no se especifica un subcomando, mostrar ayuda
        CommandLine.usage(this, System.out);
        return 0;
    }
}

/**
 * Comando para validar el código actual contra el roadmap canónico.
 */
@Command(
    name = "validate",
    description = "Valida la estructura del código contra el roadmap canónico"
)
class ValidateCommand implements Callable<Integer> {

    @Option(
        names = {"--repo-path", "-p"},
        description = "Ruta al directorio del repositorio (default: ./backend)",
        defaultValue = "./backend"
    )
    private String repoPath;

    @Option(
        names = {"--strict", "-s"},
        description = "Modo estricto: bloquea en advertencias además de violaciones críticas",
        defaultValue = "false"
    )
    private boolean strict;

    @Option(
        names = {"--output-format", "-f"},
        description = "Formato de salida: json, markdown, mermaid (default: json)",
        defaultValue = "json"
    )
    private String outputFormat;

    @Option(
        names = {"--output-file", "-o"},
        description = "Archivo de salida (default: stdout)"
    )
    private String outputFile;

    @Override
    public Integer call() {
        try {
            Path repositoryPath = Paths.get(repoPath).toAbsolutePath().normalize();
            
            System.out.println("Validating repository: " + repositoryPath);
            System.out.println("Strict mode: " + strict);
            
            // TODO: Implementar análisis real del código
            // Por ahora, crear un resultado vacío para demostrar la estructura
            ValidationResult result = new ValidationResult(
                repositoryPath.toString(),
                ValidationStatus.PASSED
            );
            
            // Determinar estado basado en violaciones
            if (result.hasCriticalViolations()) {
                result.setStatus(ValidationStatus.CRITICAL_VIOLATIONS);
            } else if (result.hasWarnings() && strict) {
                result.setStatus(ValidationStatus.CRITICAL_VIOLATIONS);
            } else if (result.hasWarnings()) {
                result.setStatus(ValidationStatus.WARNINGS);
            } else {
                result.setStatus(ValidationStatus.PASSED);
            }
            
            // TODO: Implementar generación de salida según formato
            System.out.println("Validation completed: " + result.getStatus());
            System.out.println("Violations: " + result.getViolations().size());
            System.out.println("Module statuses: " + result.getModuleStatuses().size());
            
            return result.getExitCode();
            
        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
            return 3; // ERROR
        }
    }
}

/**
 * Comando para generar el roadmap canónico en múltiples formatos.
 */
@Command(
    name = "generate-roadmap",
    description = "Genera el roadmap canónico en múltiples formatos"
)
class GenerateRoadmapCommand implements Callable<Integer> {

    @Option(
        names = {"--output-dir", "-o"},
        description = "Directorio de salida (default: ./docs/roadmap)",
        defaultValue = "./docs/roadmap"
    )
    private String outputDir;

    @Option(
        names = {"--format", "-f"},
        description = "Formatos a generar: json, markdown, mermaid (default: all)",
        defaultValue = "all"
    )
    private String format;

    @Override
    public Integer call() {
        try {
            Path outputPath = Paths.get(outputDir).toAbsolutePath().normalize();
            
            System.out.println("Generating roadmap to: " + outputPath);
            System.out.println("Formats: " + format);
            
            // TODO: Implementar generación del roadmap canónico
            // Por ahora, solo confirmar que el comando funciona
            System.out.println("Roadmap generation completed (not yet implemented)");
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Error generating roadmap: " + e.getMessage());
            e.printStackTrace();
            return 3; // ERROR
        }
    }
}

/**
 * Comando para verificar el estado de un módulo específico.
 */
@Command(
    name = "check-module",
    description = "Verifica el estado de implementación de un módulo específico"
)
class CheckModuleCommand implements Callable<Integer> {

    @Parameters(
        index = "0",
        description = "ID del módulo a verificar (ej: presupuesto, compras, tiempo)"
    )
    private String moduleName;

    @Option(
        names = {"--repo-path", "-p"},
        description = "Ruta al directorio del repositorio (default: ./backend)",
        defaultValue = "./backend"
    )
    private String repoPath;

    @Option(
        names = {"--show-dependencies", "-d"},
        description = "Mostrar dependencias del módulo",
        defaultValue = "false"
    )
    private boolean showDependencies;

    @Override
    public Integer call() {
        try {
            Path repositoryPath = Paths.get(repoPath).toAbsolutePath().normalize();
            
            System.out.println("Checking module: " + moduleName);
            System.out.println("Repository: " + repositoryPath);
            System.out.println("Show dependencies: " + showDependencies);
            
            // TODO: Implementar verificación real del módulo
            // Por ahora, solo confirmar que el comando funciona
            System.out.println("Module check completed (not yet implemented)");
            System.out.println("Module: " + moduleName);
            
            return 0;
            
        } catch (Exception e) {
            System.err.println("Error checking module: " + e.getMessage());
            e.printStackTrace();
            return 3; // ERROR
        }
    }
}
