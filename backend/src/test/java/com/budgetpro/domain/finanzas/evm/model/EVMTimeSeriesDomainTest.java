package com.budgetpro.domain.finanzas.evm.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EVMTimeSeriesDomainTest {

    @Test
    void testCrear_firstPeriod_previousValuesZero() {
        EVMTimeSeries ts = EVMTimeSeries.crear(
                EVMTimeSeriesId.nuevo(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 1),
                1,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "USD");

        assertEquals(0, ts.getCpiPeriodo().compareTo(BigDecimal.ZERO));
        assertEquals(0, ts.getSpiPeriodo().compareTo(BigDecimal.ZERO));
    }

    @Test
    void testCrear_secondPeriod_deltaComputedCorrectly() {
        EVMTimeSeries ts = EVMTimeSeries.crear(
                EVMTimeSeriesId.nuevo(),
                UUID.randomUUID(),
                LocalDate.of(2026, 3, 2),
                2,
                new BigDecimal("100.0000"),
                new BigDecimal("80.0000"),
                new BigDecimal("70.0000"),
                new BigDecimal("150.0000"),
                new BigDecimal("150.0000"),
                new BigDecimal("20.0000"),
                new BigDecimal("20.0000"),
                new BigDecimal("10.0000"),
                "USD");

        assertEquals(new BigDecimal("1.0000"), ts.getCpiPeriodo());
        assertEquals(new BigDecimal("0.7500"), ts.getSpiPeriodo());
    }

    @Test
    void testDividirSeguro_zeroDenominator_returnsZero() throws Exception {
        Method method = EVMTimeSeries.class.getDeclaredMethod(
                "dividirSeguro",
                BigDecimal.class,
                BigDecimal.class);
        method.setAccessible(true);

        BigDecimal result = (BigDecimal) method.invoke(null, BigDecimal.TEN, BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, result);
    }
}
