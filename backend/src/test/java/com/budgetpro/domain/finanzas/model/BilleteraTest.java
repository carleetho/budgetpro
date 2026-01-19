package com.budgetpro.domain.finanzas.model;

import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BilleteraTest {

    @Mock
    private IntegrityHashService hashService;

    private Presupuesto presupuestoNoAprobado;
    private Presupuesto presupuestoAprobado;
    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        presupuestoNoAprobado = Presupuesto.crear(
                PresupuestoId.from(UUID.randomUUID()),
                proyectoId,
                "Presupuesto No Aprobado"
        );
        
        String approvalHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String executionHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        presupuestoAprobado = Presupuesto.reconstruir(
                PresupuestoId.from(UUID.randomUUID()),
                proyectoId,
                "Presupuesto Aprobado",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                approvalHash,
                executionHash,
                java.time.LocalDateTime.now(),
                UUID.randomUUID(),
                "SHA-256-v1"
        );
    }

    @Test
    void egresar_debeBloquearSiHayMasDeTresPendientes() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("1000.00"), "Ingreso base", "http://evidencia/ok");

        billetera.egresar(new BigDecimal("100.00"), "Egreso 1", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 2", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 3", null, presupuestoNoAprobado, hashService);

        assertEquals(3, billetera.contarMovimientosPendientesEvidencia());
        assertThrows(IllegalStateException.class,
                () -> billetera.egresar(new BigDecimal("50.00"), "Egreso 4", null, presupuestoNoAprobado, hashService));
    }

    @Test
    void egresar_conEvidenciaDebePermitirCuandoHayTresPendientes() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("1000.00"), "Ingreso base", "http://evidencia/ok");

        billetera.egresar(new BigDecimal("100.00"), "Egreso 1", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 2", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 3", null, presupuestoNoAprobado, hashService);

        billetera.egresar(new BigDecimal("50.00"), "Egreso 4", "http://evidencia/ok", presupuestoNoAprobado, hashService);

        assertEquals(3, billetera.contarMovimientosPendientesEvidencia());
    }

    @Test
    void egresar_conPresupuestoAprobadoYHashValido_debePermitirEgreso() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso base", "http://evidencia/ok");

        // Mock hash service para que retorne el mismo hash que el presupuesto tiene almacenado
        when(hashService.calculateApprovalHash(presupuestoAprobado))
                .thenReturn(presupuestoAprobado.getIntegrityHashApproval());

        MovimientoCaja movimiento = billetera.egresar(
                new BigDecimal("1000.00"),
                "Test expense",
                "http://evidence.com/doc.pdf",
                presupuestoAprobado,
                hashService
        );

        assertNotNull(movimiento);
        assertEquals(new BigDecimal("9000.00"), billetera.getSaldoActual());
    }

    @Test
    void egresar_conPresupuestoAprobadoYHashInvalido_debeRechazar() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso base", "http://evidencia/ok");

        // Simular violación de integridad: el hash calculado no coincide con el almacenado
        when(hashService.calculateApprovalHash(presupuestoAprobado))
                .thenReturn("tampered_hash_123456789012345678901234567890123456789012345678901234567890");

        assertThrows(BudgetIntegrityViolationException.class, () -> {
            billetera.egresar(
                    new BigDecimal("1000.00"),
                    "Test expense",
                    "http://evidence.com/doc.pdf",
                    presupuestoAprobado,
                    hashService
            );
        });

        // Verificar que el saldo no cambió
        assertEquals(new BigDecimal("10000.00"), billetera.getSaldoActual());
    }

    @Test
    void egresar_conPresupuestoNoAprobado_debePermitirEgreso() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso base", "http://evidencia/ok");

        // Presupuesto no aprobado no requiere validación de integridad
        MovimientoCaja movimiento = billetera.egresar(
                new BigDecimal("1000.00"),
                "Test expense",
                "http://evidence.com/doc.pdf",
                presupuestoNoAprobado,
                hashService
        );

        assertNotNull(movimiento);
        assertEquals(new BigDecimal("9000.00"), billetera.getSaldoActual());
    }

    @Test
    void egresar_conCD04YHashValido_debeValidarAmbos() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso base", "http://evidencia/ok");

        // Crear 3 movimientos pendientes de evidencia (máximo permitido)
        billetera.egresar(new BigDecimal("100.00"), "Egreso 1", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 2", null, presupuestoNoAprobado, hashService);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 3", null, presupuestoNoAprobado, hashService);

        assertEquals(3, billetera.contarMovimientosPendientesEvidencia());

        // Mock hash service (aunque no debería llegar a validar hash porque CD-04 bloquea primero)
        // Usar lenient() porque este stub no se usará si CD-04 falla primero
        lenient().when(hashService.calculateApprovalHash(any(Presupuesto.class)))
                .thenReturn(presupuestoAprobado.getIntegrityHashApproval());

        // Debe fallar en CD-04 antes de validar hash (intentar crear 4to movimiento sin evidencia)
        assertThrows(IllegalStateException.class, () -> {
            billetera.egresar(
                    new BigDecimal("1000.00"),
                    "Test",
                    null, // Sin evidencia, debería fallar en CD-04
                    presupuestoAprobado,
                    hashService
            );
        });
    }

    @Test
    void egresar_conPresupuestoNull_debePermitirEgreso() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(new BigDecimal("10000.00"), "Ingreso base", "http://evidencia/ok");

        // Presupuesto null no requiere validación de integridad
        MovimientoCaja movimiento = billetera.egresar(
                new BigDecimal("1000.00"),
                "Test expense",
                "http://evidence.com/doc.pdf",
                null,
                hashService
        );

        assertNotNull(movimiento);
        assertEquals(new BigDecimal("9000.00"), billetera.getSaldoActual());
    }
}
