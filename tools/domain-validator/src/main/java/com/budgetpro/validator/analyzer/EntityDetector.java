package com.budgetpro.validator.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
import java.util.stream.Collectors;

/**
 * Detecta entidades del dominio en el código fuente.
 * 
 * Busca clases finales en paquetes model/ que representan agregados raíz o entidades.
 */
public class EntityDetector {
    
    private final JavaParser javaParser;
    
    public EntityDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todas las entidades en un directorio del dominio.
     * 
     * @param domainPath Ruta al directorio domain/ (ej: backend/src/main/java/com/budgetpro/domain)
     * @return Lista de nombres completos de clases detectadas (ej: com.budgetpro.domain.proyecto.model.Proyecto)
     */
    public List<String> detectEntities(Path domainPath) {
        List<String> entities = new ArrayList<>();
        
        if (domainPath == null || !domainPath.toFile().exists()) {
            return entities;
        }
        
        try {
            scanDirectory(domainPath.toFile(), entities);
        } catch (Exception e) {
            // Log error pero continúa
            System.err.println("Error scanning entities: " + e.getMessage());
        }
        
        return entities;
    }

    /**
     * Escanea recursivamente un directorio buscando archivos Java.
     */
    private void scanDirectory(File directory, List<String> entities) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, entities);
            } else if (file.getName().endsWith(".java")) {
                try {
                    String entityName = detectEntityInFile(file);
                    if (entityName != null) {
                        entities.add(entityName);
                    }
                } catch (Exception e) {
                    // Ignorar archivos que no se pueden parsear
                }
            }
        }
    }

    /**
     * Detecta si un archivo Java contiene una entidad del dominio.
     * 
     * Criterios:
     * - Clase final en paquete que contiene "model"
     * - O clase con comentario que menciona "Aggregate Root" o "Entity"
     */
    private String detectEntityInFile(File file) throws FileNotFoundException {
        CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
        if (cu == null) {
            return null;
        }
        
        // Verificar que el paquete contiene "model"
        String packageName = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString())
                .orElse("");
        
        if (!packageName.contains("model") && !packageName.contains("domain")) {
            return null;
        }
        
        // Buscar clases finales o clases que representan entidades
        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        
        for (ClassOrInterfaceDeclaration clazz : classes) {
            // Verificar si es una clase final (patrón común de entidades DDD)
            if (clazz.isFinal() && !clazz.isInterface()) {
                return packageName + "." + clazz.getNameAsString();
            }
            
            // Verificar si tiene comentario que indica que es una entidad
            if (clazz.getComment().isPresent()) {
                String comment = clazz.getComment().get().toString().toLowerCase();
                if (comment.contains("aggregate root") || 
                    comment.contains("entity") ||
                    comment.contains("agregado")) {
                    return packageName + "." + clazz.getNameAsString();
                }
            }
        }
        
        return null;
    }

    /**
     * Detecta relaciones entre entidades basándose en campos que referencian otras entidades.
     * 
     * @param entityPath Ruta al archivo de la entidad
     * @return Set de nombres de clases relacionadas
     */
    public Set<String> detectRelationships(Path entityPath) {
        Set<String> relationships = new HashSet<>();
        
        if (entityPath == null || !entityPath.toFile().exists()) {
            return relationships;
        }
        
        try {
            CompilationUnit cu = javaParser.parse(entityPath.toFile()).getResult().orElse(null);
            if (cu == null) {
                return relationships;
            }
            
            // Buscar campos que puedan ser relaciones
            List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);
            
            for (FieldDeclaration field : fields) {
                String fieldType = field.getCommonType().asString();
                
                // Si el tipo parece ser otra entidad del dominio (no primitivo, no String)
                if (isDomainEntityType(fieldType)) {
                    relationships.add(fieldType);
                }
            }
            
        } catch (Exception e) {
            // Ignorar errores de parsing
        }
        
        return relationships;
    }

    /**
     * Verifica si un tipo parece ser una entidad del dominio.
     */
    private boolean isDomainEntityType(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return false;
        }
        
        // Tipos primitivos y comunes que no son entidades
        Set<String> nonEntities = Set.of(
            "String", "Integer", "Long", "Double", "BigDecimal", 
            "Boolean", "LocalDate", "LocalDateTime", "UUID", "List", "Set", "Map"
        );
        
        if (nonEntities.contains(typeName)) {
            return false;
        }
        
        // Si contiene "Id" probablemente es un Value Object, no una entidad
        if (typeName.endsWith("Id")) {
            return false;
        }
        
        // Si empieza con mayúscula y no es primitivo, podría ser entidad
        return Character.isUpperCase(typeName.charAt(0));
    }
}
