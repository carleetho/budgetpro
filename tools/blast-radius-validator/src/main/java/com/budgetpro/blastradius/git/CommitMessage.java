package com.budgetpro.blastradius.git;

import java.util.Objects;

/**
 * Representa el mensaje de commit.
 * Proporciona métodos para detectar palabras clave de override.
 */
public class CommitMessage {
    
    private final String message;
    
    public CommitMessage(String message) {
        this.message = Objects.requireNonNull(message, "message cannot be null");
    }
    
    /**
     * Obtiene el mensaje completo.
     * 
     * @return El mensaje de commit
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Verifica si el mensaje contiene la palabra clave de override.
     * La búsqueda es case-sensitive.
     * 
     * @param keyword Palabra clave a buscar (ej: "BIGBANG_APPROVED")
     * @return true si el mensaje contiene la palabra clave, false en caso contrario
     */
    public boolean hasOverrideKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        return message.contains(keyword);
    }
    
    /**
     * Verifica si el mensaje está vacío (solo espacios en blanco).
     * 
     * @return true si el mensaje está vacío
     */
    public boolean isEmpty() {
        return message.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitMessage that = (CommitMessage) o;
        return Objects.equals(message, that.message);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(message);
    }
    
    @Override
    public String toString() {
        return "CommitMessage{message='" + message + "'}";
    }
}
