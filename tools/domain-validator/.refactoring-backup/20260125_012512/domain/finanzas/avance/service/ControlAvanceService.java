package com.budgetpro.domain.finanzas.avance.service;

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
 * Responsabilidad:
 * - Registrar avances físicos
 * - Calcular porcentaje de avance acumulado
 * - Validar que el acumulado no supere el metrado total (alertar, no bloquear)
 * 
 * No persiste directamente, orquesta la lógica de dominio.
 */
public class ControlAvanceService {

    private final AvanceFisicoRepository avanceFisicoRepository;

    public ControlAvanceService(AvanceFisicoRepository avanceFisicoRepository) {
        this.avanceFisicoRepository = avanceFisicoRepository;
    }

    /**
     * Registra un avance físico para una partida.
     * 
     * @param partida La partida a la que se le registra el avance
     * @param metradoEjecutado La cantidad física ejecutada
     * @param fecha La fecha del avance
     * @param observacion Observación opcional
     * @return El avance físico creado
     */
    public AvanceFisico registrarAvance(Partida partida, BigDecimal metradoEjecutado,
                                        java.time.LocalDate fecha, String observacion) {
        // Validar que el metrado ejecutado no sea negativo
        if (metradoEjecutado == null || metradoEjecutado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El metrado ejecutado no puede ser negativo");
        }

        // (Opcional MVP) Validar que el acumulado no supere el metrado total (solo alertar)
        BigDecimal acumulado = calcularMetradoAcumulado(partida.getId().getValue());
        BigDecimal nuevoAcumulado = acumulado.add(metradoEjecutado);
        
        if (partida.getMetrado().compareTo(BigDecimal.ZERO) > 0 && 
            nuevoAcumulado.compareTo(partida.getMetrado()) > 0) {
            // Alertar pero no bloquear (MVP)
            // En producción, se podría lanzar una excepción o generar un evento de dominio
            System.out.println(String.format(
                "ADVERTENCIA: El acumulado (%s) supera el metrado total (%s) de la partida %s",
                nuevoAcumulado, partida.getMetrado(), partida.getId()
            ));
        }

        // Crear el avance físico
        com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId id = 
            com.budgetpro.domain.finanzas.avance.model.AvanceFisicoId.nuevo();
        
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
        return avances.stream()
                .map(AvanceFisico::getMetradoEjecutado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        return acumulado
                .divide(partida.getMetrado(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}
