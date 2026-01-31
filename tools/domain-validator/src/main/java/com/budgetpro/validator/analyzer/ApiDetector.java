package com.budgetpro.validator.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
 * Detecta endpoints REST en controladores.
 * 
 * Busca clases con @RestController y extrae métodos
 * con @GetMapping, @PostMapping, etc.
 */
public class ApiDetector {

    private final JavaParser javaParser;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ApiDetector.class.getName());

    public ApiDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todos los endpoints REST en un directorio.
     * 
     * @param infrastructurePath Ruta al directorio infrastructure/rest/
     * @return Mapa de nombre de controlador -> lista de endpoints (ej: "GET
     *         /api/v1/presupuestos")
     */
    public Map<String, List<String>> detectEndpoints(Path infrastructurePath) {
        Map<String, List<String>> endpoints = new HashMap<>();

        if (infrastructurePath == null || !infrastructurePath.toFile().exists()) {
            return endpoints;
        }

        try {
            scanDirectory(infrastructurePath.toFile(), endpoints);
        } catch (Exception e) {
            LOGGER.severe("Error scanning endpoints: " + e.getMessage());
        }

        return endpoints;
    }

    /**
     * Escanea recursivamente un directorio buscando controladores REST.
     */
    private void scanDirectory(File directory, Map<String, List<String>> endpoints) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, endpoints);
            } else if (file.getName().endsWith("Controller.java")) {
                try {
                    detectEndpointsInFile(file, endpoints);
                } catch (Exception e) {
                    // Ignorar archivos que no se pueden parsear
                }
            }
        }
    }

    /**
     * Detecta endpoints REST en un archivo de controlador.
     */
    private void detectEndpointsInFile(File file, Map<String, List<String>> endpoints) throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return;
        }

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration clazz : classes) {
            // Verificar si tiene @RestController
            boolean isRestController = clazz.getAnnotations().stream()
                    .anyMatch(a -> a.getNameAsString().equals("RestController"));

            if (!isRestController) {
                continue;
            }

            String controllerName = clazz.getNameAsString();
            List<String> controllerEndpoints = new ArrayList<>();

            // Obtener base path de @RequestMapping si existe
            String basePath = extractBasePath(clazz);

            // Buscar métodos con anotaciones HTTP
            List<MethodDeclaration> methods = clazz.getMethods();
            for (MethodDeclaration method : methods) {
                String endpoint = extractEndpoint(method, basePath);
                if (endpoint != null) {
                    controllerEndpoints.add(endpoint);
                }
            }

            if (!controllerEndpoints.isEmpty()) {
                endpoints.put(controllerName, controllerEndpoints);
            }
        }
    }

    /**
     * Extrae el path base de @RequestMapping en la clase.
     */
    private String extractBasePath(ClassOrInterfaceDeclaration clazz) {
        return clazz.getAnnotations().stream().filter(a -> a.getNameAsString().equals("RequestMapping")).findFirst()
                .map(a -> {
                    // Intentar extraer el valor del annotation
                    // Simplificado: buscar en el toString
                    String annotationStr = a.toString();
                    if (annotationStr.contains("value")) {
                        // Extraer valor básico (simplificado)
                        int start = annotationStr.indexOf("\"");
                        int end = annotationStr.indexOf("\"", start + 1);
                        if (start > 0 && end > start) {
                            return annotationStr.substring(start + 1, end);
                        }
                    }
                    return "";
                }).orElse("");
    }

    /**
     * Extrae el endpoint de un método con anotación HTTP.
     */
    private String extractEndpoint(MethodDeclaration method, String basePath) {
        String httpMethod = null;
        String path = "";

        // Buscar anotaciones HTTP
        for (AnnotationExpr annotation : method.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (annotationName.equals("GetMapping") || annotationName.equals("PostMapping")
                    || annotationName.equals("PutMapping") || annotationName.equals("DeleteMapping")
                    || annotationName.equals("PatchMapping")) {

                httpMethod = annotationName.replace("Mapping", "").toUpperCase();

                // Intentar extraer path del annotation
                String annotationStr = annotation.toString();
                if (annotationStr.contains("value") || annotationStr.contains("path")) {
                    int start = annotationStr.indexOf("\"");
                    int end = annotationStr.indexOf("\"", start + 1);
                    if (start > 0 && end > start) {
                        path = annotationStr.substring(start + 1, end);
                    }
                }
                break;
            }
        }

        if (httpMethod == null) {
            return null;
        }

        // Construir endpoint completo
        String fullPath = basePath;
        if (!path.isEmpty()) {
            if (!fullPath.isEmpty() && !path.startsWith("/")) {
                fullPath += "/";
            }
            fullPath += path;
        }

        return httpMethod + " " + fullPath;
    }
}
