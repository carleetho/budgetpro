package com.budgetpro.infrastructure.persistence.adapter.evm;

import com.budgetpro.domain.finanzas.evm.model.EVMTimeSeries;
import com.budgetpro.infrastructure.persistence.entity.evm.EVMTimeSeriesEntity;
import com.budgetpro.infrastructure.persistence.repository.evm.JpaEVMTimeSeriesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EVMTimeSeriesRepositoryAdapterTest {

    @Mock
    private JpaEVMTimeSeriesRepository jpaRepository;

    @Test
    void findLatestByProyectoId_preservesStoredPeriodIndexes() {
        UUID proyectoId = UUID.randomUUID();
        EVMTimeSeriesEntity entity = new EVMTimeSeriesEntity();
        entity.setId(UUID.randomUUID());
        entity.setProyectoId(proyectoId);
        entity.setFechaCorte(LocalDate.of(2026, 3, 1));
        entity.setPeriodo(2);
        entity.setMoneda("USD");
        entity.setPvAcumulado(new BigDecimal("150.0000"));
        entity.setEvAcumulado(new BigDecimal("150.0000"));
        entity.setAcAcumulado(new BigDecimal("150.0000"));
        entity.setBacTotal(new BigDecimal("200.0000"));
        entity.setBacAjustado(new BigDecimal("220.0000"));
        entity.setCpiPeriodo(new BigDecimal("1.2500"));
        entity.setSpiPeriodo(new BigDecimal("0.7500"));

        when(jpaRepository.findFirstByProyectoIdOrderByFechaCorteDesc(proyectoId))
                .thenReturn(Optional.of(entity));

        EVMTimeSeriesRepositoryAdapter adapter = new EVMTimeSeriesRepositoryAdapter(jpaRepository);
        Optional<EVMTimeSeries> result = adapter.findLatestByProyectoId(proyectoId);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("1.2500"), result.get().getCpiPeriodo());
        assertEquals(new BigDecimal("0.7500"), result.get().getSpiPeriodo());
    }
}
