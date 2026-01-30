package com.budgetpro.blastradius.validator;

import com.budgetpro.blastradius.classifier.Zone;
import com.budgetpro.blastradius.git.StagedFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa una violación de las reglas de blast radius.
 */
public class Violation {
    
    private final ViolationType type;
    private final Zone zone;
    private final int limit;
    private final int actual;
    private final List<StagedFile> violatingFiles;
    private final String message;
    
    public Violation(ViolationType type, Zone zone, int limit, int actual, 
                    List<StagedFile> violatingFiles) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.zone = zone;
        this.limit = limit;
        this.actual = actual;
        this.violatingFiles = violatingFiles != null
            ? Collections.unmodifiableList(new ArrayList<>(violatingFiles))
            : Collections.emptyList();
        this.message = generateMessage();
    }
    
    private String generateMessage() {
        switch (type) {
            case TOTAL_FILES_EXCEEDED:
                return String.format(
                    "Total staged files (%d) exceeds limit (%d) without approval",
                    actual, limit);
            case RED_ZONE_EXCEEDED:
                return String.format(
                    "Red zone files (%d) exceed limit (%d)",
                    actual, limit);
            case YELLOW_ZONE_EXCEEDED:
                return String.format(
                    "Yellow zone files (%d) exceed limit (%d)",
                    actual, limit);
            default:
                return "Unknown violation";
        }
    }
    
    public ViolationType getType() {
        return type;
    }
    
    public Zone getZone() {
        return zone;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public int getActual() {
        return actual;
    }
    
    public List<StagedFile> getViolatingFiles() {
        return violatingFiles;
    }
    
    public String getMessage() {
        return message;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return limit == violation.limit
                && actual == violation.actual
                && type == violation.type
                && zone == violation.zone
                && Objects.equals(violatingFiles, violation.violatingFiles);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, zone, limit, actual, violatingFiles);
    }
    
    @Override
    public String toString() {
        return "Violation{" +
                "type=" + type +
                ", zone=" + zone +
                ", limit=" + limit +
                ", actual=" + actual +
                ", message='" + message + '\'' +
                '}';
    }
    
    /**
     * Tipos de violaciones posibles.
     */
    public enum ViolationType {
        TOTAL_FILES_EXCEEDED,
        RED_ZONE_EXCEEDED,
        YELLOW_ZONE_EXCEEDED
    }
}
