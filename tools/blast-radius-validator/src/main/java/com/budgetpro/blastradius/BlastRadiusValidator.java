package com.budgetpro.blastradius;

import com.budgetpro.blastradius.output.OutputFormatter;
import com.budgetpro.blastradius.validator.ValidationEngine;
import com.budgetpro.blastradius.validator.ValidationResult;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * CLI tool para validar el blast radius de cambios en módulos BudgetPro.
 * 
 * Exit Codes:
 * - 0: Validación pasada
 * - 1: Validación fallida (límites excedidos)
 * - 2: Error de configuración o Git
 */
@Command(
    name = "blast-radius-validator",
    mixinStandardHelpOptions = true,
    version = "Blast Radius Validator 1.0.0-SNAPSHOT",
    description = "Valida el blast radius de cambios staged en el repositorio Git"
)
public class BlastRadiusValidator implements Callable<Integer> {
    
    @Parameters(
        index = "0",
        description = "Ruta al directorio del repositorio Git (default: .)",
        defaultValue = "."
    )
    private String repositoryPath;
    
    @Option(
        names = {"--config", "-c"},
        description = "Ruta al archivo de configuración JSON (opcional, usa defaults si no se especifica)"
    )
    private String configPath;
    
    @Option(
        names = {"--no-colors"},
        description = "Deshabilitar colores en la salida"
    )
    private boolean noColors;
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new BlastRadiusValidator()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() {
        try {
            // Resolver paths
            Path repoPath = Paths.get(repositoryPath).toAbsolutePath().normalize();
            Path configFilePath = configPath != null 
                ? Paths.get(configPath).toAbsolutePath().normalize()
                : null;
            
            // Validar que el repositorio existe
            if (!repoPath.toFile().exists()) {
                System.err.println("Error: Repository path does not exist: " + repoPath);
                return 2;
            }
            
            // Ejecutar validación
            ValidationEngine engine = new ValidationEngine();
            ValidationResult result = engine.validate(repoPath, configFilePath);
            
            // Formatear y mostrar resultado
            OutputFormatter formatter = new OutputFormatter(!noColors);
            formatter.format(result, System.out);
            
            // Retornar código de salida
            return result.getExitCode();
            
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return 2;
        }
    }
}
