package com.budgetpro.domain.rrhh.service;

import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboral;
import com.budgetpro.domain.finanzas.sobrecosto.model.ConfiguracionLaboralId;
import com.budgetpro.domain.rrhh.model.Contacto;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorFSRTest {

    @Test
    void fsr_esTotalPagadoEntreDiasLaborables_seisDecimales() {
        ConfiguracionLaboral config = ConfiguracionLaboral.reconstruir(ConfiguracionLaboralId.nuevo(), null, 15, 12,
                BigDecimal.ZERO, 7, 251, 0L);
        Empleado empleado = Empleado.crear(EmpleadoId.generate(), "X", "Y", "ID-FSR", Contacto.of("a@b.com", null, null),
                LocalDate.of(2020, 1, 1), new BigDecimal("5000"), "Op", TipoEmpleado.PERMANENTE);

        BigDecimal esperado = new BigDecimal("285").divide(new BigDecimal("251"), 6, RoundingMode.HALF_UP);
        assertEquals(esperado, new CalculadorFSR().calcularFSR(config, empleado));
        assertEquals(new BigDecimal("1.135458"), esperado);
    }
}
