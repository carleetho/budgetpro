package com.budgetpro.tools.naming.engine;

import com.budgetpro.tools.naming.layer.ArchitecturalLayer;
import com.budgetpro.tools.naming.layer.LayerDetector;
import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.scanner.ClassDeclarationExtractor;
import com.budgetpro.tools.naming.scanner.JavaFileScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Motor de validación que coordina el escaneo, detección de capas y ejecución
 * de reglas.
 */
public class ValidationEngine {

    private final JavaFileScanner scanner;
    private final ClassDeclarationExtractor extractor;
    private final LayerDetector detector;
    private final Map<ArchitecturalLayer, ValidationRule> rules;

    public ValidationEngine(JavaFileScanner scanner, ClassDeclarationExtractor extractor, LayerDetector detector,
            Map<ArchitecturalLayer, ValidationRule> rules) {
        this.scanner = scanner;
        this.extractor = extractor;
        this.detector = detector;
        this.rules = rules;
    }

    /**
     * Valida recursivamente todos los archivos Java en la ruta raíz.
     * 
     * @param rootPath Directorio raíz para escanear.
     * @return Resultado de la validación con todas las violaciones encontradas.
     */
    public ValidationResult validate(Path rootPath) {
        List<NamingViolation> allViolations = new ArrayList<>();

        try {
            List<Path> javaFiles = scanner.scanJavaFiles(rootPath);

            for (Path filePath : javaFiles) {
                try {
                    processFile(filePath, allViolations);
                } catch (Exception e) {
                    System.err.println("Error procesando archivo " + filePath + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error durante el escaneo de archivos: " + e.getMessage());
        }

        return new ValidationResult(allViolations);
    }

    private void processFile(Path filePath, List<NamingViolation> allViolations) throws IOException {
        String content = Files.readString(filePath);
        Optional<String> classNameOpt = extractor.extractClassName(content);

        if (classNameOpt.isEmpty()) {
            // No se pudo extraer el nombre de la clase, se omite el archivo
            return;
        }

        String className = classNameOpt.get();
        ArchitecturalLayer layer = detector.detectLayer(filePath, className);

        if (layer == ArchitecturalLayer.UNKNOWN) {
            // Capa desconocida, no se aplican reglas de nomenclatura específicas
            return;
        }

        ValidationRule rule = rules.get(layer);
        if (rule != null) {
            List<NamingViolation> ruleViolations = rule.validate(filePath, className);
            allViolations.addAll(ruleViolations);
        }
    }
}
