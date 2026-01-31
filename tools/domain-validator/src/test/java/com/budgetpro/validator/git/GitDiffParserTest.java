package com.budgetpro.validator.git;

import com.budgetpro.validator.config.StateMachineConfig;
import com.budgetpro.validator.model.ChangedFile;
import com.budgetpro.validator.model.ChangedFile.ChangeType;
import com.budgetpro.validator.model.LineRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitDiffParserTest {

    private GitDiffParser parser;
    private StateMachineConfig config;

    @BeforeEach
    void setUp() {
        parser = new GitDiffParser();

        // Configuración simple que incluye "Presupuesto"
        StateMachineConfig.StateMachineDefinition def1 = new StateMachineConfig.StateMachineDefinition();
        def1.setClassFqn("com.budgetpro.domain.Presupuesto");
        def1.setStateEnum("EstadoPresupuesto");

        StateMachineConfig.StateMachineDefinition def2 = new StateMachineConfig.StateMachineDefinition();
        def2.setClassFqn("Proyecto"); // Caso de FQN parcial
        def2.setStateEnum("EstadoProyecto");

        config = new StateMachineConfig(List.of(def1, def2));
    }

    @Test
    void testParseSimpleDiff() {
        List<String> diffLines = List.of(
                "diff --git a/src/main/java/com/budgetpro/domain/Presupuesto.java b/src/main/java/com/budgetpro/domain/Presupuesto.java",
                "index 1234567..89abcdef 100644", "--- a/src/main/java/com/budgetpro/domain/Presupuesto.java",
                "+++ b/src/main/java/com/budgetpro/domain/Presupuesto.java",
                "@@ -42,5 +42,7 @@ public class Presupuesto {", "     private String id;",
                "+    private String nuevoCampo;", "+    private int otraCosa;",
                "     private EstadoPresupuesto estado;");

        List<ChangedFile> result = parser.parseDiffOutput(diffLines, config);

        assertEquals(1, result.size());
        ChangedFile file = result.get(0);
        assertTrue(file.getFilePath().endsWith("Presupuesto.java"));
        assertEquals(ChangeType.MODIFIED, file.getChangeType());
        assertEquals(1, file.getChangedLineRanges().size());

        // Rango esperado: start=42, count=7 -> 42 a 48
        LineRange range = file.getChangedLineRanges().get(0);
        assertEquals(42, range.startLine());
        assertEquals(48, range.endLine());
    }

    @Test
    void testFilterNonStateMachineFiles() {
        List<String> diffLines = List.of(
                "diff --git a/src/main/java/com/budgetpro/domain/Presupuesto.java b/src/main/java/com/budgetpro/domain/Presupuesto.java",
                "index 1234567..89abcdef 100644", "--- a/src/main/java/com/budgetpro/domain/Presupuesto.java",
                "+++ b/src/main/java/com/budgetpro/domain/Presupuesto.java", "@@ -1,1 +1,1 @@", "-original", "+cambio",
                "diff --git a/src/main/java/com/budgetpro/service/UsuarioService.java b/src/main/java/com/budgetpro/service/UsuarioService.java",
                "index 1234567..89abcdef 100644", "--- a/src/main/java/com/budgetpro/service/UsuarioService.java",
                "+++ b/src/main/java/com/budgetpro/service/UsuarioService.java", "@@ -1,1 +1,1 @@", "-original",
                "+cambio");

        List<ChangedFile> result = parser.parseDiffOutput(diffLines, config);

        // Solo debe detectar Presupuesto.java, UsuarioService.java no está en config
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFilePath().contains("Presupuesto.java"));
    }

    @Test
    void testExtractMultipleLineRanges() {
        List<String> diffLines = List.of("diff --git a/Presupuesto.java b/Presupuesto.java", "@@ -10,2 +10,2 @@",
                "@@ -50,5 +50,10 @@");

        List<ChangedFile> result = parser.parseDiffOutput(diffLines, config);

        assertEquals(1, result.size());
        List<LineRange> ranges = result.get(0).getChangedLineRanges();
        assertEquals(2, ranges.size());
        assertEquals(new LineRange(10, 11), ranges.get(0));
        assertEquals(new LineRange(50, 59), ranges.get(1));
    }

    @Test
    void testHandleRenames() {
        List<String> diffLines = List.of("diff --git a/OldPresupuesto.java b/Presupuesto.java", "similarity index 100%",
                "rename from OldPresupuesto.java", "rename to Presupuesto.java");

        List<ChangedFile> result = parser.parseDiffOutput(diffLines, config);

        assertEquals(1, result.size());
        assertEquals("Presupuesto.java", result.get(0).getFilePath());
        assertEquals(ChangeType.MODIFIED, result.get(0).getChangeType());
    }
}
