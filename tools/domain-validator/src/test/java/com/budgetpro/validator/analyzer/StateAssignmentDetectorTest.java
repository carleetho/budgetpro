package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.config.StateMachineConfig;
import com.budgetpro.validator.model.StateAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para StateAssignmentDetector.
 */
class StateAssignmentDetectorTest {

    private StateAssignmentDetector detector;
    private StateMachineConfig config;

    @BeforeEach
    void setUp() {
        detector = new StateAssignmentDetector();

        // Configurar máquinas de estado de prueba
        StateMachineConfig.StateMachineDefinition def = new StateMachineConfig.StateMachineDefinition();
        def.setClassFqn("com.budgetpro.domain.presupuesto.model.Presupuesto");
        def.setStateField("estado");
        def.setStateEnum("EstadoPresupuesto");

        config = new StateMachineConfig(List.of(def));
    }

    @Test
    void deberiaDetectarAsignacionSimpleDeEstado(@TempDir Path tempDir) throws IOException {
        String javaCode = """
                package com.budgetpro.domain.presupuesto.model;

                public class Presupuesto {
                    private EstadoPresupuesto estado;

                    public void aprobar() {
                        this.estado = EstadoPresupuesto.CONGELADO;
                    }
                }
                """;

        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);

        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);

        assertEquals(1, assignments.size(), "Debe detectar una asignación de estado");
        StateAssignment assignment = assignments.get(0);
        assertEquals("CONGELADO", assignment.getToState());
        assertEquals("aprobar", assignment.getMethodName());
        assertTrue(assignment.isValid(), "El estado debe ser válido");
    }

    @Test
    void deberiaDetectarAsignacionConValidacion(@TempDir Path tempDir) throws IOException {
        String javaCode = """
                package com.budgetpro.domain.presupuesto.model;

                public class Presupuesto {
                    private EstadoPresupuesto estado;

                    public void aprobar() {
                        if (this.estado == EstadoPresupuesto.BORRADOR) {
                            this.estado = EstadoPresupuesto.CONGELADO;
                        }
                    }
                }
                """;

        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);

        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);

        assertEquals(1, assignments.size());
        StateAssignment assignment = assignments.get(0);
        assertEquals("CONGELADO", assignment.getToState());
        assertEquals("BORRADOR", assignment.getFromState());
    }

    @Test
    void deberiaDetectarAsignacionEnClaseValida(@TempDir Path tempDir) throws IOException {
        String javaCode = """
                package com.budgetpro.domain.presupuesto.model;

                public class Presupuesto {
                    private EstadoPresupuesto estado;

                    public void setEstado() {
                        this.estado = EstadoPresupuesto.INVALID_STATE;
                    }
                }
                """;

        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);

        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);

        assertEquals(1, assignments.size());
        assertTrue(assignments.get(0).isValid(), "Debe ser válido porque la clase está configurada");
    }

    @Test
    void deberiaManejarArchivoInexistente() {
        Path nonExistentFile = Paths.get("/ruta/inexistente/Archivo.java");
        List<StateAssignment> assignments = detector.detectAssignments(nonExistentFile, config);
        assertTrue(assignments.isEmpty());
    }
}
