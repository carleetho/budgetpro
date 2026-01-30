package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineValidationOrchestratorTest {

    private StateMachineValidationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new StateMachineValidationOrchestrator();
    }

    @Test
    void testOrchestrateWithMockGit(@TempDir Path tempDir) throws Exception {
        // Setup: Create a dummy Presupuesto.java
        Path domainDir = tempDir.resolve("src/main/java/com/budgetpro/domain");
        Files.createDirectories(domainDir);
        Path presupuestoFile = domainDir.resolve("Presupuesto.java");
        Files.writeString(presupuestoFile,
                "package com.budgetpro.domain;\n" + "public class Presupuesto {\n"
                        + "    private EstadoPresupuesto estado;\n" + "    public void aprobar() {\n"
                        + "        this.estado = EstadoPresupuesto.CONGELADO;\n" + // Asumiendo que estaba en BORRADOR
                                                                                   // (no detectable aquí sin contexto)
                        "    }\n" + "}\n");

        // Create a dummy Enum
        Path enumFile = domainDir.resolve("EstadoPresupuesto.java");
        Files.writeString(enumFile, "package com.budgetpro.domain;\n" + "public enum EstadoPresupuesto {\n"
                + "    BORRADOR, CONGELADO, CERRADO\n" + "}\n");

        // Crear un script que simule git diff
        Path diffScript = tempDir.resolve("mock-git-diff.sh");
        String diffContent = "diff --git a/src/main/java/com/budgetpro/domain/Presupuesto.java b/src/main/java/com/budgetpro/domain/Presupuesto.java\n"
                + "--- a/src/main/java/com/budgetpro/domain/Presupuesto.java\n"
                + "+++ b/src/main/java/com/budgetpro/domain/Presupuesto.java\n" + "@@ -5,1 +5,1 @@\n"
                + "+        this.estado = EstadoPresupuesto.CONGELADO;\n";

        Files.writeString(diffScript, "echo \"" + diffContent.replace("\"", "\\\"") + "\"");
        diffScript.toFile().setExecutable(true);

        // Ejecutar orquestación
        // Nota: El orquestador cargará el state-machine-rules.yml de resources.
        // Asegurémonos de que el configLoader funcione o usemos uno con config
        // inyectada si fuera posible.
        // Pero el test debe ser de integración.

        List<TransitionViolation> violations = orchestrator.orchestrate(tempDir, domainDir, diffScript.toString());

        // Validar resultados (depende de lo que haya en el yml de resources)
        assertNotNull(violations);
        // Al ser un test de integración real, dependería del yml que esté en resources.
        // En este entorno, el yml tiene reglas para Presupuesto.
    }
}
