package com.budgetpro.domain.finanzas.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BilleteraTest {

    private Billetera billetera;
    private final BilleteraId billeteraId = BilleteraId.of(UUID.randomUUID());
    private final UUID proyectoId = UUID.randomUUID();
    private final String MONEDA_PEN = "PEN";
    private final String MONEDA_USD = "USD";

    @BeforeEach
    void setUp() {
        billetera = Billetera.crear(billeteraId, proyectoId, MONEDA_PEN);
    }

    @Test
    void testCurrencyMatch_InIngreso() {
        BigDecimal monto = new BigDecimal("100.00");
        assertDoesNotThrow(() -> billetera.ingresar(monto, MONEDA_PEN, "Ingreso válido", "http://evidence.url"));
        assertEquals(monto, billetera.getSaldoActual());
    }

    @Test
    void testCurrencyMixValidation_InIngreso() {
        BigDecimal monto = new BigDecimal("50.00");
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> billetera.ingresar(monto, MONEDA_USD, "Ingreso inválido", "http://evidence.url"));

        String expectedMessage = String.format(
                "Currency mismatch: Wallet currency (%s) does not match movement currency (%s)", MONEDA_PEN,
                MONEDA_USD);

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    // Note: Egreso test requires mocking strict checks (IntegrityHashService, etc.)
    // or bypassing them if possible.
    // However, Billetera.egresar performs currency check BEFORE budget integrity
    // check.
    // So passing null/dummy for Budget ID/Validation might work if we verify
    // exception order.
    // Let's verify: egresar order is amount -> evidence -> currency -> budget
    // integrity.
    // So if we fail currency, we shouldn't hit budget integrity.

    @Test
    void testCurrencyMixValidation_InEgreso() {
        // First deposit some money so we have balance
        billetera.ingresar(new BigDecimal("1000.00"), MONEDA_PEN, "Saldo inicial", "http://evidence.url");

        BigDecimal montoEgreso = new BigDecimal("50.00");
        // We pass dummy values for budget params because currency check should fail
        // first
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> billetera.egresar(montoEgreso, MONEDA_USD, "Egreso inválido", "http://evidence.url", null, true) // PresupuestoId
                                                                                                                       // null
                                                                                                                       // might
                                                                                                                       // be
                                                                                                                       // checked
                                                                                                                       // inside
                                                                                                                       // exception?
                                                                                                                       // No,
                                                                                                                       // primitive
                                                                                                                       // boolean
                                                                                                                       // isPresupuestoValid
        );

        String expectedMessage = String.format(
                "Currency mismatch: Wallet currency (%s) does not match movement currency (%s)", MONEDA_PEN,
                MONEDA_USD);

        assertTrue(exception.getMessage().contains("Currency mismatch"));
    }

    @Test
    void testCurrencyMatch_InEgreso() {
        // Setup balance
        billetera.ingresar(new BigDecimal("1000.00"), MONEDA_PEN, "Saldo inicial", "http://evidence.url");

        BigDecimal montoEgreso = new BigDecimal("100.00");
        // We simulate valid budget integrity to let it pass
        assertDoesNotThrow(
                () -> billetera.egresar(montoEgreso, MONEDA_PEN, "Egreso válido", "http://evidence.url", null, true));

        assertEquals(new BigDecimal("900.00"), billetera.getSaldoActual());
    }
}
