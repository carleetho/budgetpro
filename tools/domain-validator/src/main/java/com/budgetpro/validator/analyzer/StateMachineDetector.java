package com.budgetpro.validator.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detecta máquinas de estado (enums) en el código fuente.
 * 
 * Busca enums que representan estados de entidades (ej: EstadoPresupuesto,
 * EstadoProyecto).
 */
public class StateMachineDetector {

    private final JavaParser javaParser;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(StateMachineDetector.class.getName());

    public StateMachineDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todas las máquinas de estado (enums) en un directorio.
     * 
     * @param domainPath Ruta al directorio domain/
     * @return Mapa de nombre de enum -> lista de valores (estados)
     */
    public Map<String, List<String>> detectStateMachines(Path domainPath) {
        Map<String, List<String>> stateMachines = new HashMap<>();

        if (domainPath == null || !domainPath.toFile().exists()) {
            return stateMachines;
        }

        try {
            scanDirectory(domainPath.toFile(), stateMachines);
        } catch (Exception e) {
            LOGGER.severe("Error scanning state machines: " + e.getMessage());
        }

        return stateMachines;
    }

    /**
     * Escanea recursivamente un directorio buscando enums de estado.
     */
    private void scanDirectory(File directory, Map<String, List<String>> stateMachines) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, stateMachines);
            } else if (file.getName().endsWith(".java")) {
                try {
                    detectStateMachineInFile(file, stateMachines);
                } catch (Exception e) {
                    // Ignorar archivos que no se pueden parsear
                }
            }
        }
    }

    /**
     * Detecta si un archivo Java contiene un enum de estado.
     */
    private void detectStateMachineInFile(File file, Map<String, List<String>> stateMachines)
            throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }

        String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        // Buscar enums que parezcan ser máquinas de estado
        List<EnumDeclaration> enums = cu.findAll(EnumDeclaration.class);

        for (EnumDeclaration enumDecl : enums) {
            String enumName = enumDecl.getNameAsString();

            // Verificar si el nombre sugiere que es un estado
            boolean isStateMachine = enumName.toLowerCase().contains("estado")
                    || enumName.toLowerCase().contains("state") || enumName.toLowerCase().endsWith("status");

            // Verificar comentario
            if (!isStateMachine && enumDecl.getComment().isPresent()) {
                String comment = enumDecl.getComment().get().toString().toLowerCase();
                if (comment.contains("estado") || comment.contains("state") || comment.contains("máquina de estado")
                        || comment.contains("state machine")) {
                    isStateMachine = true;
                }
            }

            if (isStateMachine) {
                List<String> values = new ArrayList<>();
                for (EnumConstantDeclaration constant : enumDecl.getEntries()) {
                    values.add(constant.getNameAsString());
                }

                String fullName = packageName + "." + enumName;
                // Guardar tanto con FQN como con nombre simple para búsquedas flexibles
                stateMachines.put(fullName, values);
                stateMachines.put(enumName, values); // También por nombre simple
            }

            // También detectar todos los enums (no solo state machines) para enum_exists
            // Esto permite detectar enums como NaturalezaGasto
            List<String> enumValues = new ArrayList<>();
            for (EnumConstantDeclaration constant : enumDecl.getEntries()) {
                enumValues.add(constant.getNameAsString());
            }
            String fullEnumName = packageName + "." + enumName;
            // Guardar con FQN y nombre simple
            if (!stateMachines.containsKey(fullEnumName)) {
                stateMachines.put(fullEnumName, enumValues);
            }
            if (!stateMachines.containsKey(enumName)) {
                stateMachines.put(enumName, enumValues);
            }
        }
    }
}
