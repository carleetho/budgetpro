package com.budgetpro.tools.naming.layer;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class LayerDetectorTest {

    private final LayerDetector detector = new LayerDetector();

    @Test
    void testDetectJpaEntity() {
        Path path = Paths
                .get("backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/PresupuestoEntity.java");
        assertEquals(ArchitecturalLayer.JPA_ENTITY, detector.detectLayer(path, "PresupuestoEntity"));
    }

    @Test
    void testDetectDomainEntityFromModel() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/model/Presupuesto.java");
        assertEquals(ArchitecturalLayer.DOMAIN_ENTITY, detector.detectLayer(path, "Presupuesto"));
    }

    @Test
    void testDetectDomainEntityFromEntities() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/domain/finanzas/entities/Budget.java");
        assertEquals(ArchitecturalLayer.DOMAIN_ENTITY, detector.detectLayer(path, "Budget"));
    }

    @Test
    void testDetectMapperFromPath() {
        Path path = Paths
                .get("backend/src/main/java/com/budgetpro/infrastructure/persistence/mapper/PresupuestoMapper.java");
        assertEquals(ArchitecturalLayer.MAPPER, detector.detectLayer(path, "PresupuestoMapper"));
    }

    @Test
    void testDetectMapperFromClassName() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/util/DataMapper.java");
        assertEquals(ArchitecturalLayer.MAPPER, detector.detectLayer(path, "DataMapper"));
    }

    @Test
    void testDetectValueObject() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/domain/shared/valueobjects/Money.java");
        assertEquals(ArchitecturalLayer.VALUE_OBJECT, detector.detectLayer(path, "Money"));
    }

    @Test
    void testDetectDomainService() {
        Path path = Paths
                .get("backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/service/PresupuestoService.java");
        assertEquals(ArchitecturalLayer.DOMAIN_SERVICE, detector.detectLayer(path, "PresupuestoService"));
    }

    @Test
    void testHandleWindowsPathSeparators() {
        Path path = Paths.get("backend\\src\\main\\java\\com\\budgetpro\\domain\\model\\Presupuesto.java");
        assertEquals(ArchitecturalLayer.DOMAIN_ENTITY, detector.detectLayer(path, "Presupuesto"));
    }

    @Test
    void testHandleUnknownLayer() {
        Path path = Paths.get("backend/src/main/java/com/budgetpro/util/StringUtils.java");
        assertEquals(ArchitecturalLayer.UNKNOWN, detector.detectLayer(path, "StringUtils"));
    }

    @Test
    void testCaseInsensitivity() {
        Path path = Paths.get("Backend/Src/Main/Java/Com/Budgetpro/Domain/Model/Presupuesto.java");
        assertEquals(ArchitecturalLayer.DOMAIN_ENTITY, detector.detectLayer(path, "Presupuesto"));
    }
}
