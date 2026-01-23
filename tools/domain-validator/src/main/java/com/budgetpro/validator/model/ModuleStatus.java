package com.budgetpro.validator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Estado de implementación de un módulo del dominio.
 */
public class ModuleStatus {
    
    @JsonProperty("module_id")
    private String moduleId;
    
    @JsonProperty("implementation_status")
    private ImplementationStatus implementationStatus;
    
    @JsonProperty("detected_entities")
    private List<String> detectedEntities;
    
    @JsonProperty("detected_services")
    private List<String> detectedServices;
    
    @JsonProperty("detected_endpoints")
    private List<String> detectedEndpoints;
    
    @JsonProperty("missing_dependencies")
    private List<String> missingDependencies;

    public ModuleStatus() {
        this.detectedEntities = new ArrayList<>();
        this.detectedServices = new ArrayList<>();
        this.detectedEndpoints = new ArrayList<>();
        this.missingDependencies = new ArrayList<>();
    }

    public ModuleStatus(String moduleId, ImplementationStatus implementationStatus) {
        this();
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId cannot be null");
        this.implementationStatus = Objects.requireNonNull(implementationStatus, "implementationStatus cannot be null");
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public ImplementationStatus getImplementationStatus() {
        return implementationStatus;
    }

    public void setImplementationStatus(ImplementationStatus implementationStatus) {
        this.implementationStatus = implementationStatus;
    }

    public List<String> getDetectedEntities() {
        return detectedEntities;
    }

    public void setDetectedEntities(List<String> detectedEntities) {
        this.detectedEntities = detectedEntities != null ? detectedEntities : new ArrayList<>();
    }

    public List<String> getDetectedServices() {
        return detectedServices;
    }

    public void setDetectedServices(List<String> detectedServices) {
        this.detectedServices = detectedServices != null ? detectedServices : new ArrayList<>();
    }

    public List<String> getDetectedEndpoints() {
        return detectedEndpoints;
    }

    public void setDetectedEndpoints(List<String> detectedEndpoints) {
        this.detectedEndpoints = detectedEndpoints != null ? detectedEndpoints : new ArrayList<>();
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setMissingDependencies(List<String> missingDependencies) {
        this.missingDependencies = missingDependencies != null ? missingDependencies : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleStatus that = (ModuleStatus) o;
        return Objects.equals(moduleId, that.moduleId) &&
                implementationStatus == that.implementationStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, implementationStatus);
    }

    @Override
    public String toString() {
        return "ModuleStatus{" +
                "moduleId='" + moduleId + '\'' +
                ", implementationStatus=" + implementationStatus +
                ", detectedEntities=" + detectedEntities.size() +
                ", detectedServices=" + detectedServices.size() +
                ", detectedEndpoints=" + detectedEndpoints.size() +
                ", missingDependencies=" + missingDependencies.size() +
                '}';
    }
}
