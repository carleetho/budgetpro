package com.budgetpro.validator.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa el roadmap canónico completo con todos los módulos.
 */
public class CanonicalRoadmap {
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("generated_at")
    private String generatedAt;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("modules")
    private List<ModuleDefinition> modules;

    // Cache para búsqueda rápida por ID
    private transient Map<String, ModuleDefinition> moduleCache;

    public CanonicalRoadmap() {
        this.modules = new ArrayList<>();
        this.moduleCache = new HashMap<>();
    }

    public CanonicalRoadmap(String version, List<ModuleDefinition> modules) {
        this();
        this.version = Objects.requireNonNull(version, "version cannot be null");
        this.modules = Objects.requireNonNull(modules, "modules cannot be null");
        rebuildCache();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ModuleDefinition> getModules() {
        return modules;
    }

    public void setModules(List<ModuleDefinition> modules) {
        this.modules = modules != null ? modules : new ArrayList<>();
        rebuildCache();
    }

    /**
     * Obtiene un módulo por su ID.
     */
    public Optional<ModuleDefinition> getModuleById(String moduleId) {
        if (moduleId == null || moduleCache == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(moduleCache.get(moduleId));
    }

    /**
     * Verifica si un módulo existe en el roadmap.
     */
    public boolean hasModule(String moduleId) {
        return moduleId != null && moduleCache != null && moduleCache.containsKey(moduleId);
    }

    /**
     * Obtiene todos los módulos de una fase específica.
     */
    public List<ModuleDefinition> getModulesByPhase(String phase) {
        if (phase == null || modules == null) {
            return new ArrayList<>();
        }
        return modules.stream()
                .filter(m -> phase.equalsIgnoreCase(m.getPhase()))
                .toList();
    }

    /**
     * Obtiene el módulo Presupuesto.
     */
    public Optional<ModuleDefinition> getPresupuestoModule() {
        return getModuleById("presupuesto");
    }

    /**
     * Obtiene el módulo Tiempo.
     */
    public Optional<ModuleDefinition> getTiempoModule() {
        return getModuleById("tiempo");
    }

    /**
     * Verifica si el principio de baseline (Presupuesto + Tiempo freeze together) está codificado.
     */
    public boolean hasBaselinePrincipleEncoded() {
        Optional<ModuleDefinition> presupuesto = getPresupuestoModule();
        Optional<ModuleDefinition> tiempo = getTiempoModule();
        
        if (presupuesto.isEmpty() || tiempo.isEmpty()) {
            return false;
        }
        
        // Verificar que Presupuesto tiene constraint temporal con Tiempo
        boolean presupuestoHasCoupling = presupuesto.get().hasTemporalCouplingWith("tiempo");
        
        // Verificar que Tiempo tiene constraint temporal con Presupuesto
        boolean tiempoHasCoupling = tiempo.get().hasTemporalCouplingWith("presupuesto");
        
        return presupuestoHasCoupling && tiempoHasCoupling;
    }

    /**
     * Reconstruye el cache de módulos por ID.
     */
    private void rebuildCache() {
        if (moduleCache == null) {
            moduleCache = new HashMap<>();
        } else {
            moduleCache.clear();
        }
        
        if (modules != null) {
            for (ModuleDefinition module : modules) {
                if (module.getId() != null) {
                    moduleCache.put(module.getId(), module);
                }
            }
        }
    }

    /**
     * Valida la estructura del roadmap.
     * 
     * @return Lista de errores encontrados (vacía si es válido)
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (version == null || version.isBlank()) {
            errors.add("Roadmap version is missing");
        }
        
        if (modules == null || modules.isEmpty()) {
            errors.add("Roadmap has no modules defined");
            return errors;
        }
        
        // Validar que todos los módulos tienen ID único
        Map<String, Integer> idCounts = new HashMap<>();
        for (ModuleDefinition module : modules) {
            if (module.getId() == null || module.getId().isBlank()) {
                errors.add("Module found without ID");
            } else {
                idCounts.put(module.getId(), idCounts.getOrDefault(module.getId(), 0) + 1);
            }
        }
        
        idCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .forEach(e -> errors.add("Duplicate module ID: " + e.getKey()));
        
        // Validar referencias de dependencias
        for (ModuleDefinition module : modules) {
            for (String depId : module.getDependencies()) {
                if (!hasModule(depId)) {
                    errors.add("Module '" + module.getId() + "' references unknown dependency: " + depId);
                }
            }
            for (String enableId : module.getEnables()) {
                if (!hasModule(enableId)) {
                    errors.add("Module '" + module.getId() + "' references unknown enabled module: " + enableId);
                }
            }
        }
        
        return errors;
    }

    @Override
    public String toString() {
        return "CanonicalRoadmap{" +
                "version='" + version + '\'' +
                ", modules=" + (modules != null ? modules.size() : 0) +
                '}';
    }
}
