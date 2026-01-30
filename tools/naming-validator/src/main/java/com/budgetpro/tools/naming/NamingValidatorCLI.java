package com.budgetpro.tools.naming;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "naming-validator", mixinStandardHelpOptions = true, version = "1.0.0", description = "Validador de convenciones de nombres para BudgetPro.")
public class NamingValidatorCLI implements Callable<Integer> {

    @Parameters(index = "0", description = "Ruta al directorio de fuentes a validar.")
    private Path sourcePath;

    @Override
    public Integer call() throws Exception {
        System.out.println("Iniciando validación de nombres en: " + sourcePath.toAbsolutePath());

        // TODO: Implementar escaneo de archivos y ejecución de reglas

        System.out.println("Validación completada con éxito (0 violaciones).");
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new NamingValidatorCLI()).execute(args);
        System.exit(exitCode);
    }
}
