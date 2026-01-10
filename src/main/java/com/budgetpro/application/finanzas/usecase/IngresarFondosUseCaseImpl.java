package com.budgetpro.application.finanzas.usecase;

import com.budgetpro.application.finanzas.dto.IngresarFondosCommand;
import com.budgetpro.application.finanzas.dto.MovimientoResponse;
import com.budgetpro.application.finanzas.port.in.IngresarFondosUseCase;
import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.billetera.Monto;
import com.budgetpro.domain.finanzas.billetera.Movimiento;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para ingresar fondos a una billetera.
 * 
 * Responsabilidades:
 * - Orquestar el flujo de ingreso de fondos
 * - Buscar o crear la billetera del proyecto (si no existe, se crea automáticamente)
 * - Coordinar entre el dominio y la persistencia
 * - Controlar transacciones
 * 
 * REGLA: Si el proyecto no tiene billetera, se crea automáticamente con saldo cero.
 * Luego se ingresa el monto solicitado.
 */
@Service
@Validated
@Transactional
public class IngresarFondosUseCaseImpl implements IngresarFondosUseCase {

    private final BilleteraRepository billeteraRepository;

    public IngresarFondosUseCaseImpl(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public MovimientoResponse ejecutar(IngresarFondosCommand command) {
        UUID proyectoId = command.proyectoId();

        // 1. Convertir BigDecimal a Monto (Value Object del dominio)
        Monto monto = Monto.of(command.monto());

        // 2. Buscar la billetera del proyecto, o crearla si no existe
        Optional<Billetera> billeteraOpt = billeteraRepository.findByProyectoId(proyectoId);
        Billetera billetera;

        if (billeteraOpt.isPresent()) {
            billetera = billeteraOpt.get();
        } else {
            // Crear billetera automáticamente si no existe (regla de negocio)
            billetera = Billetera.crear(proyectoId);
            // Persistir la nueva billetera primero
            billeteraRepository.save(billetera);
            // Recargar para obtener el version actualizado
            billetera = billeteraRepository.findByProyectoId(proyectoId)
                    .orElseThrow(() -> new IllegalStateException("No se pudo crear la billetera para el proyecto: " + proyectoId));
        }

        // 3. Ejecutar el ingreso en el agregado (esto crea el movimiento y actualiza el saldo)
        // El dominio valida que el monto sea positivo y crea el movimiento
        Movimiento movimiento = billetera.ingresar(monto, command.referencia(), command.evidenciaUrl());

        // 4. Persistir la billetera actualizada (con el nuevo movimiento y saldo)
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
