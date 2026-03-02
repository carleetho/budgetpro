package com.budgetpro.infrastructure.persistence.adapter.evm;

import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeriesId;
import com.budgetpro.domain.finanzas.evm.port.out.EVMTimeSeriesRepository;
import com.budgetpro.infrastructure.persistence.entity.evm.EVMTimeSeriesEntity;
import com.budgetpro.infrastructure.persistence.repository.evm.JpaEVMTimeSeriesRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para la serie temporal EVM.
 */
@Component
public class EVMTimeSeriesRepositoryAdapter implements EVMTimeSeriesRepository {

    private final JpaEVMTimeSeriesRepository jpaRepository;

    public EVMTimeSeriesRepositoryAdapter(JpaEVMTimeSeriesRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(EVMTimeSeries timeSeries) {
        EVMTimeSeriesEntity entity = Objects.requireNonNull(toEntity(timeSeries), "timeSeries entity no puede ser null");
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EVMTimeSeries> findByProyectoId(UUID proyectoId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByProyectoIdAndFechaCorteRangeOrderByFechaCorteAsc(proyectoId, startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EVMTimeSeries> findLatestByProyectoId(UUID proyectoId) {
        return jpaRepository.findFirstByProyectoIdOrderByFechaCorteDesc(proyectoId).map(this::toDomain);
    }

    @Override
    @Transactional
    public Optional<EVMTimeSeries> findLatestWithLock(UUID proyectoId) {
        return jpaRepository.findLatestByProyectoIdWithLock(proyectoId).map(this::toDomain);
    }

    private EVMTimeSeriesEntity toEntity(EVMTimeSeries timeSeries) {
        EVMTimeSeriesEntity entity = new EVMTimeSeriesEntity();
        entity.setId(timeSeries.getId().getValue());
        entity.setProyectoId(timeSeries.getProyectoId());
        entity.setFechaCorte(timeSeries.getFechaCorte());
        entity.setPeriodo(timeSeries.getPeriodo());
        entity.setMoneda(timeSeries.getMoneda());
        entity.setPvAcumulado(timeSeries.getPvAcumulado());
        entity.setEvAcumulado(timeSeries.getEvAcumulado());
        entity.setAcAcumulado(timeSeries.getAcAcumulado());
        entity.setBacTotal(timeSeries.getBacTotal());
        entity.setBacAjustado(timeSeries.getBacAjustado());
        entity.setCpiPeriodo(timeSeries.getCpiPeriodo());
        entity.setSpiPeriodo(timeSeries.getSpiPeriodo());
        return entity;
    }

    private EVMTimeSeries toDomain(EVMTimeSeriesEntity entity) {
        // EVMTimeSeries no expone reconstruir(); se usa crear() con baseline de deltas.
        return EVMTimeSeries.crear(
                EVMTimeSeriesId.de(entity.getId()),
                entity.getProyectoId(),
                entity.getFechaCorte(),
                entity.getPeriodo(),
                entity.getPvAcumulado(),
                entity.getEvAcumulado(),
                entity.getAcAcumulado(),
                entity.getBacTotal(),
                entity.getBacAjustado(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                entity.getMoneda());
    }
}

