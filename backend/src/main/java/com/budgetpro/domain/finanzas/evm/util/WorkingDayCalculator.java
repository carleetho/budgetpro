package com.budgetpro.domain.finanzas.evm.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Utilidad de dominio pura para calcular días laborables (Lunes a Viernes).
 * 
 * Esta clase es independiente de frameworks y solo utiliza java.time.*.
 * Se instancia directamente: new WorkingDayCalculator()
 * 
 * Usado por el endpoint Forecast (REQ-63) para proyectar fechas de finalización.
 */
public final class WorkingDayCalculator {

    /**
     * Cuenta los días laborables (Lun-Vie) entre dos fechas.
     * 
     * El rango es [start, end), es decir:
     * - start es inclusivo
     * - end es exclusivo
     * 
     * @param start Fecha de inicio (inclusiva)
     * @param end Fecha de fin (exclusiva)
     * @return Número de días laborables entre start y end
     */
    public int workingDaysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        
        // Edge case: start == end retorna 0
        if (start.equals(end)) {
            return 0;
        }
        
        int count = 0;
        LocalDate current = start;
        while (current.isBefore(end)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    /**
     * Suma N días laborables a una fecha de inicio, saltando sábados y domingos.
     * 
     * @param start Fecha de inicio
     * @param workingDays Número de días laborables a sumar (debe ser >= 0)
     * @return Fecha resultante después de sumar N días laborables
     */
    public LocalDate plusWorkingDays(LocalDate start, int workingDays) {
        if (start == null) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser nula");
        }
        
        if (workingDays < 0) {
            throw new IllegalArgumentException("El número de días laborables no puede ser negativo");
        }
        
        // Edge case: workingDays == 0 retorna start
        if (workingDays == 0) {
            return start;
        }
        
        LocalDate result = start;
        int added = 0;
        while (added < workingDays) {
            result = result.plusDays(1);
            DayOfWeek dow = result.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return result;
    }
}
