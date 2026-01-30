package com.budgetpro.blastradius.classifier;

import com.budgetpro.blastradius.config.BlastRadiusConfig;
import com.budgetpro.blastradius.git.StagedFile;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ZoneClassifier.
 */
class ZoneClassifierTest {
    
    private final ZoneClassifier classifier = new ZoneClassifier();
    
    @Test
    void testClassifyFilesIntoCorrectZones() {
        // Setup: Archivos en diferentes zonas
        StagedFile redFile = new StagedFile("domain/presupuesto/Budget.java");
        StagedFile yellowFile = new StagedFile("infrastructure/persistence/Repo.java");
        StagedFile greenFile = new StagedFile("utils/Helper.java");
        
        List<StagedFile> files = Arrays.asList(redFile, yellowFile, greenFile);
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/", "domain/estimacion/"))
            .yellowZonePaths(Arrays.asList("infrastructure/persistence/"))
            .build();
        
        // Action: Clasificar archivos
        ClassifiedFiles result = classifier.classify(files, config);
        
        // Expect: Budget.java en RED, Repo.java en YELLOW, Helper.java en GREEN
        assertEquals(1, result.getCount(Zone.RED));
        assertEquals(1, result.getCount(Zone.YELLOW));
        assertEquals(1, result.getCount(Zone.GREEN));
        
        assertTrue(result.getFiles(Zone.RED).contains(redFile));
        assertTrue(result.getFiles(Zone.YELLOW).contains(yellowFile));
        assertTrue(result.getFiles(Zone.GREEN).contains(greenFile));
    }
    
    @Test
    void testFirstMatchWinsForOverlappingPaths() {
        // Setup: Red zone: ["domain/"], Yellow zone: ["domain/shared/"]
        StagedFile file = new StagedFile("domain/shared/Value.java");
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/"))
            .yellowZonePaths(Arrays.asList("domain/shared/"))
            .build();
        
        // Action: Clasificar archivo
        ClassifiedFiles result = classifier.classify(Arrays.asList(file), config);
        
        // Expect: Clasificado como RED (first match wins)
        assertEquals(1, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(0, result.getCount(Zone.GREEN));
        assertTrue(result.getFiles(Zone.RED).contains(file));
    }
    
    @Test
    void testCaseSensitivePathMatching() {
        // Setup: Red zone: ["Domain/"], file: "domain/Budget.java"
        StagedFile file = new StagedFile("domain/Budget.java");
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("Domain/"))
            .yellowZonePaths(Arrays.asList("Infrastructure/"))
            .build();
        
        // Action: Clasificar archivo
        ClassifiedFiles result = classifier.classify(Arrays.asList(file), config);
        
        // Expect: Clasificado como GREEN (no match debido a case)
        assertEquals(0, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(1, result.getCount(Zone.GREEN));
        assertTrue(result.getFiles(Zone.GREEN).contains(file));
    }
    
    @Test
    void testCountFilesPerZone() {
        // Setup: 3 red, 2 yellow, 5 green files
        List<StagedFile> redFiles = Arrays.asList(
            new StagedFile("domain/presupuesto/File1.java"),
            new StagedFile("domain/presupuesto/File2.java"),
            new StagedFile("domain/estimacion/File3.java")
        );
        
        List<StagedFile> yellowFiles = Arrays.asList(
            new StagedFile("infrastructure/persistence/File4.java"),
            new StagedFile("infrastructure/persistence/File5.java")
        );
        
        List<StagedFile> greenFiles = Arrays.asList(
            new StagedFile("utils/File6.java"),
            new StagedFile("utils/File7.java"),
            new StagedFile("utils/File8.java"),
            new StagedFile("utils/File9.java"),
            new StagedFile("utils/File10.java")
        );
        
        List<StagedFile> allFiles = new java.util.ArrayList<>();
        allFiles.addAll(redFiles);
        allFiles.addAll(yellowFiles);
        allFiles.addAll(greenFiles);
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/", "domain/estimacion/"))
            .yellowZonePaths(Arrays.asList("infrastructure/persistence/"))
            .build();
        
        // Action: Clasificar archivos
        ClassifiedFiles result = classifier.classify(allFiles, config);
        
        // Expect: Conteos correctos
        assertEquals(3, result.getCount(Zone.RED));
        assertEquals(2, result.getCount(Zone.YELLOW));
        assertEquals(5, result.getCount(Zone.GREEN));
        assertEquals(10, result.getTotalCount());
    }
    
    @Test
    void testEmptyFileList() {
        // Setup: Lista vacía de archivos
        List<StagedFile> emptyList = Arrays.asList();
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/"))
            .yellowZonePaths(Arrays.asList("infrastructure/"))
            .build();
        
        // Action: Clasificar lista vacía
        ClassifiedFiles result = classifier.classify(emptyList, config);
        
        // Expect: Todos los conteos son 0
        assertEquals(0, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(0, result.getCount(Zone.GREEN));
        assertEquals(0, result.getTotalCount());
        assertFalse(result.hasFiles(Zone.RED));
        assertFalse(result.hasFiles(Zone.YELLOW));
        assertFalse(result.hasFiles(Zone.GREEN));
    }
    
    @Test
    void testAllFilesInRedZone() {
        // Setup: Todos los archivos en red zone
        List<StagedFile> files = Arrays.asList(
            new StagedFile("domain/presupuesto/File1.java"),
            new StagedFile("domain/presupuesto/File2.java"),
            new StagedFile("domain/estimacion/File3.java")
        );
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/"))
            .yellowZonePaths(Arrays.asList("infrastructure/"))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(files, config);
        
        // Expect: Todos en RED
        assertEquals(3, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(0, result.getCount(Zone.GREEN));
    }
    
    @Test
    void testAllFilesInGreenZone() {
        // Setup: Todos los archivos fuera de zonas configuradas
        List<StagedFile> files = Arrays.asList(
            new StagedFile("utils/File1.java"),
            new StagedFile("scripts/File2.java"),
            new StagedFile("docs/File3.java")
        );
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/"))
            .yellowZonePaths(Arrays.asList("infrastructure/"))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(files, config);
        
        // Expect: Todos en GREEN
        assertEquals(0, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(3, result.getCount(Zone.GREEN));
    }
    
    @Test
    void testExactPathMatch() {
        // Setup: Archivo que coincide exactamente con un path de zona
        StagedFile file = new StagedFile("domain/presupuesto/");
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/"))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(Arrays.asList(file), config);
        
        // Expect: Clasificado como RED
        assertEquals(1, result.getCount(Zone.RED));
        assertTrue(result.getFiles(Zone.RED).contains(file));
    }
    
    @Test
    void testPathWithSubdirectories() {
        // Setup: Archivo en subdirectorio de zona
        StagedFile file = new StagedFile("domain/presupuesto/subdir/deep/File.java");
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/"))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(Arrays.asList(file), config);
        
        // Expect: Clasificado como RED (prefix match)
        assertEquals(1, result.getCount(Zone.RED));
        assertTrue(result.getFiles(Zone.RED).contains(file));
    }
    
    @Test
    void testMultipleRedZonePaths() {
        // Setup: Archivos que coinciden con diferentes paths de red zone
        List<StagedFile> files = Arrays.asList(
            new StagedFile("domain/presupuesto/File1.java"),
            new StagedFile("domain/estimacion/File2.java"),
            new StagedFile("domain/valueobjects/File3.java")
        );
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList(
                "domain/presupuesto/",
                "domain/estimacion/",
                "domain/valueobjects/"
            ))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(files, config);
        
        // Expect: Todos en RED
        assertEquals(3, result.getCount(Zone.RED));
        assertEquals(0, result.getCount(Zone.YELLOW));
        assertEquals(0, result.getCount(Zone.GREEN));
    }
    
    @Test
    void testClassifiedFilesImmutableLists() {
        // Verificar que las listas retornadas son inmutables
        List<StagedFile> files = Arrays.asList(
            new StagedFile("domain/presupuesto/File.java")
        );
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/"))
            .build();
        
        ClassifiedFiles result = classifier.classify(files, config);
        
        List<StagedFile> redFiles = result.getFiles(Zone.RED);
        
        // Expect: Intentar modificar debería lanzar excepción
        assertThrows(UnsupportedOperationException.class, () -> 
            redFiles.add(new StagedFile("test.java"))
        );
    }
    
    @Test
    void testClassifiedFilesHasFiles() {
        // Verificar método hasFiles
        List<StagedFile> files = Arrays.asList(
            new StagedFile("domain/presupuesto/File.java")
        );
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/"))
            .build();
        
        ClassifiedFiles result = classifier.classify(files, config);
        
        assertTrue(result.hasFiles(Zone.RED));
        assertFalse(result.hasFiles(Zone.YELLOW));
        assertFalse(result.hasFiles(Zone.GREEN));
    }
    
    @Test
    void testNullStagedFilesThrowsException() {
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/"))
            .build();
        
        assertThrows(NullPointerException.class, () -> 
            classifier.classify(null, config)
        );
    }
    
    @Test
    void testNullConfigThrowsException() {
        List<StagedFile> files = Arrays.asList(
            new StagedFile("domain/presupuesto/File.java")
        );
        
        assertThrows(NullPointerException.class, () -> 
            classifier.classify(files, null)
        );
    }
    
    @Test
    void testPathNotStartingWithZonePath() {
        // Setup: Archivo que contiene el path pero no empieza con él
        StagedFile file = new StagedFile("src/domain/presupuesto/File.java");
        
        BlastRadiusConfig config = BlastRadiusConfig.builder()
            .redZonePaths(Arrays.asList("domain/presupuesto/"))
            .build();
        
        // Action: Clasificar
        ClassifiedFiles result = classifier.classify(Arrays.asList(file), config);
        
        // Expect: Clasificado como GREEN (no empieza con el path)
        assertEquals(0, result.getCount(Zone.RED));
        assertEquals(1, result.getCount(Zone.GREEN));
    }
}
