package com.budgetpro.quality;

public class ExceptionHandlingTest {

    public void testViolation() {
        try {
            doSomething();
            // ruleid: budgetpro.quality.exception-handling-standards
        } catch (Exception e) {
            // generic catch without rethrow
        }

        try {
            doSomething();
            // ruleid: budgetpro.quality.exception-handling-standards
        } catch (Throwable t) {
            // generic catch without rethrow
        }

        try {
            doSomething();
            // ruleid: budgetpro.quality.exception-handling-standards
        } catch (NullPointerException e) {
        }
    }

    public void testOk() {
        try {
            doSomething();
        } catch (IllegalArgumentException e) {
            // ok: budgetpro.quality.exception-handling-standards
            System.out.println("Invalid argument");
        }

        try {
            doSomething();
        } catch (Exception e) {
            // ok: budgetpro.quality.exception-handling-standards
            System.out.println("Fatal error");
            throw e;
        }
    }

    private void doSomething() {
    }
}
