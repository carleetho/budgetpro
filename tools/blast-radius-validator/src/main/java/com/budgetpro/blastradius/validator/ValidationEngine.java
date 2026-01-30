package com.budgetpro.blastradius.validator;

import com.budgetpro.blastradius.classifier.ClassifiedFiles;
import com.budgetpro.blastradius.classifier.Zone;
import com.budgetpro.blastradius.classifier.ZoneClassifier;
import com.budgetpro.blastradius.config.BlastRadiusConfig;
import com.budgetpro.blastradius.config.ConfigLoader;
import com.budgetpro.blastradius.git.CommitMessage;
import com.budgetpro.blastradius.git.GitRepository;
import com.budgetpro.blastradius.git.StagedFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de validación que orquesta Git → Classify → Validate.
 */
public class ValidationEngine {
    
    private final ZoneClassifier classifier;
    private final ConfigLoader configLoader;
    
    public ValidationEngine() {
        this.classifier = new ZoneClassifier();
        this.configLoader = new ConfigLoader();
    }
    
    /**
     * Ejecuta la validación completa de blast radius.
     * 
     * @param repositoryPath Ruta al repositorio Git
     * @param configPath Ruta opcional al archivo de configuración (null para usar defaults)
     * @return Resultado de la validación
     */
    public ValidationResult validate(Path repositoryPath, Path configPath) {
        try {
            // 1. Cargar configuración
            BlastRadiusConfig config;
            if (configPath != null) {
                config = configLoader.load(configPath);
            } else {
                config = configLoader.loadDefaults();
            }
            
            // 2. Obtener archivos staged desde Git
            GitRepository gitRepo = new GitRepository(repositoryPath);
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            
            // 3. Obtener mensaje de commit
            CommitMessage commitMessage = gitRepo.getCommitMessage();
            
            // 4. Verificar override keyword
            boolean overrideDetected = commitMessage.hasOverrideKeyword(config.getOverrideKeyword());
            
            // 5. Si hay override, retornar éxito inmediatamente
            if (overrideDetected) {
                ClassifiedFiles classifiedFiles = classifier.classify(stagedFiles, config);
                return ValidationResult.success(classifiedFiles, true);
            }
            
            // 6. Clasificar archivos en zonas
            ClassifiedFiles classifiedFiles = classifier.classify(stagedFiles, config);
            
            // 7. Validar límites
            List<Violation> violations = validateLimits(classifiedFiles, config);
            
            // 8. Retornar resultado
            if (violations.isEmpty()) {
                return ValidationResult.success(classifiedFiles, false);
            } else {
                return ValidationResult.failure(classifiedFiles, violations, false);
            }
            
        } catch (GitRepository.RepositoryNotFoundException e) {
            return ValidationResult.error("Git repository not found: " + e.getMessage());
        } catch (GitRepository.GitOperationException e) {
            return ValidationResult.error("Git operation failed: " + e.getMessage());
        } catch (ConfigLoader.ConfigLoadException e) {
            return ValidationResult.error("Configuration error: " + e.getMessage());
        } catch (Exception e) {
            return ValidationResult.error("Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Valida los límites configurados contra los archivos clasificados.
     * 
     * @param classifiedFiles Archivos clasificados por zona
     * @param config Configuración con límites
     * @return Lista de violaciones encontradas
     */
    private List<Violation> validateLimits(ClassifiedFiles classifiedFiles, BlastRadiusConfig config) {
        List<Violation> violations = new ArrayList<>();
        
        int totalFiles = classifiedFiles.getTotalCount();
        int maxWithoutApproval = config.getMaxFilesWithoutApproval();
        
        // Validar límite total de archivos
        if (totalFiles > maxWithoutApproval) {
            violations.add(new Violation(
                Violation.ViolationType.TOTAL_FILES_EXCEEDED,
                null,
                maxWithoutApproval,
                totalFiles,
                new ArrayList<>(classifiedFiles.getAllFiles().values().stream()
                    .flatMap(List::stream)
                    .toList())
            ));
        }
        
        // Validar límite de red zone
        int redZoneCount = classifiedFiles.getCount(Zone.RED);
        if (redZoneCount > config.getMaxFilesRedZone()) {
            violations.add(new Violation(
                Violation.ViolationType.RED_ZONE_EXCEEDED,
                Zone.RED,
                config.getMaxFilesRedZone(),
                redZoneCount,
                classifiedFiles.getFiles(Zone.RED)
            ));
        }
        
        // Validar límite de yellow zone
        int yellowZoneCount = classifiedFiles.getCount(Zone.YELLOW);
        if (yellowZoneCount > config.getMaxFilesYellowZone()) {
            violations.add(new Violation(
                Violation.ViolationType.YELLOW_ZONE_EXCEEDED,
                Zone.YELLOW,
                config.getMaxFilesYellowZone(),
                yellowZoneCount,
                classifiedFiles.getFiles(Zone.YELLOW)
            ));
        }
        
        return violations;
    }
}
