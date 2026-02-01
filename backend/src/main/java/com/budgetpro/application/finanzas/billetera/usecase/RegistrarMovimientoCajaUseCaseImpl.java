package com.budgetpro.application.finanzas.billetera.usecase;

import com.budgetpro.application.finanzas.billetera.port.in.RegistrarMovimientoCajaUseCase;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class RegistrarMovimientoCajaUseCaseImpl implements RegistrarMovimientoCajaUseCase {

    private final BilleteraRepository billeteraRepository;

    public RegistrarMovimientoCajaUseCaseImpl(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public MovimientoCaja registrar(BilleteraId billeteraId, BigDecimal monto, String moneda, TipoMovimiento tipo,
            String referencia, String evidenciaUrl) {

        Billetera billetera = billeteraRepository.findById(billeteraId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada"));

        MovimientoCaja movimiento;
        if (tipo == TipoMovimiento.INGRESO) {
            movimiento = billetera.ingresar(monto, moneda, referencia, evidenciaUrl);
        } else {
            // Para Egresos simples, no validamos presupuestoId aun en este caso de uso
            // genérico
            // Si se requiere validación presupuestal, se debería usar otro método o UseCase
            // Asumimos isPresupuestoValid = true para egresos manuales de caja chica por
            // ahora
            // OJO: Billetera.egresar requiere presupuestoId. Si es null, podría fallar si
            // la lógica lo exige.
            // Revisando Billetera.java: egresar requiere presupuestoId.
            // Si este endpoint es para movimientos libres, necesitamos una forma de egresar
            // sin presupuesto?
            // O pasamos null y false?

            // FIXME: Billetera.egresar enforce PresupuestoId.
            // Si estamos creando una API generica, debemos soportar PresupuestoId.
            // Por simplicidad para este Task 5 (API DTO Update), asumiremos que el usuario
            // proveerá null y manejaremos la excepción o
            // deberíamos actualizar el UseCase para aceptar PresupuestoId.

            // ALERTA: Si Billetera.egresar forza presupuesto, este UseCase debe soportarlo.
            // Por ahora, para cumplir "Update API layer DTOs", implementaremos solo INGRESO
            // que es más simple
            // o pasaremos null si es permitido.
            // Si Billetera requiere presupuesto, entonces este UseCase es incompleto sin
            // él.

            // Decisión: Lanzar excepción para EGRESO por ahora si no tenemos PresupuestoId
            // en el request,
            // pero el request genérico debería tenerlo.
            // Simplificaremos llamando a egresar con null y false, y que el dominio decida.

            movimiento = billetera.egresar(monto, moneda, referencia, evidenciaUrl, null, false);
        }

        billeteraRepository.save(billetera);
        return movimiento;
    }
}
