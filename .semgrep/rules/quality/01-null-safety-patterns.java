package com.budgetpro.quality;

import java.util.Objects;

public class NullSafetyTest {

    // ruleid: budgetpro.quality.null-safety-patterns
    public void testViolation(String input) {
        System.out.println(input.length());
    }

    // ok: budgetpro.quality.null-safety-patterns
    public void testOk(String input) {
        Objects.requireNonNull(input, "input cannot be null");
        System.out.println(input.length());
    }

    // ok: budgetpro.quality.null-safety-patterns
    // Primitive types are fine
    public void testOkPrimitive(int value) {
        System.out.println(value);
    }

    // ok: budgetpro.quality.null-safety-patterns
    // Private methods are fine
    private void testPrivate(String input) {
        System.out.println(input);
    }
}
