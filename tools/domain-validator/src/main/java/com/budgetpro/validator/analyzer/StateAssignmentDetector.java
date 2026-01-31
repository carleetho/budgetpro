package com.budgetpro.validator.analyzer;

import com.budgetpro.validator.config.StateMachineConfig;
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
 * Analiza código fuente para encontrar patrones como: - this.estado =
 * EstadoPresupuesto.CONGELADO; - this.estado = Estado.{STATE_NAME};
 */
public class StateAssignmentDetector {

    private final JavaParser javaParser;
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(StateAssignmentDetector.class.getName());

    public StateAssignmentDetector() {
        TypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        this.javaParser = new JavaParser();
        this.javaParser.getParserConfiguration().setSymbolResolver(new JavaSymbolSolver(typeSolver));
    }

    /**
     * Detecta todas las asignaciones de estado en un archivo Java.
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
            String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");

            // Obtener nombre de clase
            Optional<ClassOrInterfaceDeclaration> classDecl = cu.findFirst(ClassOrInterfaceDeclaration.class);
            String className = classDecl.map(ClassOrInterfaceDeclaration::getNameAsString).orElse("");

            // Visitar todas las asignaciones
            StateAssignmentVisitor visitor = new StateAssignmentVisitor(filePath, packageName, className, config);
            visitor.visit(cu, assignments);

        } catch (FileNotFoundException e) {
            LOGGER.severe("Error reading file: " + javaFile + " - " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error parsing file: " + javaFile + " - " + e.getMessage());
        }

        return assignments;
    }

    private static class StateAssignmentVisitor extends VoidVisitorAdapter<List<StateAssignment>> {

        private final String filePath;
        private final String className;
        private final StateMachineConfig config;

        public StateAssignmentVisitor(String filePath, String packageName, String className,
                StateMachineConfig config) {
            this.filePath = filePath;
            this.className = className;
            this.config = config;
        }

        @Override
        public void visit(AssignExpr n, List<StateAssignment> assignments) {
            super.visit(n, assignments);

            if (isStateAssignment(n)) {
                StateAssignment assignment = extractStateAssignment(n);
                if (assignment != null) {
                    // Por ahora marcamos como válido si la clase tiene configuración
                    StateMachineConfig.StateMachineDefinition def = config.findDefinitionForClass(className);
                    assignment.setValid(def != null);
                    assignments.add(assignment);
                }
            }
        }

        private boolean isStateAssignment(AssignExpr assignExpr) {
            if (!(assignExpr.getTarget() instanceof FieldAccessExpr)) {
                return false;
            }

            FieldAccessExpr target = (FieldAccessExpr) assignExpr.getTarget();
            if (!(target.getScope() instanceof ThisExpr)) {
                return false;
            }

            if (!(assignExpr.getValue() instanceof FieldAccessExpr)) {
                return false;
            }

            FieldAccessExpr value = (FieldAccessExpr) assignExpr.getValue();
            return value.getScope() instanceof NameExpr;
        }

        private StateAssignment extractStateAssignment(AssignExpr assignExpr) {
            FieldAccessExpr target = (FieldAccessExpr) assignExpr.getTarget();
            FieldAccessExpr value = (FieldAccessExpr) assignExpr.getValue();

            String stateFieldName = target.getNameAsString();
            String stateValue = value.getNameAsString();

            Optional<MethodDeclaration> methodOpt = assignExpr.findAncestor(MethodDeclaration.class);
            String methodName = methodOpt.map(MethodDeclaration::getNameAsString).orElse("<unknown>");

            String fromState = determineFromState(assignExpr);

            int lineNumber = assignExpr.getBegin().map(p -> p.line).orElse(-1);

            return new StateAssignment(filePath, lineNumber, methodName, fromState, stateValue, stateFieldName,
                    className);
        }

        private String determineFromState(AssignExpr assignExpr) {
            Optional<IfStmt> ifStmtOpt = assignExpr.findAncestor(IfStmt.class);
            if (ifStmtOpt.isPresent()) {
                IfStmt ifStmt = ifStmtOpt.get();
                String stateFromCondition = extractStateFromCondition(ifStmt.getCondition());
                if (stateFromCondition != null) {
                    return stateFromCondition;
                }
            }

            Optional<SwitchStmt> switchStmtOpt = assignExpr.findAncestor(SwitchStmt.class);
            if (switchStmtOpt.isPresent()) {
                // Implementación simplificada
            }

            return null;
        }

        private String extractStateFromCondition(Node condition) {
            List<FieldAccessExpr> fieldAccesses = condition.findAll(FieldAccessExpr.class);

            for (FieldAccessExpr fieldAccess : fieldAccesses) {
                if (fieldAccess.getScope() instanceof NameExpr) {
                    NameExpr scope = (NameExpr) fieldAccess.getScope();
                    String enumType = scope.getNameAsString();

                    if (enumType.toLowerCase().contains("estado") || enumType.toLowerCase().contains("state")) {
                        return fieldAccess.getNameAsString();
                    }
                }
            }

            return null;
        }
    }
}
