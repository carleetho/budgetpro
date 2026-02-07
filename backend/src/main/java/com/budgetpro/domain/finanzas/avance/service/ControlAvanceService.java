package com.budgetpro.domain.finanzas.avance.service;

import com.budgetpro.domain.finanzas.avance.exception.MetradoExcedidoException;
import com.budgetpro.domain.finanzas.avance.model.AvanceFisico;
import com.budgetpro.domain.finanzas.avance.port.out.AvanceFisicoRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de Dominio para controlar el avance físico de partidas.
 * 
 * Responsabilidad: - Registrar avances físicos - Calcular porcentaje de avance
 * acumulado - **APLICAR STRICT MODE**: Validar que el acumulado no supere el
 * metrado total (BLOQUEAR)
 * 
 * No persiste directamente, orquesta la lógica de dominio.
 */
public class ControlAvanceService {

    private final AvanceFisicoRepository avanceFisicoRepository;

    /**
     * Strict mode para enforcement de metrado cap. true: Lanza
     * MetradoExcedidoException si se excede el metrado (PRODUCTION) false: Solo
     * alerta (legacy/development mode)
     * 
     * AXIOM: Default TRUE para Phase 0 (E-01 gap remediation)
     */
    private final boolean strictMetradoCap;

    public ControlAvanceService(AvanceFisicoRepository avanceFisicoRepository) {
        this(avanceFisicoRepository, true); // Strict mode por defecto
    }

    public ControlAvanceService(AvanceFisicoRepository avanceFisicoRepository, boolean strictMetradoCap) {
        this.avanceFisicoRepository = avanceFisicoRepository;
        this.strictMetradoCap = strictMetradoCap;
    }

    /**
     * Registra un avance físico para una partida.
     * 
     * @param partida          La partida a la que se le registra el avance
     * @param metradoEjecutado La cantidad física ejecutada
     * @param fecha            La fecha del avance
     * @param observacion      Observación opcional
     * @return El avance físico creado
     * @throws MetradoExcedidoException Si strict mode está activo y el acumulado
     *                                  excede el metrado
     */
    public AvanceFisico registrarAvance(Partida partida, BigDecimal metradoEjecutado, java.time.LocalDate fecha,
            String observacion) {
        // Validar que el metrado ejecutado no sea negativo
        if (metradoEjecutado == null || metradoEjecutado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El metrado ejecutado no puede ser negativo");
        }

        // E-01: Validar que el acumulado no supere el metrado total (STRICT MODE)
        BigDecimal acumulado = calcularMetradoAcumulado(partida.getId().getValue());
        BigDecimal nuevoAcumulado = acumulado.add(metradoEjecutado);

        if (partida.getMetrado().compareTo(BigDecimal.ZERO) > 0 && nuevoAcumulado.compareTo(partida.getMetrado()) > 0) {

            if (strictMetradoCap) {
                // STRICT MODE: Lanzar excepción (AXIOM E-01 enforcement)
                throw new MetradoExcedidoException(partida.getId().getValue(), partida.getMetrado(), nuevoAcumulado,
                        String.format(
                                "Cannot exceed budgeted metrado (%s). Accumulated: %s. Requires Change Order approval.",
                                partida.getMetrado(), nuevoAcumulado));
            } else {
                // LEGACY MODE: Solo alertar (deprecated - to be removed in future versions)
                System.out.println(
                        String.format("ADVERTENCIA: El acumulado (%s) supera el metrado total (%s) de la partida %s",
                                nuevoAcumulado, partida.getMetrado(), partida.getId()));
            }
        }

        // Crear el avance físico
        com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId id = com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId
                .nuevo();

        return AvanceFisico.crear(id, partida.getId().getValue(), fecha, metradoEjecutado, observacion);
    }

    /**
     * Calcula el metrado acumulado de una partida (suma de todos los avances).
     * 
     * @param partidaId El ID de la partida
     * @return El metrado acumulado
     */
    public BigDecimal calcularMetradoAcumulado(UUID partidaId) {
        List<AvanceFisico> avances = avanceFisicoRepository.findByPartidaId(partidaId);
        return avances.stream().map(AvanceFisico::getMetradoEjecutado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el porcentaje de avance de una partida.
     * 
     * Fórmula: (Metrado Acumulado / Metrado Total) * 100
     * 
     * @param partida La partida
     * @return El porcentaje de avance (0-100)
     */
    public BigDecimal calcularPorcentajeAvance(Partida partida) {
        if (partida.getMetrado().compareTo(BigDecimal.ZERO) == 0) {
            // Si es título (metrado = 0), retornar 0
            return BigDecimal.ZERO;
        }

        BigDecimal acumulado = calcularMetradoAcumulado(partida.getId().getValue());

        if (acumulado.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return acumulado.divide(partida.getMetrado(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
}
