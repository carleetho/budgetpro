package com.budgetpro.domain.finanzas.evm.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida (persistencia) para {@code EVMTimeSeries}.
 *
 * <p>
 * Esta interfaz define el contrato de acceso a la serie temporal de métricas EVM
 * (S-Curve / Forecast). Su implementación concreta vive en infraestructura.
 * </p>
 */
public interface EVMTimeSeriesRepository {

    /**
     * Persiste una entrada de serie temporal EVM.
     *
     * @param timeSeries entrada a persistir
     */
    void save(com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries timeSeries);

    /**
     * Consulta la serie temporal por proyecto en un rango de fechas.
     *
     * <p>
     * Reglas del rango:
     * </p>
     * <ul>
     *   <li>Si {@code startDate} es {@code null}, el rango es no acotado por inicio.</li>
     *   <li>Si {@code endDate} es {@code null}, el rango es no acotado por fin.</li>
     *   <li>El resultado debe venir ordenado por fecha de corte ASC (y/o por período ASC).</li>
     * </ul>
     *
     * @param proyectoId id del proyecto
     * @param startDate fecha inicial (nullable)
     * @param endDate fecha final (nullable)
     * @return lista ordenada ascendente de entradas de serie temporal
     */
    List<com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries> findByProyectoId(
            UUID proyectoId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * Obtiene la última entrada de serie temporal del proyecto (sin lock).
     *
     * <p>
     * Uso exclusivo: consultas de lectura (S-Curve / Forecast). No usar en flujos
     * que deriven el siguiente {@code periodo}.
     * </p>
     *
     * @param proyectoId id del proyecto
     * @return última entrada, si existe
     */
    Optional<com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries> findLatestByProyectoId(UUID proyectoId);

    /**
     * Obtiene la última entrada de serie temporal del proyecto con lock pesimista de escritura.
     *
     * <p>
     * <b>(a) Por qué existe:</b> este método define el contrato de concurrencia para
     * el flujo de escritura del listener que genera nuevos períodos de {@code EVMTimeSeries}.
     * </p>
     *
     * <p>
     * <b>(b) TOCTOU que previene:</b> sin lock, dos ejecuciones concurrentes pueden leer el mismo
     * "último período" y ambas calcular {@code nextPeriodo = ultimo.getPeriodo() + 1}, causando
     * colisiones/duplicados (race condición de time-of-check vs time-of-use).
     * </p>
     *
     * <p>
     * <b>(c) Hint de implementación (JPA):</b> la implementación en infraestructura debería aplicar
     * {@code @Lock(LockModeType.PESSIMISTIC_WRITE)} sobre la consulta que obtiene la última fila
     * (y ordenar de forma determinista). Esto garantiza exclusión mutua a nivel de fila.
     * </p>
     *
     * <p>
     * <b>(d) Patrón de referencia:</b> ver {@code docs/canonical/modules/INVENTARIO_MODULE_CANONICAL.md}
     * sección <i>11. Technical Debt &amp; Risks</i> (Locking / Row Locking) como guía de gobierno
     * para escenarios de alta concurrencia.
     * </p>
     *
     * <p>
     * Secuenciación esperada del período (caller-side):
     * {@code findLatestWithLock(proyectoId).map(ts -> ts.getPeriodo() + 1).orElse(1)}.
     * </p>
     *
     * @param proyectoId id del proyecto
     * @return última entrada (con lock), si existe
     */
    Optional<com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries> findLatestWithLock(UUID proyectoId);
}

