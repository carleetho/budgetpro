package com.budgetpro.domain.test.model;

// Test 1: Snapshot con setter - debe generar ERROR
public class TestSnapshot {
    private String campo;

    // ruleid: budgetpro.domain.immutability.snapshot-no-setters
    public void setCampo(String valor) {
        this.campo = valor;
    }

    // ruleid: budgetpro.domain.immutability.snapshot-no-setters
    public void setValor(Integer valor) {
        this.campo = valor.toString();
    }
}

// Test 2: Snapshot sin marcador de inmutabilidad - debe generar WARNING
// ok: budgetpro.domain.immutability.snapshot-no-setters (no tiene setters)
public class EstimacionSnapshot {
    private final String campo;

    public EstimacionSnapshot(String campo) {
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

// Test 3: Snapshot con @Immutable - no debe generar warning
// ok: budgetpro.domain.immutability.snapshot-markers
// ok: budgetpro.domain.immutability.snapshot-no-setters
@Immutable
public class APUSnapshot {
    private final String campo;

    public APUSnapshot(String campo) {
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}

// Test 4: Snapshot como record - no debe generar warning
// ok: budgetpro.domain.immutability.snapshot-markers
// ok: budgetpro.domain.immutability.snapshot-no-setters
public record CronogramaSnapshot(String campo) {
}

// Test 5: Método de negocio que NO es setter - no debe generar violación
// ok: budgetpro.domain.immutability.snapshot-no-setters
public class EVMSnapshot {
    private BigDecimal valor;

    public void actualizarRendimiento(BigDecimal nuevoRendimiento, UUID usuarioId) {
        // Método de negocio, no setter
        if (nuevoRendimiento.compareTo(BigDecimal.ZERO) > 0) {
            this.valor = nuevoRendimiento;
        }
    }
}

// Test 6: Snapshot con setter y sin marcador - debe generar ambas violaciones
public class ProblemaSnapshot {
    private String campo;

    // ruleid: budgetpro.domain.immutability.snapshot-no-setters
    public void setCampo(String valor) {
        this.campo = valor;
    }
}
