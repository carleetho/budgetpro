package com.budgetpro.domain.finanzas.estimacion.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecisionTest {
    public void calculate() {
        BigDecimal val = new BigDecimal("100.12345");

        // ruleid: 03-bigdecimal-precision-standard
        val.setScale(2, RoundingMode.HALF_UP);

        // ok: 03-bigdecimal-precision-standard
        val.setScale(4, RoundingMode.HALF_UP);

        // ruleid: 03-bigdecimal-precision-standard
        val.setScale(6, RoundingMode.HALF_UP);
    }

    public void getValueForPersistence() {
        BigDecimal val = new BigDecimal("100.12345");
        // ok: 03-bigdecimal-precision-standard
        val.setScale(2, RoundingMode.HALF_UP);
    }

    public void saveForPersistence() {
        BigDecimal val = new BigDecimal("100.12345");
        // ok: 03-bigdecimal-precision-standard
        val.setScale(2, RoundingMode.HALF_UP);
    }
}
