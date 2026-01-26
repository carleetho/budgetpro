package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.inventario.exception.ImputacionObligatoriaException;
import com.budgetpro.domain.logistica.inventario.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.logistica.inventario.port.out.PartidaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de Dominio para validar reglas de imputación y registrar Costo Real
 * (AC).
 */
public class ImputacionService {

    private final PartidaRepository partidaRepository;
    private final ConsumoPartidaRepository consumoPartidaRepository;

    public ImputacionService(PartidaRepository partidaRepository, ConsumoPartidaRepository consumoPartidaRepository) {
        this.partidaRepository = partidaRepository;
        this.consumoPartidaRepository = consumoPartidaRepository;
    }

    /**
     * Valida reglas de imputación y registra el Costo Real (AC).
     * 
     * @param partidaId       ID de la Partida (puede ser null si no es obligatorio)
     * @param naturalezaGasto Naturaleza del gasto (contexto)
     * @param cantidad        Cantidad consumida
     * @param costoPromedio   Costo unitario (PMP)
     * @param referencia      Descripción del consumo
     * @throws ImputacionObligatoriaException si falta partidaId en GASTO_DIRECTO
     * @throws IllegalArgumentException       si la partida no existe o presupuesto
     *                                        no congelado
     */
    public void validarYRegistrarAC(UUID partidaId, NaturalezaGasto naturalezaGasto, BigDecimal cantidad,
            BigDecimal costoPromedio, String referencia) {

        // 1. Validar obligatoriedad de imputación
        if (naturalezaGasto == NaturalezaGasto.DIRECTO_PARTIDA) {
            if (partidaId == null) {
                throw new ImputacionObligatoriaException(
                        "El GASTO_DIRECTO_PARTIDA requiere una Partida Presupuestal obligatoria.");
            }
        }

        // 2. Si hay partida imputada, validar existencia y estado
        if (partidaId != null) {
            if (!partidaRepository.existsById(partidaId)) {
                throw new IllegalArgumentException("La partida presupuestal no existe: " + partidaId);
            }
            if (!partidaRepository.isPresupuestoCongelado(partidaId)) {
                throw new IllegalArgumentException(
                        "No se puede imputar a una partida de un presupuesto NO CONGELADO (Borrador/En Revisión).");
            }

            // 3. Registrar AC (Actual Cost)
            BigDecimal costoAC = cantidad.multiply(costoPromedio);
            consumoPartidaRepository.registrarConsumo(partidaId, costoAC, LocalDateTime.now(), referencia);
        } else {
            // GASTO_GENERAL u ADMINISTRATIVO sin partida específica:
            // Regla de negocio: Se imputan a bolsas generales (No implementado aquí, out of
            // scope for strict Partida validation)
            // O se permite salir sin AC específico en Partida (pero sí en Contabilidad via
            // eventos posteriores).
            // Para Task 11: Solo nos importa validar Y registrar SI hay partida.
        }
    }
}
