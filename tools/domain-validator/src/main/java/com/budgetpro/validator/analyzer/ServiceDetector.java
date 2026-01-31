package com.budgetpro.validator.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
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
 * Detecta servicios del dominio y casos de uso en el código fuente.
 * 
 * Busca clases con @Service o clases en paquetes service/ o usecase/.
 */
public class ServiceDetector {

    private final JavaParser javaParser;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ServiceDetector.class.getName());

    public ServiceDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todos los servicios en un directorio del dominio.
     * 
     * @param domainPath Ruta al directorio domain/
     * @return Mapa de nombre de servicio -> lista de métodos públicos
     */
    public Map<String, List<String>> detectServices(Path domainPath) {
        Map<String, List<String>> services = new HashMap<>();

        if (domainPath == null || !domainPath.toFile().exists()) {
            return services;
        }

        try {
            scanDirectory(domainPath.toFile(), services);
        } catch (Exception e) {
            LOGGER.severe("Error scanning services: " + e.getMessage());
        }

        return services;
    }

    /**
     * Escanea recursivamente un directorio buscando servicios.
     */
    private void scanDirectory(File directory, Map<String, List<String>> services) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, services);
            } else if (file.getName().endsWith(".java")) {
                try {
                    detectServiceInFile(file, services);
                } catch (Exception e) {
                    // Ignorar archivos que no se pueden parsear
                }
            }
        }
    }

    /**
     * Detecta si un archivo Java contiene un servicio.
     */
    private void detectServiceInFile(File file, Map<String, List<String>> services) throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }

        String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

        // Buscar clases en paquetes service/ o usecase/ o con @Service
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration clazz : classes) {
            boolean isService = false;

            // Verificar si está en paquete service o usecase
            if (packageName.contains("service") || packageName.contains("usecase")) {
                isService = true;
            }

            // Verificar si tiene anotación @Service
            if (clazz.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("Service"))) {
                isService = true;
            }

            // Verificar comentario
            if (clazz.getComment().isPresent()) {
                String comment = clazz.getComment().get().toString().toLowerCase();
                if (comment.contains("service") || comment.contains("use case") || comment.contains("caso de uso")) {
                    isService = true;
                }
            }

            if (isService && !clazz.isInterface()) {
                String serviceName = packageName + "." + clazz.getNameAsString();
                List<String> methods = extractPublicMethods(clazz);
                services.put(serviceName, methods);
            }
        }
    }

    /**
     * Extrae los nombres de los métodos públicos de una clase.
     */
    private List<String> extractPublicMethods(ClassOrInterfaceDeclaration clazz) {
        List<String> methods = new ArrayList<>();

        List<MethodDeclaration> methodDeclarations = clazz.getMethods();
        for (MethodDeclaration method : methodDeclarations) {
            if (method.isPublic() && !method.isStatic()) {
                methods.add(method.getNameAsString());
            }
        }

        return methods;
    }
}
