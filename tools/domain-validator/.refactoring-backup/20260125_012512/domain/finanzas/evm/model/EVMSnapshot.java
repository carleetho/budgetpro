package com.budgetpro.domain.finanzas.evm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root que representa un Snapshot de métricas EVM (Earned Value
 * Management).
 * 
 * Este agregado captura el desempeño técnico-financiero de un proyecto en un
 * momento dado.
 */
public final class EVMSnapshot {

    private final EVMSnapshotId id;
    private final UUID proyectoId;
    private final LocalDateTime fechaCorte;
    private final LocalDateTime fechaCalculo;

    // Métricas Base
    private final BigDecimal pv; // Planned Value (Valor Planificado)
    private final BigDecimal ev; // Earned Value (Valor Ganado)
    private final BigDecimal ac; // Actual Cost (Costo Real)
    private final BigDecimal bac; // Budget at Completion (Presupuesto Total)

    // Métricas de Variación
    private final BigDecimal cv; // Cost Variance (Variación de Costo)
    private final BigDecimal sv; // Schedule Variance (Variación de Cronograma)

    // Índices de Desempeño
    private final BigDecimal cpi; // Cost Performance Index (Índice de Desempeño de Costo)
    private final BigDecimal spi; // Schedule Performance Index (Índice de Desempeño de Cronograma)

    // Proyecciones
    private final BigDecimal eac; // Estimate at Completion (Estimado al Completar)
    private final BigDecimal etc; // Estimate to Complete (Estimado hasta Completar)
    private final BigDecimal vac; // Variance at Completion (Variación al Completar)

    private final String interpretacion;

    private EVMSnapshot(EVMSnapshotId id, UUID proyectoId, LocalDateTime fechaCorte, BigDecimal pv, BigDecimal ev,
            BigDecimal ac, BigDecimal bac) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fechaCorte = Objects.requireNonNull(fechaCorte, "La fecha de corte no puede ser nula");
        this.fechaCalculo = LocalDateTime.now();

        this.pv = Objects.requireNonNull(pv, "PV no puede ser nulo");
        this.ev = Objects.requireNonNull(ev, "EV no puede ser nulo");
        this.ac = Objects.requireNonNull(ac, "AC no puede ser nulo");
        this.bac = Objects.requireNonNull(bac, "BAC no puede ser nulo");

        validarInvariantes();

        // Calcular Métricas derivadas
        this.cv = ev.subtract(ac);
        this.sv = ev.subtract(pv);

        this.cpi = calcularIndice(ev, ac);
        this.spi = calcularIndice(ev, pv);

        // Calcular Proyecciones (Usando formula típica EAC = BAC / CPI)
        this.eac = calcularEAC(bac, this.cpi);
        this.etc = this.eac.subtract(ac);
        this.vac = bac.subtract(this.eac);

        this.interpretacion = generarInterpretacion();
    }

    /**
     * Factory method para crear un nuevo snapshot calculando métricas derivadas.
     */
    public static EVMSnapshot calcular(EVMSnapshotId id, UUID proyectoId, LocalDateTime fechaCorte, BigDecimal pv,
            BigDecimal ev, BigDecimal ac, BigDecimal bac) {
        return new EVMSnapshot(id, proyectoId, fechaCorte, pv, ev, ac, bac);
    }

    /**
     * Factory method para reconstruir un snapshot desde persistencia.
     */
    public static EVMSnapshot reconstruir(EVMSnapshotId id, UUID proyectoId, LocalDateTime fechaCorte,
            LocalDateTime fechaCalculo, BigDecimal pv, BigDecimal ev, BigDecimal ac, BigDecimal bac, BigDecimal cv,
            BigDecimal sv, BigDecimal cpi, BigDecimal spi, BigDecimal eac, BigDecimal etc, BigDecimal vac,
            String interpretacion) {
        // En reconstrucción solo validamos nulidad, no recalculamos.
        return new EVMSnapshot(id, proyectoId, fechaCorte, fechaCalculo, pv, ev, ac, bac, cv, sv, cpi, spi, eac, etc,
                vac, interpretacion);
    }

    private EVMSnapshot(EVMSnapshotId id, UUID proyectoId, LocalDateTime fechaCorte, LocalDateTime fechaCalculo,
            BigDecimal pv, BigDecimal ev, BigDecimal ac, BigDecimal bac, BigDecimal cv, BigDecimal sv, BigDecimal cpi,
            BigDecimal spi, BigDecimal eac, BigDecimal etc, BigDecimal vac, String interpretacion) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.fechaCorte = fechaCorte;
        this.fechaCalculo = fechaCalculo;
        this.pv = pv;
        this.ev = ev;
        this.ac = ac;
        this.bac = bac;
        this.cv = cv;
        this.sv = sv;
        this.cpi = cpi;
        this.spi = spi;
        this.eac = eac;
        this.etc = etc;
        this.vac = vac;
        this.interpretacion = interpretacion;
    }

    private void validarInvariantes() {
        if (ev.compareTo(bac) > 0) {
            throw new IllegalStateException("EV no puede exceder el BAC (Valor Ganado > Presupuesto al completar)");
        }
    }

    private BigDecimal calcularIndice(BigDecimal num, BigDecimal den) {
        if (den.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return num.divide(den, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularEAC(BigDecimal bac, BigDecimal cpi) {
        if (cpi.compareTo(BigDecimal.ZERO) == 0) {
            return bac; // Si no hay rendimiento, asumimos BAC como base o algún otro criterio
        }
        return bac.divide(cpi, 2, RoundingMode.HALF_UP);
    }

    private String generarInterpretacion() {
        StringBuilder sb = new StringBuilder();

        // Interpretación de Costo (CPI)
        if (cpi.compareTo(BigDecimal.ONE) < 0) {
            sb.append("Proyecto bajo presupuesto (CPI < 1.0): gastando más de lo planificado. ");
        } else if (cpi.compareTo(BigDecimal.ONE) > 0) {
            sb.append("Proyecto con ahorro en costos (CPI > 1.0). ");
        } else {
            sb.append("Proyecto conforme al presupuesto (CPI = 1.0). ");
        }

        // Interpretación de Cronograma (SPI)
        if (spi.compareTo(BigDecimal.ONE) < 0) {
            sb.append("Proyecto retrasado (SPI < 1.0). ");
        } else if (spi.compareTo(BigDecimal.ONE) > 0) {
            sb.append("Proyecto adelantado (SPI > 1.0): progreso mayor al planificado. ");
        } else {
            sb.append("Proyecto conforme al cronograma (SPI = 1.0). ");
        }

        return sb.toString().trim();
    }

    // Getters
    public EVMSnapshotId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDateTime getFechaCorte() {
        return fechaCorte;
    }

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public BigDecimal getPv() {
        return pv;
    }

    public BigDecimal getEv() {
        return ev;
    }

    public BigDecimal getAc() {
        return ac;
    }

    public BigDecimal getBac() {
        return bac;
    }

    public BigDecimal getCv() {
        return cv;
    }

    public BigDecimal getSv() {
        return sv;
    }

    public BigDecimal getCpi() {
        return cpi;
    }

    public BigDecimal getSpi() {
        return spi;
    }

    public BigDecimal getEac() {
        return eac;
    }

    public BigDecimal getEtc() {
        return etc;
    }

    public BigDecimal getVac() {
        return vac;
    }

    public String getInterpretacion() {
        return interpretacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EVMSnapshot that = (EVMSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
