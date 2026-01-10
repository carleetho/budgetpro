package com.budgetpro.application.finanzas.usecase;

import com.budgetpro.application.finanzas.dto.EgresarFondosCommand;
import com.budgetpro.application.finanzas.dto.MovimientoResponse;
import com.budgetpro.application.finanzas.port.in.EgresarFondosUseCase;
import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.billetera.Monto;
import com.budgetpro.domain.finanzas.billetera.Movimiento;
import com.budgetpro.domain.finanzas.billetera.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Implementación del caso de uso para egresar fondos de una billetera.
 * 
 * Responsabilidades:
 * - Orquestar el flujo de egreso de fondos
 * - Buscar la billetera del proyecto (debe existir para egresar)
 * - Coordinar entre el dominio y la persistencia
 * - Controlar transacciones
 * - Manejar excepciones de dominio (SaldoInsuficienteException)
 * 
 * REGLA: El proyecto debe tener una billetera existente para poder egresar fondos.
 * Si no existe, se lanza una excepción.
 * 
 * INVARIANTE CRÍTICA: El dominio valida que el saldo no quede negativo.
 * Si el saldo resultante sería negativo, el dominio lanza SaldoInsuficienteException.
 */
@Service
@Validated
@Transactional
public class EgresarFondosUseCaseImpl implements EgresarFondosUseCase {

    private final BilleteraRepository billeteraRepository;

    public EgresarFondosUseCaseImpl(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public MovimientoResponse ejecutar(EgresarFondosCommand command) {
        UUID proyectoId = command.proyectoId();

        // 1. Buscar la billetera del proyecto (debe existir para egresar)
        Billetera billetera = billeteraRepository.findByProyectoId(proyectoId)
                .orElseThrow(() -> new IllegalStateException(
                    "No existe una billetera para el proyecto: " + proyectoId + 
                    ". Se debe crear primero o ingresar fondos iniciales."));

        // 2. Convertir BigDecimal a Monto (Value Object del dominio)
        Monto monto = Monto.of(command.monto());

        // 3. Ejecutar el egreso en el agregado (esto crea el movimiento y actualiza el saldo)
        // El dominio valida que:
        // - El monto sea positivo
        // - El saldo resultante NO sea negativo (INVARIANTE CRÍTICA)
        // Si el saldo resultante sería negativo, lanza SaldoInsuficienteException
        Movimiento movimiento;
        try {
            movimiento = billetera.egresar(monto, command.referencia(), command.evidenciaUrl());
        } catch (SaldoInsuficienteException e) {
            // Relanzar la excepción de dominio (ya tiene el mensaje apropiado)
            // El Controller Advice se encargará de convertirlo a HTTP 422 o 400
            throw e;
        }

        // 4. Persistir la billetera actualizada (con el nuevo movimiento y saldo actualizado)
        // El repositorio guardará la billetera y los movimientos nuevos en una transacción única
        billeteraRepository.save(billetera);

        // 5. Convertir el movimiento del dominio a DTO de respuesta
        return toResponse(movimiento);
    }

    /**
     * Convierte un Movimiento del dominio a DTO de respuesta.
     * 
     * @param movimiento El movimiento del dominio
     * @return El DTO de respuesta
     */
    private MovimientoResponse toResponse(Movimiento movimiento) {
        return new MovimientoResponse(
            movimiento.getId(),
            movimiento.getBilleteraId().getValue(),
            movimiento.getMonto().toBigDecimal(),
            movimiento.getTipo().name(),
            movimiento.getFecha(),
            movimiento.getReferencia(),
            movimiento.getEvidenciaUrl()
        );
    }
}
