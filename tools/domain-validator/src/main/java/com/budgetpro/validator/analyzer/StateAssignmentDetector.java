package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.model.StateAssignment;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Detecta asignaciones de estado en archivos Java.
 * 
 * Analiza código fuente para encontrar patrones como:
 * - this.estado = EstadoPresupuesto.CONGELADO;
 * - this.estado = Estado.{STATE_NAME};
 * 
 * Extrae información sobre transiciones de estado incluyendo:
 * - Método contenedor
 * - Estado origen (si se puede determinar desde validaciones if/switch)
 * - Estado destino
 * - Validación contra valores de enum
 */
public class StateAssignmentDetector {
    
    private final JavaParser javaParser;
    
    public StateAssignmentDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration()
                .setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }
    
    /**
     * Detecta todas las asignaciones de estado en un archivo Java.
     * 
     * @param javaFile Ruta al archivo Java a analizar
     * @param config Configuración de máquinas de estado para validación
     * @return Lista de asignaciones de estado detectadas
     */
    public List<StateAssignment> detectAssignments(Path javaFile, StateMachineConfig config) {
        List<StateAssignment> assignments = new ArrayList<>();
        
        if (javaFile == null || !javaFile.toFile().exists()) {
            return assignments;
        }
        
        try {
            File file = javaFile.toFile();
            CompilationUnit cu = javaParser.parse(file).getResult().orElse(null);
            
            if (cu == null) {
                return assignments;
            }
            
            String filePath = javaFile.toString();
            String packageName = cu.getPackageDeclaration()
                    .map(p -> p.getNameAsString())
                    .orElse("");
            
            // Obtener nombre de clase
            Optional<ClassOrInterfaceDeclaration> classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class);
            String className = classDecl.map(ClassOrInterfaceDeclaration::getNameAsString).orElse("");
            
            // Visitar todas las asignaciones
            StateAssignmentVisitor visitor = new StateAssignmentVisitor(
                    filePath, packageName, className, config);
            visitor.visit(cu, assignments);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error reading file: " + javaFile + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing file: " + javaFile + " - " + e.getMessage());
        }
        
        return assignments;
    }
    
    /**
     * Visitor interno que recorre el AST buscando asignaciones de estado.
     */
    @SuppressWarnings("unchecked")
    private static class StateAssignmentVisitor extends VoidVisitorAdapter<List<StateAssignment>> {
        
        private final String filePath;
        private final String className;
        private final StateMachineConfig config;
        
        public StateAssignmentVisitor(String filePath, String packageName, 
                                     String className, StateMachineConfig config) {
            this.filePath = filePath;
            this.className = className;
            this.config = config;
        }
        
        @Override
        public void visit(AssignExpr n, List<StateAssignment> assignments) {
            super.visit(n, assignments);
            
            // Verificar si es una asignación de estado
            if (isStateAssignment(n)) {
                StateAssignment assignment = extractStateAssignment(n);
                if (assignment != null) {
                    // Validar que el estado existe en el enum
                    validateStateAssignment(assignment);
                    assignments.add(assignment);
                }
            }
        }
        
        /**
         * Verifica si una expresión de asignación es una asignación de estado.
         */
        private boolean isStateAssignment(AssignExpr assignExpr) {
            // Debe ser: this.{field} = {EnumType}.{VALUE}
            if (!(assignExpr.getTarget() instanceof FieldAccessExpr)) {
                return false;
            }
            
            FieldAccessExpr target = (FieldAccessExpr) assignExpr.getTarget();
            
            // Verificar que es this.{field}
            if (!(target.getScope() instanceof ThisExpr)) {
                return false;
            }
            
            // Verificar que el valor asignado es un enum
            if (!(assignExpr.getValue() instanceof FieldAccessExpr)) {
                return false;
            }
            
            FieldAccessExpr value = (FieldAccessExpr) assignExpr.getValue();
            
            // Debe ser: EnumType.VALUE
            return value.getScope() instanceof NameExpr;
        }
        
        /**
         * Extrae información de una asignación de estado.
         */
        private StateAssignment extractStateAssignment(AssignExpr assignExpr) {
            FieldAccessExpr target = (FieldAccessExpr) assignExpr.getTarget();
            FieldAccessExpr value = (FieldAccessExpr) assignExpr.getValue();
            
            String stateFieldName = target.getNameAsString();
            String stateValue = value.getNameAsString();
            
            // Encontrar método contenedor
            Optional<MethodDeclaration> methodOpt = assignExpr.findAncestor(MethodDeclaration.class);
            String methodName = methodOpt.map(MethodDeclaration::getNameAsString).orElse("<unknown>");
            
            // Intentar determinar estado origen desde contexto
            String fromState = determineFromState(assignExpr);
            
            int lineNumber = assignExpr.getBegin()
                    .map(p -> p.line)
                    .orElse(-1);
            
            return new StateAssignment(
                    filePath,
                    lineNumber,
                    methodName,
                    fromState,
                    stateValue,
                    stateFieldName,
                    className
            );
        }
        
        /**
         * Intenta determinar el estado origen analizando validaciones if/switch.
         */
        private String determineFromState(AssignExpr assignExpr) {
            // Buscar if statements que validen el estado antes de la asignación
            Optional<IfStmt> ifStmtOpt = assignExpr.findAncestor(IfStmt.class);
            if (ifStmtOpt.isPresent()) {
                IfStmt ifStmt = ifStmtOpt.get();
                String stateFromCondition = extractStateFromCondition(ifStmt.getCondition());
                if (stateFromCondition != null) {
                    return stateFromCondition;
                }
            }
            
            // Buscar switch statements
            Optional<SwitchStmt> switchStmtOpt = assignExpr.findAncestor(SwitchStmt.class);
            if (switchStmtOpt.isPresent()) {
                // El estado origen sería el case label
                // Por simplicidad, retornamos null y dejamos que se determine en el futuro
                // con análisis más profundo
            }
            
            return null;
        }
        
        /**
         * Extrae el estado desde una condición if.
         * Busca patrones como: this.estado == EstadoPresupuesto.BORRADOR
         */
        private String extractStateFromCondition(Node condition) {
            // Buscar FieldAccessExpr que represente un estado enum
            List<FieldAccessExpr> fieldAccesses = condition.findAll(FieldAccessExpr.class);
            
            for (FieldAccessExpr fieldAccess : fieldAccesses) {
                // Verificar si es un acceso a enum (EnumType.VALUE)
                if (fieldAccess.getScope() instanceof NameExpr) {
                    NameExpr scope = (NameExpr) fieldAccess.getScope();
                    String enumType = scope.getNameAsString();
                    
                    // Verificar si el tipo del enum sugiere que es un estado
                    if (enumType.toLowerCase().contains("estado") || 
                        enumType.toLowerCase().contains("state")) {
                        return fieldAccess.getNameAsString();
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Valida que el estado asignado existe en el enum correspondiente.
         */
        private void validateStateAssignment(StateAssignment assignment) {
            // Determinar el tipo de enum desde el contexto
            // Por ahora, buscamos en todos los enums conocidos
            // En el futuro, podríamos usar resolución de tipos más precisa
            
            String toState = assignment.getToState();
            boolean isValid = false;
            
            // Buscar en todos los enums de estado
            for (String enumName : config.getStateMachines().keySet()) {
                if (config.isValidState(enumName, toState)) {
                    isValid = true;
                    break;
                }
            }
            
            assignment.setValid(isValid);
        }
    }
}
