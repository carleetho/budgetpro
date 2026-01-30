package com.budgetpro.performance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalTest {

    public void testViolation(BigDecimal amount) {
        // ruleid: budgetpro.performance.inefficient-bigdecimal-operations
        BigDecimal result = amount.setScale(2, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
    }

    public void testOk(BigDecimal amount) {
        // ok: budgetpro.performance.inefficient-bigdecimal-operations
        BigDecimal result = amount.add(BigDecimal.TEN).setScale(2, RoundingMode.HALF_UP);

        // ok: budgetpro.performance.inefficient-bigdecimal-operations
        BigDecimal step1 = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal step2 = step1.setScale(4, RoundingMode.HALF_UP);
    }
}
