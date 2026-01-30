package com.budgetpro.tools.naming.layer;

import java.nio.file.Path;

/**
 * Detecta la capa arquitectónica de un archivo Java basándose en su ruta y
 * nombre de clase.
 */
public class LayerDetector {

    /**
     * Identifica la capa arquitectónica del archivo especificado.
     * 
     * @param filePath  Ruta del archivo Java.
     * @param className Nombre de la clase analizada.
     * @return ArchitecturalLayer detectada.
     */
    public ArchitecturalLayer detectLayer(Path filePath, String className) {
        if (filePath == null || className == null) {
            return ArchitecturalLayer.UNKNOWN;
        }

        String pathStr = filePath.toString().replace('\\', '/').toLowerCase();
        String classNameLower = className.toLowerCase();

        // 1. JPA Entity: /infrastructure/persistence/entity/
        if (pathStr.contains("/infrastructure/persistence/entity/")) {
            return ArchitecturalLayer.JPA_ENTITY;
        }

        // 2. Mapper: /mapper/ en ruta O "Mapper" en nombre de clase
        if (pathStr.contains("/mapper/") || className.contains("Mapper")) {
            return ArchitecturalLayer.MAPPER;
        }

        // 3. Value Object: /domain/ Y /valueobjects/
        if (pathStr.contains("/domain/") && pathStr.contains("/valueobjects/")) {
            return ArchitecturalLayer.VALUE_OBJECT;
        }

        // 4. Domain Service: /domain/ Y nombre termina en "Service" (o contiene Service
        // según requerimiento)
        if (pathStr.contains("/domain/") && className.contains("Service")) {
            return ArchitecturalLayer.DOMAIN_SERVICE;
        }

        // 5. Domain Entity: /domain/ Y (/entities/ O /model/)
        if (pathStr.contains("/domain/") && (pathStr.contains("/entities/") || pathStr.contains("/model/"))) {
            return ArchitecturalLayer.DOMAIN_ENTITY;
        }

        return ArchitecturalLayer.UNKNOWN;
    }
}
