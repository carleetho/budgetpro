package com.budgetpro.application.finanzas.usecase;

import com.budgetpro.application.finanzas.dto.BilleteraResponse;
import com.budgetpro.application.finanzas.dto.CrearBilleteraCommand;
import com.budgetpro.application.finanzas.exception.BilleteraDuplicadaException;
import com.budgetpro.application.finanzas.port.in.CrearBilleteraUseCase;
import com.budgetpro.domain.finanzas.billetera.Billetera;
import com.budgetpro.domain.finanzas.port.out.BilleteraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Implementación del caso de uso para crear una nueva billetera.
 * 
 * Responsabilidades:
 * - Orquestar el flujo de creación de billeteras
 * - Validar reglas de aplicación (duplicados: proyecto ya tiene billetera)
 * - Coordinar entre el dominio y la persistencia
 * - Controlar transacciones
 * 
 * NO contiene lógica de negocio profunda (eso está en el Agregado Billetera).
 */
@Service
@Validated
@Transactional
public class CrearBilleteraUseCaseImpl implements CrearBilleteraUseCase {

    private final BilleteraRepository billeteraRepository;

    public CrearBilleteraUseCaseImpl(BilleteraRepository billeteraRepository) {
        this.billeteraRepository = billeteraRepository;
    }

    @Override
    public BilleteraResponse ejecutar(CrearBilleteraCommand command) {
        UUID proyectoId = command.proyectoId();

        // 1. Verificar si el proyecto ya tiene una billetera (regla 1:1)
        if (billeteraRepository.findByProyectoId(proyectoId).isPresent()) {
            throw new BilleteraDuplicadaException(proyectoId);
        }

        // 2. Crear el agregado usando el factory method del dominio
        // Esto disparará el evento BilleteraCreada
        Billetera nuevaBilletera = Billetera.crear(proyectoId);

        // 3. Persistir el agregado usando el repositorio
        // El repositorio guardará la billetera y sus eventos/movimientos en una transacción única
        billeteraRepository.save(nuevaBilletera);

        // 4. Convertir el agregado a DTO de respuesta
        return toResponse(nuevaBilletera);
    }

    /**
     * Convierte un agregado del dominio a DTO de respuesta.
     * 
     * @param billetera El agregado del dominio
     * @return El DTO de respuesta
     */
    private BilleteraResponse toResponse(Billetera billetera) {
        return new BilleteraResponse(
            billetera.getId().getValue(),
            billetera.getProyectoId(),
            billetera.getSaldoActual().toBigDecimal(),
            billetera.getVersion()
        );
    }
}
