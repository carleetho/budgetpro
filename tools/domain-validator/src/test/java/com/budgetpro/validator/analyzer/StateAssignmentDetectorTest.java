package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.model.StateAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, java.util.List<String>> stateMachines = new HashMap<>();
        stateMachines.put("EstadoPresupuesto", 
                java.util.List.of("BORRADOR", "CONGELADO", "APROBADO", "RECHAZADO"));
        stateMachines.put("Estado", 
                java.util.List.of("ACTIVO", "INACTIVO"));
        
        config = new StateMachineConfig(stateMachines);
    }
    
    @Test
    void deberiaDetectarAsignacionSimpleDeEstado(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java de prueba
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
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados
        assertEquals(1, assignments.size(), "Debe detectar una asignación de estado");
        
        StateAssignment assignment = assignments.get(0);
        assertEquals("CONGELADO", assignment.getToState());
        assertEquals("aprobar", assignment.getMethodName());
        assertEquals("estado", assignment.getStateFieldName());
        assertTrue(assignment.isValid(), "El estado debe ser válido");
        assertNotNull(assignment.getFilePath());
        assertTrue(assignment.getLineNumber() > 0);
    }
    
    @Test
    void deberiaDetectarAsignacionConValidacion(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java con validación if
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
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados
        assertEquals(1, assignments.size(), "Debe detectar una asignación de estado");
        
        StateAssignment assignment = assignments.get(0);
        assertEquals("CONGELADO", assignment.getToState());
        assertEquals("BORRADOR", assignment.getFromState(), 
                "Debe detectar el estado origen desde la condición if");
        assertEquals("aprobar", assignment.getMethodName());
        assertTrue(assignment.isValid(), "El estado debe ser válido");
    }
    
    @Test
    void deberiaMarcarEstadoNoExistenteComoInvalido(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java con estado inválido
        String javaCode = """
            package com.budgetpro.domain.presupuesto.model;
            
            public class Presupuesto {
                private EstadoPresupuesto estado;
                
                public void aprobar() {
                    this.estado = EstadoPresupuesto.INVALID_STATE;
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados
        assertEquals(1, assignments.size(), "Debe detectar la asignación aunque sea inválida");
        
        StateAssignment assignment = assignments.get(0);
        assertEquals("INVALID_STATE", assignment.getToState());
        assertFalse(assignment.isValid(), 
                "El estado debe marcarse como inválido porque no existe en el enum");
    }
    
    @Test
    void deberiaDetectarMultiplesAsignacionesEnUnArchivo(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java con múltiples asignaciones
        String javaCode = """
            package com.budgetpro.domain.presupuesto.model;
            
            public class Presupuesto {
                private EstadoPresupuesto estado;
                
                public void aprobar() {
                    this.estado = EstadoPresupuesto.APROBADO;
                }
                
                public void rechazar() {
                    this.estado = EstadoPresupuesto.RECHAZADO;
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados
        assertEquals(2, assignments.size(), "Debe detectar ambas asignaciones");
        
        // Verificar que ambas asignaciones tienen métodos diferentes
        long aprobarCount = assignments.stream()
                .filter(a -> "aprobar".equals(a.getMethodName()))
                .count();
        long rechazarCount = assignments.stream()
                .filter(a -> "rechazar".equals(a.getMethodName()))
                .count();
        
        assertEquals(1, aprobarCount, "Debe detectar asignación en método aprobar");
        assertEquals(1, rechazarCount, "Debe detectar asignación en método rechazar");
    }
    
    @Test
    void deberiaIgnorarAsignacionesNoRelacionadasConEstado(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java con asignaciones que no son de estado
        String javaCode = """
            package com.budgetpro.domain.presupuesto.model;
            
            public class Presupuesto {
                private EstadoPresupuesto estado;
                private String nombre;
                
                public void setNombre(String nombre) {
                    this.nombre = nombre;
                }
                
                public void cambiarEstado() {
                    this.estado = EstadoPresupuesto.CONGELADO;
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("Presupuesto.java");
        Files.writeString(javaFile, javaCode);
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados - solo debe detectar la asignación de estado
        assertEquals(1, assignments.size(), 
                "Debe detectar solo la asignación de estado, no otras asignaciones");
        
        StateAssignment assignment = assignments.get(0);
        assertEquals("cambiarEstado", assignment.getMethodName());
        assertEquals("estado", assignment.getStateFieldName());
    }
    
    @Test
    void deberiaManejarArchivoInexistente() {
        Path nonExistentFile = Paths.get("/ruta/inexistente/Archivo.java");
        
        List<StateAssignment> assignments = detector.detectAssignments(nonExistentFile, config);
        
        assertTrue(assignments.isEmpty(), 
                "Debe retornar lista vacía para archivo inexistente");
    }
    
    @Test
    void deberiaDetectarAsignacionConEnumSimple(@TempDir Path tempDir) throws IOException {
        // Crear archivo Java usando enum simple "Estado"
        String javaCode = """
            package com.budgetpro.domain.model;
            
            public class Entidad {
                private Estado estado;
                
                public void activar() {
                    this.estado = Estado.ACTIVO;
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("Entidad.java");
        Files.writeString(javaFile, javaCode);
        
        // Detectar asignaciones
        List<StateAssignment> assignments = detector.detectAssignments(javaFile, config);
        
        // Verificar resultados
        assertEquals(1, assignments.size(), "Debe detectar asignación con enum simple");
        
        StateAssignment assignment = assignments.get(0);
        assertEquals("ACTIVO", assignment.getToState());
        assertEquals("activar", assignment.getMethodName());
        assertTrue(assignment.isValid(), "El estado debe ser válido");
    }
}
