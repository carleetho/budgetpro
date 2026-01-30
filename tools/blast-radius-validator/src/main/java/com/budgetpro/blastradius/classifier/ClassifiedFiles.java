package com.budgetpro.blastradius.classifier;

import com.budgetpro.blastradius.git.StagedFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resultado de la clasificación de archivos por zonas.
 * Contiene los archivos agrupados por zona y métodos para obtener conteos.
 */
public class ClassifiedFiles {
    
    private final Map<Zone, List<StagedFile>> filesByZone;
    
    public ClassifiedFiles(Map<Zone, List<StagedFile>> filesByZone) {
        Objects.requireNonNull(filesByZone, "filesByZone cannot be null");
        
        // Crear copias inmutables de las listas
        this.filesByZone = new EnumMap<>(Zone.class);
        for (Map.Entry<Zone, List<StagedFile>> entry : filesByZone.entrySet()) {
            this.filesByZone.put(
                entry.getKey(),
                Collections.unmodifiableList(new ArrayList<>(entry.getValue()))
            );
        }
        
        // Asegurar que todas las zonas estén presentes
        for (Zone zone : Zone.values()) {
            this.filesByZone.putIfAbsent(zone, Collections.emptyList());
        }
    }
    
    /**
     * Obtiene el número de archivos en una zona específica.
     * 
     * @param zone Zona a consultar
     * @return Número de archivos en la zona
     */
    public int getCount(Zone zone) {
        Objects.requireNonNull(zone, "zone cannot be null");
        return filesByZone.getOrDefault(zone, Collections.emptyList()).size();
    }
    
    /**
     * Obtiene el número total de archivos clasificados.
     * 
     * @return Número total de archivos
     */
    public int getTotalCount() {
        return filesByZone.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Obtiene la lista de archivos en una zona específica.
     * La lista retornada es inmutable.
     * 
     * @param zone Zona a consultar
     * @return Lista inmutable de archivos en la zona
     */
    public List<StagedFile> getFiles(Zone zone) {
        Objects.requireNonNull(zone, "zone cannot be null");
        return filesByZone.getOrDefault(zone, Collections.emptyList());
    }
    
    /**
     * Obtiene el mapa completo de archivos por zona.
     * El mapa y las listas son inmutables.
     * 
     * @return Mapa inmutable de zonas a listas de archivos
     */
    public Map<Zone, List<StagedFile>> getAllFiles() {
        return Collections.unmodifiableMap(filesByZone);
    }
    
    /**
     * Verifica si hay archivos en una zona específica.
     * 
     * @param zone Zona a verificar
     * @return true si hay al menos un archivo en la zona, false en caso contrario
     */
    public boolean hasFiles(Zone zone) {
        return getCount(zone) > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassifiedFiles that = (ClassifiedFiles) o;
        return Objects.equals(filesByZone, that.filesByZone);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filesByZone);
    }
    
    @Override
    public String toString() {
        return "ClassifiedFiles{" +
                "RED=" + getCount(Zone.RED) +
                ", YELLOW=" + getCount(Zone.YELLOW) +
                ", GREEN=" + getCount(Zone.GREEN) +
                ", total=" + getTotalCount() +
                '}';
    }
}
