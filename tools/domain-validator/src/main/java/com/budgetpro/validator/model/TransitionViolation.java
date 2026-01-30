package com.budgetpro.validator.model;

import java.util.List;
import java.util.Objects;

/**
 * Representa una violación de transición de estado detectada.
 * 
 * Captura información sobre transiciones inválidas o falta de validación
 * en cambios de estado de entidades del dominio.
 */
public class TransitionViolation {
    
    public enum ViolationType {
        /**
         * Transición inválida: el estado destino no está permitido desde el estado origen.
         */
        INVALID_TRANSITION,
        
        /**
         * Falta de validación: no se pudo determinar el estado origen,
         * sugiriendo que falta lógica de validación.
         */
        MISSING_VALIDATION,
        
        /**
         * Estado no definido: el estado destino no existe en el enum.
         */
        NON_EXISTENT_STATE
    }
    
    private ViolationSeverity severity;
    private String filePath;
    private int lineNumber;
    private String fromState;  // nullable
    private String toState;
    private List<String> validTransitions;  // nullable - lista de transiciones válidas
    private ViolationType violationType;
    private String message;
    private String className;
    private String methodName;
    
    public TransitionViolation() {
    }
    
    public TransitionViolation(ViolationSeverity severity, String filePath, int lineNumber,
                               String fromState, String toState, List<String> validTransitions,
                               ViolationType violationType, String message, String className, String methodName) {
        this.severity = severity;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.fromState = fromState;
        this.toState = toState;
        this.validTransitions = validTransitions;
        this.violationType = violationType;
        this.message = message;
        this.className = className;
        this.methodName = methodName;
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public void setSeverity(ViolationSeverity severity) {
        this.severity = severity;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getFromState() {
        return fromState;
    }
    
    public void setFromState(String fromState) {
        this.fromState = fromState;
    }
    
    public String getToState() {
        return toState;
    }
    
    public void setToState(String toState) {
        this.toState = toState;
    }
    
    public List<String> getValidTransitions() {
        return validTransitions;
    }
    
    public void setValidTransitions(List<String> validTransitions) {
        this.validTransitions = validTransitions;
    }
    
    public ViolationType getViolationType() {
        return violationType;
    }
    
    public void setViolationType(ViolationType violationType) {
        this.violationType = violationType;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransitionViolation that = (TransitionViolation) o;
        return lineNumber == that.lineNumber &&
                severity == that.severity &&
                violationType == that.violationType &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(fromState, that.fromState) &&
                Objects.equals(toState, that.toState) &&
                Objects.equals(validTransitions, that.validTransitions) &&
                Objects.equals(message, that.message) &&
                Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(severity, filePath, lineNumber, fromState, toState, 
                           validTransitions, violationType, message, className, methodName);
    }
    
    @Override
    public String toString() {
        return "TransitionViolation{" +
                "severity=" + severity +
                ", filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                ", fromState='" + fromState + '\'' +
                ", toState='" + toState + '\'' +
                ", validTransitions=" + validTransitions +
                ", violationType=" + violationType +
                ", message='" + message + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
