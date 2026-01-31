package com.budgetpro.tools.naming.layer;

import com.budgetpro.tools.naming.config.ValidationConfig;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Detecta la capa arquitectónica de un archivo Java basándose en su ruta y
 * nombre de clase y una configuración externa.
 */
public class LayerDetector {
    private final ValidationConfig config;

    public LayerDetector(ValidationConfig config) {
        this.config = config;
    }

    /**
     * Identifica la capa arquitectónica del archivo especificado.
     * 
     * @param filePath  Ruta del archivo Java.
     * @param className Nombre de la clase analizada.
     * @return ArchitecturalLayer detectada.
     */
    public ArchitecturalLayer detectLayer(Path filePath, String className) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        Objects.requireNonNull(className, "Class name cannot be null");

        if (config == null || config.layers() == null) {
            return ArchitecturalLayer.UNKNOWN;
        }

        String pathStr = filePath.toString().replace('\\', '/');

        for (ArchitecturalLayer layer : ArchitecturalLayer.values()) {
            if (layer == ArchitecturalLayer.UNKNOWN)
                continue;

            ValidationConfig.LayerPatterns patterns = config.layers().get(layer.name());
            if (patterns == null)
                continue;

            // 1. Chequear patrones de ruta
            if (matchesAny(pathStr, patterns.pathPatterns()) || matchesAny(className, patterns.classNamePatterns())) {
                return layer;
            }
        }

        return ArchitecturalLayer.UNKNOWN;
    }

    private boolean matchesAny(String target, List<String> patterns) {
        if (patterns == null)
            return false;
        for (String pattern : patterns) {
            if (target.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
