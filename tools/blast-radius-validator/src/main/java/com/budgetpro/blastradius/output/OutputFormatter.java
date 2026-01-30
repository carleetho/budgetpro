package com.budgetpro.blastradius.output;

import com.budgetpro.blastradius.classifier.ClassifiedFiles;
import com.budgetpro.blastradius.classifier.Zone;
import com.budgetpro.blastradius.git.StagedFile;
import com.budgetpro.blastradius.validator.ValidationResult;
import com.budgetpro.blastradius.validator.Violation;

import java.io.PrintStream;
import java.util.List;

/**
 * Formatea la salida de validación en formato legible por humanos.
 */
public class OutputFormatter {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BOLD = "\u001B[1m";
    
    private final boolean useColors;
    
    public OutputFormatter() {
        this.useColors = System.console() != null && 
                        !"false".equalsIgnoreCase(System.getProperty("blastradius.colors", "true"));
    }
    
    public OutputFormatter(boolean useColors) {
        this.useColors = useColors;
    }
    
    /**
     * Formatea y escribe el resultado de validación a un PrintStream.
     * 
     * @param result Resultado de validación
     * @param out Stream de salida
     */
    public void format(ValidationResult result, PrintStream out) {
        if (result.isError()) {
            formatError(result, out);
            return;
        }
        
        ClassifiedFiles classifiedFiles = result.getClassifiedFiles();
        
        // Encabezado
        out.println();
        out.println(bold("=== Blast Radius Validation ==="));
        out.println();
        
        // Resumen de archivos
        formatFileSummary(classifiedFiles, out);
        out.println();
        
        // Override status
        if (result.isOverrideDetected()) {
            out.println(green("✓ Override keyword detected - all validations skipped"));
            out.println();
        }
        
        // Resultado
        if (result.isSuccess()) {
            formatSuccess(out);
        } else {
            formatViolations(result.getViolations(), out);
        }
        
        out.println();
    }
    
    private void formatFileSummary(ClassifiedFiles classifiedFiles, PrintStream out) {
        out.println("Files staged: " + bold(String.valueOf(classifiedFiles.getTotalCount())));
        out.println("  " + red("Red zone:   ") + classifiedFiles.getCount(Zone.RED));
        out.println("  " + yellow("Yellow zone: ") + classifiedFiles.getCount(Zone.YELLOW));
        out.println("  " + green("Green zone:  ") + classifiedFiles.getCount(Zone.GREEN));
    }
    
    private void formatSuccess(PrintStream out) {
        out.println(green("✓ Validation PASSED"));
        out.println(green("  All limits respected"));
    }
    
    private void formatViolations(List<Violation> violations, PrintStream out) {
        out.println(red("✗ Validation FAILED"));
        out.println();
        out.println(red("Violations detected:"));
        out.println();
        
        for (Violation violation : violations) {
            formatViolation(violation, out);
        }
    }
    
    private void formatViolation(Violation violation, PrintStream out) {
        out.println(red("  • " + violation.getMessage()));
        
        if (violation.getZone() != null) {
            out.println("    Zone: " + getZoneColor(violation.getZone()) + violation.getZone());
        }
        
        List<StagedFile> files = violation.getViolatingFiles();
        if (!files.isEmpty() && files.size() <= 10) {
            out.println("    Files:");
            for (StagedFile file : files) {
                out.println("      - " + file.path());
            }
        } else if (files.size() > 10) {
            out.println("    Files (" + files.size() + " total):");
            for (int i = 0; i < 10; i++) {
                out.println("      - " + files.get(i).path());
            }
            out.println("      ... and " + (files.size() - 10) + " more");
        }
        
        out.println();
    }
    
    private void formatError(ValidationResult result, PrintStream out) {
        out.println();
        out.println(red("✗ ERROR"));
        out.println();
        out.println(red(result.getErrorMessage()));
        out.println();
    }
    
    private String bold(String text) {
        return useColors ? ANSI_BOLD + text + ANSI_RESET : text;
    }
    
    private String red(String text) {
        return useColors ? ANSI_RED + text + ANSI_RESET : text;
    }
    
    private String green(String text) {
        return useColors ? ANSI_GREEN + text + ANSI_RESET : text;
    }
    
    private String yellow(String text) {
        return useColors ? ANSI_YELLOW + text + ANSI_RESET : text;
    }
    
    private String getZoneColor(Zone zone) {
        switch (zone) {
            case RED:
                return useColors ? ANSI_RED : "";
            case YELLOW:
                return useColors ? ANSI_YELLOW : "";
            case GREEN:
                return useColors ? ANSI_GREEN : "";
            default:
                return "";
        }
    }
}
