package com.budgetpro.application.finanzas.usecase;

import com.budgetpro.application.finanzas.dto.SaldoResponse;
import com.budgetpro.application.finanzas.port.in.ConsultarSaldoUseCase;
import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso para consultar el saldo de una billetera.
 * 
 * Responsabilidades:
 * - Orquestar la consulta del saldo
 * - Buscar la billetera del proyecto
 * - Convertir el saldo del dominio a DTO de respuesta
 * 
 * Cumple con el requisito S1-07: "Query: Saldo actual por proyecto"
 * 
 * REGLA: Si el proyecto no tiene billetera, retorna Optional.empty().
 * Esto permite que la capa de presentación maneje el caso "billetera no existe" apropiadamente.
 */
@Service
@Validated
@Transactional(readOnly = true)
public class ConsultarSaldoUseCaseImpl implements ConsultarSaldoUseCase {

    private final BilleteraRepository billeteraRepository;

    public ConsultarSaldoUseCaseImpl(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public Optional<SaldoResponse> ejecutar(UUID proyectoId) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El ID del proyecto no puede ser nulo");
        }

        // 1. Buscar la billetera del proyecto
        Optional<Billetera> billeteraOpt = billeteraRepository.findByProyectoId(proyectoId);

        // 2. Si no existe, retornar Optional.empty()
        if (billeteraOpt.isEmpty()) {
            return Optional.empty();
        }

        // 3. Convertir el saldo del dominio a DTO de respuesta
        Billetera billetera = billeteraOpt.get();
        SaldoResponse response = new SaldoResponse(
            billetera.getProyectoId(),
            billetera.getSaldoActual().toBigDecimal(),
            "USD" // Por defecto USD, puede ser parametrizado según el sistema
        );

        return Optional.of(response);
    }
}
