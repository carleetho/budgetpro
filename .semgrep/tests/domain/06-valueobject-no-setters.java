package com.budgetpro.domain.finanzas.valueobjects;

import java.math.BigDecimal;
import java.util.UUID;

// Test 1: Value Object con setter - debe generar ERROR
public class MontoVO {
    private BigDecimal amount;

    // ruleid: budgetpro.domain.immutability.valueobject-no-setters
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}

// Test 2: Value Object con setter con múltiples parámetros - debe generar ERROR
public class CoordenadasVO {
    private double lat;
    private double lon;

    // ruleid: budgetpro.domain.immutability.valueobject-no-setters
    public void setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}

// Test 3: Value Object sin setters - no debe generar violación
// ok: budgetpro.domain.immutability.valueobject-no-setters
public class ProyectoIdVO {
    private final UUID value;

    private ProyectoIdVO(UUID value) {
        this.value = value;
    }

    public static ProyectoIdVO from(UUID uuid) {
        return new ProyectoIdVO(uuid);
    }

    public UUID getValue() {
        return value;
    }
}

// Test 4: Interface con declaración abstracta de setter - no debe generar violación
// ok: budgetpro.domain.immutability.valueobject-no-setters (no tiene implementación)
interface ValueObjectInterface {
    void setValue(String value); // Declaración abstracta, sin implementación
}

// Test 5: Método que no es setter - no debe generar violación
// ok: budgetpro.domain.immutability.valueobject-no-setters
public class ValidacionVO {
    private String campo;

    public void validate() {
        // Método de validación, no setter
        if (campo == null) {
            throw new IllegalArgumentException("Campo requerido");
        }
    }

    public void apply(String context) {
        // Método de aplicación, no setter
        this.campo = context.toUpperCase();
    }
}

// Test 6: Value Object con setter en línea - debe generar ERROR
public class SimpleVO {
    private String value;

    // ruleid: budgetpro.domain.immutability.valueobject-no-setters
    public void setValue(String value) { this.value = value; }
}
