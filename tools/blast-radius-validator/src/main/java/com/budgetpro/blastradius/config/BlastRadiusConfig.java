package com.budgetpro.blastradius.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configuración para la validación de blast radius.
 * Define los límites y zonas para controlar el alcance de los cambios.
 */
public class BlastRadiusConfig {
    
    private static final int DEFAULT_MAX_FILES_WITHOUT_APPROVAL = 10;
    private static final int DEFAULT_MAX_FILES_RED_ZONE = 1;
    private static final int DEFAULT_MAX_FILES_YELLOW_ZONE = 3;
    private static final String DEFAULT_OVERRIDE_KEYWORD = "BIGBANG_APPROVED";
    
    private final int maxFilesWithoutApproval;
    private final int maxFilesRedZone;
    private final int maxFilesYellowZone;
    private final List<String> redZonePaths;
    private final List<String> yellowZonePaths;
    private final String overrideKeyword;
    
    /**
     * Constructor para deserialización JSON.
     */
    @JsonCreator
    public BlastRadiusConfig(
            @JsonProperty("max_files_without_approval") Integer maxFilesWithoutApproval,
            @JsonProperty("max_files_red_zone") Integer maxFilesRedZone,
            @JsonProperty("max_files_yellow_zone") Integer maxFilesYellowZone,
            @JsonProperty("red_zone_paths") List<String> redZonePaths,
            @JsonProperty("yellow_zone_paths") List<String> yellowZonePaths,
            @JsonProperty("override_keyword") String overrideKeyword) {
        this.maxFilesWithoutApproval = maxFilesWithoutApproval != null 
            ? maxFilesWithoutApproval 
            : DEFAULT_MAX_FILES_WITHOUT_APPROVAL;
        this.maxFilesRedZone = maxFilesRedZone != null 
            ? maxFilesRedZone 
            : DEFAULT_MAX_FILES_RED_ZONE;
        this.maxFilesYellowZone = maxFilesYellowZone != null 
            ? maxFilesYellowZone 
            : DEFAULT_MAX_FILES_YELLOW_ZONE;
        this.redZonePaths = redZonePaths != null 
            ? new ArrayList<>(redZonePaths) 
            : new ArrayList<>();
        this.yellowZonePaths = yellowZonePaths != null 
            ? new ArrayList<>(yellowZonePaths) 
            : new ArrayList<>();
        this.overrideKeyword = overrideKeyword != null 
            ? overrideKeyword 
            : DEFAULT_OVERRIDE_KEYWORD;
    }
    
    /**
     * Constructor para builder pattern (usado en tests).
     */
    private BlastRadiusConfig(Builder builder) {
        this.maxFilesWithoutApproval = builder.maxFilesWithoutApproval;
        this.maxFilesRedZone = builder.maxFilesRedZone;
        this.maxFilesYellowZone = builder.maxFilesYellowZone;
        this.redZonePaths = new ArrayList<>(builder.redZonePaths);
        this.yellowZonePaths = new ArrayList<>(builder.yellowZonePaths);
        this.overrideKeyword = builder.overrideKeyword;
    }
    
    public int getMaxFilesWithoutApproval() {
        return maxFilesWithoutApproval;
    }
    
    public int getMaxFilesRedZone() {
        return maxFilesRedZone;
    }
    
    public int getMaxFilesYellowZone() {
        return maxFilesYellowZone;
    }
    
    public List<String> getRedZonePaths() {
        return Collections.unmodifiableList(redZonePaths);
    }
    
    public List<String> getYellowZonePaths() {
        return Collections.unmodifiableList(yellowZonePaths);
    }
    
    public String getOverrideKeyword() {
        return overrideKeyword;
    }
    
    /**
     * Valida que la configuración sea consistente.
     * 
     * @return Lista de errores encontrados, o lista vacía si es válida.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (maxFilesWithoutApproval <= 0) {
            errors.add("max_files_without_approval must be positive, got: " + maxFilesWithoutApproval);
        }
        
        if (maxFilesRedZone <= 0) {
            errors.add("max_files_red_zone must be positive, got: " + maxFilesRedZone);
        }
        
        if (maxFilesYellowZone <= 0) {
            errors.add("max_files_yellow_zone must be positive, got: " + maxFilesYellowZone);
        }
        
        if (redZonePaths == null || redZonePaths.isEmpty()) {
            errors.add("red_zone_paths must be non-empty");
        } else {
            for (String path : redZonePaths) {
                if (path == null || path.trim().isEmpty()) {
                    errors.add("red_zone_paths cannot contain null or empty paths");
                    break;
                }
            }
        }
        
        if (yellowZonePaths == null || yellowZonePaths.isEmpty()) {
            errors.add("yellow_zone_paths must be non-empty");
        } else {
            for (String path : yellowZonePaths) {
                if (path == null || path.trim().isEmpty()) {
                    errors.add("yellow_zone_paths cannot contain null or empty paths");
                    break;
                }
            }
        }
        
        if (overrideKeyword == null || overrideKeyword.trim().isEmpty()) {
            errors.add("override_keyword must be non-empty");
        }
        
        return errors;
    }
    
    /**
     * Crea un builder para construir configuraciones en tests.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder para crear configuraciones en tests.
     */
    public static class Builder {
        private int maxFilesWithoutApproval = DEFAULT_MAX_FILES_WITHOUT_APPROVAL;
        private int maxFilesRedZone = DEFAULT_MAX_FILES_RED_ZONE;
        private int maxFilesYellowZone = DEFAULT_MAX_FILES_YELLOW_ZONE;
        private List<String> redZonePaths = new ArrayList<>();
        private List<String> yellowZonePaths = new ArrayList<>();
        private String overrideKeyword = DEFAULT_OVERRIDE_KEYWORD;
        
        public Builder maxFilesWithoutApproval(int value) {
            this.maxFilesWithoutApproval = value;
            return this;
        }
        
        public Builder maxFilesRedZone(int value) {
            this.maxFilesRedZone = value;
            return this;
        }
        
        public Builder maxFilesYellowZone(int value) {
            this.maxFilesYellowZone = value;
            return this;
        }
        
        public Builder redZonePaths(List<String> paths) {
            this.redZonePaths = new ArrayList<>(paths);
            return this;
        }
        
        public Builder yellowZonePaths(List<String> paths) {
            this.yellowZonePaths = new ArrayList<>(paths);
            return this;
        }
        
        public Builder overrideKeyword(String keyword) {
            this.overrideKeyword = keyword;
            return this;
        }
        
        public BlastRadiusConfig build() {
            return new BlastRadiusConfig(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlastRadiusConfig that = (BlastRadiusConfig) o;
        return maxFilesWithoutApproval == that.maxFilesWithoutApproval
                && maxFilesRedZone == that.maxFilesRedZone
                && maxFilesYellowZone == that.maxFilesYellowZone
                && Objects.equals(redZonePaths, that.redZonePaths)
                && Objects.equals(yellowZonePaths, that.yellowZonePaths)
                && Objects.equals(overrideKeyword, that.overrideKeyword);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(maxFilesWithoutApproval, maxFilesRedZone, maxFilesYellowZone,
                redZonePaths, yellowZonePaths, overrideKeyword);
    }
    
    @Override
    public String toString() {
        return "BlastRadiusConfig{" +
                "maxFilesWithoutApproval=" + maxFilesWithoutApproval +
                ", maxFilesRedZone=" + maxFilesRedZone +
                ", maxFilesYellowZone=" + maxFilesYellowZone +
                ", redZonePaths=" + redZonePaths +
                ", yellowZonePaths=" + yellowZonePaths +
                ", overrideKeyword='" + overrideKeyword + '\'' +
                '}';
    }
}
