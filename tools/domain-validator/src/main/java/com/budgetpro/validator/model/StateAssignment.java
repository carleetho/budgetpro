package com.budgetpro.validator.model;

import java.util.Objects;

/**
 * Representa una asignación de estado detectada en el código fuente.
 * 
 * Captura información sobre transiciones de estado en métodos de entidades del dominio.
 */
public class StateAssignment {
    
    private String filePath;
    private int lineNumber;
    private String methodName;
    private String fromState;  // nullable - puede ser null si no se puede determinar
    private String toState;
    private String stateFieldName;  // ej: "estado", "state"
    private String className;
    private boolean valid;  // true si el estado existe en el enum, false si no
    
    public StateAssignment() {
        this.valid = true;  // Por defecto asumimos válido hasta verificar
    }
    
    public StateAssignment(String filePath, int lineNumber, String methodName, 
                          String fromState, String toState, String stateFieldName, 
                          String className) {
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.methodName = methodName;
        this.fromState = fromState;
        this.toState = toState;
        this.stateFieldName = stateFieldName;
        this.className = className;
        this.valid = true;
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
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
    
    public String getStateFieldName() {
        return stateFieldName;
    }
    
    public void setStateFieldName(String stateFieldName) {
        this.stateFieldName = stateFieldName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateAssignment that = (StateAssignment) o;
        return lineNumber == that.lineNumber &&
                valid == that.valid &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(fromState, that.fromState) &&
                Objects.equals(toState, that.toState) &&
                Objects.equals(stateFieldName, that.stateFieldName) &&
                Objects.equals(className, that.className);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(filePath, lineNumber, methodName, fromState, 
                           toState, stateFieldName, className, valid);
    }
    
    @Override
    public String toString() {
        return "StateAssignment{" +
                "filePath='" + filePath + '\'' +
                ", lineNumber=" + lineNumber +
                ", methodName='" + methodName + '\'' +
                ", fromState='" + fromState + '\'' +
                ", toState='" + toState + '\'' +
                ", stateFieldName='" + stateFieldName + '\'' +
                ", className='" + className + '\'' +
                ", valid=" + valid +
                '}';
    }
}
