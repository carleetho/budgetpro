package com.budgetpro.application.partida.usecase;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;
import com.budgetpro.application.partida.port.in.CrearPartidaUseCase;
import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.CodigoPartida;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.presupuesto.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

/**
 * Implementación del caso de uso para crear una nueva partida.
 * 
 * Responsabilidades:
 * - Orquestar el flujo de creación de partidas
 * - Validar reglas de aplicación (duplicados, etc.)
 * - Coordinar entre el dominio y la persistencia
 * - Controlar transacciones
 * 
 * NO contiene lógica de negocio profunda (eso está en el Agregado Partida).
 */
@Service
@Validated
@Transactional
public class CrearPartidaUseCaseImpl implements CrearPartidaUseCase {

    private final PresupuestoRepository presupuestoRepository;

    public CrearPartidaUseCaseImpl(PresupuestoRepository presupuestoRepository) {
        this.presupuestoRepository = presupuestoRepository;
    }

    @Override
    public PartidaResponse ejecutar(CrearPartidaCommand command) {
        // 1. Convertir presupuestoId a Value Object
        PresupuestoId presupuestoId = PresupuestoId.of(command.presupuestoId());

        // 2. Obtener el agregado Presupuesto (con sus partidas)
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No existe un presupuesto con ID %s", command.presupuestoId())
                ));

        // 3. Normalizar el código para verificar duplicados
        CodigoPartida codigo = CodigoPartida.of(command.codigo()); // Normaliza automáticamente (trim + uppercase)

        // 4. Verificar si ya existe una partida con el mismo código en el presupuesto
        if (presupuesto.buscarPartidaPorCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException(
                String.format("Ya existe una partida con código %s en el presupuesto %s", 
                    codigo.getValue(), command.presupuestoId())
            );
        }

        // 5. Convertir el tipo de String a Enum del dominio
        TipoRecurso tipoRecurso = parsearTipoRecurso(command.tipo());

        // 6. Convertir el monto presupuestado a Value Object del dominio
        Monto montoPresupuestado = Monto.of(command.montoPresupuestado());

        // 7. Agregar la partida al agregado Presupuesto (método del agregado raíz)
        Partida nuevaPartida = presupuesto.agregarPartida(codigo, command.nombre(), tipoRecurso, montoPresupuestado);

        // 8. Persistir el agregado Presupuesto (con la nueva partida incluida)
        presupuestoRepository.save(presupuesto);

        // 9. Convertir la partida agregada a DTO de respuesta
        return toResponse(nuevaPartida);
    }

    /**
     * Convierte un String a TipoRecurso (Enum del dominio).
     * 
     * @param tipoStr El tipo como String
     * @return El TipoRecurso correspondiente
     * @throws IllegalArgumentException si el tipo no es válido
     */
    private TipoRecurso parsearTipoRecurso(String tipoStr) {
        if (tipoStr == null || tipoStr.isBlank()) {
            throw new IllegalArgumentException("El tipo de la partida no puede estar vacío");
        }
        try {
            return TipoRecurso.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Tipo de partida inválido: " + tipoStr + ". Valores válidos: " +
                java.util.Arrays.toString(TipoRecurso.values()), e);
        }
    }

    /**
     * Convierte un agregado del dominio a DTO de respuesta.
     * 
     * @param partida El agregado del dominio
     * @return El DTO de respuesta con saldos desglosados
     */
    private PartidaResponse toResponse(Partida partida) {
        return new PartidaResponse(
            partida.getId().getValue(),
            partida.getProyectoId(),
            partida.getPresupuestoId(),
            partida.getCodigo().getValue(),
            partida.getNombre(),
            partida.getTipo().name(),
            mapEstado(partida.getEstado()),
            new PartidaResponse.SaldosPartidaResponse(
                partida.getMontoPresupuestado().toBigDecimal(),
                partida.getMontoReservado().toBigDecimal(),
                partida.getMontoEjecutado().toBigDecimal(),
                partida.getSaldoDisponible().toBigDecimal()
            ),
            partida.getVersion()
        );
    }

    /**
     * Mapea el estado del dominio al DTO de respuesta.
     */
    private PartidaResponse.EstadoPartidaResponse mapEstado(com.budgetpro.domain.finanzas.partida.EstadoPartida estado) {
        return switch (estado) {
            case BORRADOR -> PartidaResponse.EstadoPartidaResponse.BORRADOR;
            case APROBADA -> PartidaResponse.EstadoPartidaResponse.APROBADA;
            case CERRADA -> PartidaResponse.EstadoPartidaResponse.CERRADA;
        };
    }
}
