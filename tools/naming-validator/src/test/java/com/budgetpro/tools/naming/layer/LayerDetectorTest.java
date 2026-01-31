package com.budgetpro.tools.naming.layer;

import com.budgetpro.tools.naming.config.ValidationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LayerDetectorTest {

    private LayerDetector detector;

    @BeforeEach
    void setUp() {
        Map<String, ValidationConfig.LayerPatterns> layers = new HashMap<>();
        layers.put("JPA_ENTITY",
                new ValidationConfig.LayerPatterns(List.of("/infrastructure/persistence/entity/"), null));
        layers.put("MAPPER", new ValidationConfig.LayerPatterns(List.of("/mapper/"), List.of("Mapper")));
        layers.put("VALUE_OBJECT", new ValidationConfig.LayerPatterns(List.of("/domain/", "/valueobjects/"), null));
        layers.put("DOMAIN_SERVICE", new ValidationConfig.LayerPatterns(List.of("/domain/"), List.of("Service")));
        layers.put("DOMAIN_ENTITY",
                new ValidationConfig.LayerPatterns(List.of("/domain/"), List.of("/entities/", "/model/")));

        ValidationConfig config = new ValidationConfig(layers, new HashMap<>(), List.of());
        detector = new LayerDetector(config);
    }

    @Test
    void detectLayer_JpaEntityPath_ReturnsJpaEntity() {
        Path path = Paths
                .get("backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/UserJpaEntity.java");
        assertEquals(ArchitecturalLayer.JPA_ENTITY, detector.detectLayer(path, "UserJpaEntity"));
    }

    @Test
    void detectLayer_MapperPath_ReturnsMapper() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/application/mapper/UserMapper.java");
        assertEquals(ArchitecturalLayer.MAPPER, detector.detectLayer(path, "UserMapper"));
    }

    @Test
    void detectLayer_DomainEntityPath_ReturnsDomainEntity() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/domain/model/User.java");
        assertEquals(ArchitecturalLayer.DOMAIN_ENTITY, detector.detectLayer(path, "User"));
    }

    @Test
    void detectLayer_UnknownPath_ReturnsUnknown() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/util/FileUtils.java");
        assertEquals(ArchitecturalLayer.UNKNOWN, detector.detectLayer(path, "FileUtils"));
    }
}
