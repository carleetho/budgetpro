package com.budgetpro.validator.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detecta puntos de integración: repositorios (ports) y adaptadores.
 * 
 * Busca interfaces en paquetes port/ y clases en paquetes adapter/.
 */
public class IntegrationPointDetector {

    private final JavaParser javaParser;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(IntegrationPointDetector.class.getName());

    public IntegrationPointDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todos los repositorios (ports) en un directorio.
     * 
     * @param domainPath Ruta al directorio domain/
     * @return Lista de nombres completos de interfaces de repositorio
     */
    public List<String> detectRepositories(Path domainPath) {
        List<String> repositories = new ArrayList<>();

        if (domainPath == null || !domainPath.toFile().exists()) {
            return repositories;
        }

        try {
            scanDirectory(domainPath.toFile(), repositories, true);
        } catch (Exception e) {
            LOGGER.severe("Error scanning repositories: " + e.getMessage());
        }

        return repositories;
    }

    /**
     * Detecta todos los adaptadores en un directorio.
     * 
     * @param infrastructurePath Ruta al directorio infrastructure/
     * @return Lista de nombres completos de clases adaptador
     */
    public List<String> detectAdapters(Path infrastructurePath) {
        List<String> adapters = new ArrayList<>();

        if (infrastructurePath == null || !infrastructurePath.toFile().exists()) {
            return adapters;
        }

        try {
            scanDirectory(infrastructurePath.toFile(), adapters, false);
        } catch (Exception e) {
            LOGGER.severe("Error scanning adapters: " + e.getMessage());
        }

        return adapters;
    }

    /**
     * Escanea recursivamente un directorio buscando ports o adapters.
     */
    private void scanDirectory(File directory, List<String> results, boolean lookingForPorts) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, results, lookingForPorts);
            } else if (file.getName().endsWith(".java")) {
                try {
                    if (lookingForPorts) {
                        detectRepositoryInFile(file, results);
                    } else {
                        detectAdapterInFile(file, results);
                    }
                } catch (Exception e) {
                    // Ignorar archivos que no se pueden parsear
                }
            }
        }
    }

    /**
     * Detecta si un archivo contiene una interfaz de repositorio (port).
     */
    private void detectRepositoryInFile(File file, List<String> repositories) throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }

        String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        // Buscar interfaces en paquetes port/
        if (!packageName.contains("port")) {
            return;
        }

        List<ClassOrInterfaceDeclaration> interfaces = cu.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration iface : interfaces) {
            if (iface.isInterface()) {
                String interfaceName = iface.getNameAsString();

                // Verificar si parece ser un repositorio
                boolean isRepository = interfaceName.endsWith("Repository") || interfaceName.endsWith("Port")
                        || interfaceName.endsWith("Adapter");

                // Verificar comentario
                if (!isRepository && iface.getComment().isPresent()) {
                    String comment = iface.getComment().get().toString().toLowerCase();
                    if (comment.contains("repository") || comment.contains("port") || comment.contains("repositorio")) {
                        isRepository = true;
                    }
                }

                if (isRepository) {
                    repositories.add(packageName + "." + interfaceName);
                }
            }
        }
    }

    /**
     * Detecta si un archivo contiene una clase adaptador.
     */
    private void detectAdapterInFile(File file, List<String> adapters) throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }

        String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        // Buscar clases en paquetes adapter/ o persistence/
        if (!packageName.contains("adapter") && !packageName.contains("persistence")) {
            return;
        }

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration clazz : classes) {
            if (!clazz.isInterface() && !clazz.isAbstract()) {
                String className = clazz.getNameAsString();

                // Verificar si parece ser un adaptador
                boolean isAdapter = className.endsWith("Adapter") || className.endsWith("Repository")
                        || className.endsWith("Entity");

                // Verificar comentario
                if (!isAdapter && clazz.getComment().isPresent()) {
                    String comment = clazz.getComment().get().toString().toLowerCase();
                    if (comment.contains("adapter") || comment.contains("adaptador") || comment.contains("repository")
                            || comment.contains("persistence")) {
                        isAdapter = true;
                    }
                }

                if (isAdapter) {
                    adapters.add(packageName + "." + className);
                }
            }
        }
    }
}
