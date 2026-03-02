package com.budgetpro.domain.finanzas.evm.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkingDayCalculatorTest {

    private final WorkingDayCalculator calculator = new WorkingDayCalculator();

    @Test
    void testAC_TD_04_workingDaysBetween_excludesWeekends() {
        int workingDays = calculator.workingDaysBetween(
                LocalDate.of(2025, 1, 31),
                LocalDate.of(2025, 2, 7));

        assertEquals(5, workingDays);
    }

    @Test
    void testPlusWorkingDays_skipsWeekend() {
        LocalDate result = calculator.plusWorkingDays(LocalDate.of(2025, 1, 31), 1);
        assertEquals(LocalDate.of(2025, 2, 3), result);
    }

    @Test
    void testEdgeCase_sameDay_returnsZero() {
        LocalDate date = LocalDate.of(2025, 2, 10);
        assertEquals(0, calculator.workingDaysBetween(date, date));
    }
}
