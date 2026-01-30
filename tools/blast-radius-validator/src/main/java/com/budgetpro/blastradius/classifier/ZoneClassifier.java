package com.budgetpro.blastradius.classifier;

import com.budgetpro.blastradius.config.BlastRadiusConfig;
import com.budgetpro.blastradius.git.StagedFile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Clasifica archivos staged en zonas de protección (red, yellow, green)
 * basándose en los paths configurados.
 */
public class ZoneClassifier {
    
    /**
     * Clasifica una lista de archivos staged en zonas según la configuración.
     * 
     * El algoritmo de clasificación:
     * 1. Para cada archivo, verifica si su path empieza con algún path de red zone → RED
     * 2. Si no coincide con red zone, verifica yellow zone → YELLOW
     * 3. Si no coincide con ninguna zona configurada → GREEN
     * 
     * El matching es case-sensitive y usa prefix matching (startsWith).
     * El orden de verificación es importante: first match wins.
     * 
     * @param stagedFiles Lista de archivos staged a clasificar
     * @param config Configuración con las rutas de las zonas
     * @return Resultado de clasificación con archivos agrupados por zona
     */
    public ClassifiedFiles classify(List<StagedFile> stagedFiles, BlastRadiusConfig config) {
        Objects.requireNonNull(stagedFiles, "stagedFiles cannot be null");
        Objects.requireNonNull(config, "config cannot be null");
        
        Map<Zone, List<StagedFile>> filesByZone = new EnumMap<>(Zone.class);
        
        // Inicializar listas vacías para cada zona
        filesByZone.put(Zone.RED, new ArrayList<>());
        filesByZone.put(Zone.YELLOW, new ArrayList<>());
        filesByZone.put(Zone.GREEN, new ArrayList<>());
        
        // Clasificar cada archivo
        for (StagedFile file : stagedFiles) {
            Zone zone = classifyFile(file, config);
            filesByZone.get(zone).add(file);
        }
        
        return new ClassifiedFiles(filesByZone);
    }
    
    /**
     * Clasifica un archivo individual en una zona.
     * 
     * @param file Archivo a clasificar
     * @param config Configuración con las rutas de las zonas
     * @return Zona asignada al archivo
     */
    private Zone classifyFile(StagedFile file, BlastRadiusConfig config) {
        String filePath = file.path();
        
        // Verificar red zone primero (first match wins)
        for (String redZonePath : config.getRedZonePaths()) {
            if (filePath.startsWith(redZonePath)) {
                return Zone.RED;
            }
        }
        
        // Verificar yellow zone
        for (String yellowZonePath : config.getYellowZonePaths()) {
            if (filePath.startsWith(yellowZonePath)) {
                return Zone.YELLOW;
            }
        }
        
        // Si no coincide con ninguna zona configurada, es GREEN
        return Zone.GREEN;
    }
}
