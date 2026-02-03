package com.budgetpro.infrastructure.persistence.mapper.billetera;

import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.infrastructure.persistence.entity.billetera.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.billetera.MovimientoCajaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BilleteraMapperTest {

    private BilleteraMapper mapper;
    private final UUID proyectoId = UUID.randomUUID();
    private final BilleteraId billeteraId = BilleteraId.of(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        mapper = new BilleteraMapper();
    }

    @Test
    void toEntity_ShouldMapMonedaCorrectly() {
        Billetera billetera = Billetera.crear(billeteraId, proyectoId, "USD");

        BilleteraEntity entity = mapper.toEntity(billetera);

        assertNotNull(entity);
        assertEquals("USD", entity.getMoneda());
        assertEquals(billeteraId.getValue(), entity.getId());
    }

    @Test
    void toDomain_ShouldMapMonedaCorrectly() {
        BilleteraEntity entity = new BilleteraEntity(billeteraId.getValue(), proyectoId, "EUR", BigDecimal.TEN, 1);

        Billetera domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("EUR", domain.getMoneda());
    }

    @Test
    void toDomain_ShouldDefaultToPEN_WhenMonedaIsNull() {
        // Simulating legacy entity with null moneda
        BilleteraEntity entity = new BilleteraEntity(billeteraId.getValue(), proyectoId, null, BigDecimal.TEN, 1);

        Billetera domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals("PEN", domain.getMoneda());
    }

    @Test
    void toMovimientoEntity_ShouldMapMonedaCorrectly() {
        Billetera billetera = Billetera.crear(billeteraId, proyectoId, "USD");
        MovimientoCaja movimiento = billetera.ingresar(BigDecimal.TEN, "USD", "Ref", "Url");
        BilleteraEntity billeteraEntity = mapper.toEntity(billetera);

        // Accessing private method via reflection or just testing public API if
        // exposed?
        // toMovimientoEntity is private in BilleteraMapper.
        // But toEntity calls it. We can check the movements in the resulting entity.

        assertNotNull(billeteraEntity.getMovimientos());
        assertEquals(1, billeteraEntity.getMovimientos().size());
        assertEquals("USD", billeteraEntity.getMovimientos().get(0).getMoneda());
    }

    @Test
    void toMovimientoDomain_ShouldMapMonedaCorrectly() {
        BilleteraEntity billeteraEntity = new BilleteraEntity(billeteraId.getValue(), proyectoId, "USD", BigDecimal.TEN,
                1);
        MovimientoCajaEntity movEntity = new MovimientoCajaEntity(UUID.randomUUID(), billeteraEntity, BigDecimal.TEN,
                "USD", TipoMovimiento.INGRESO, null, "Ref", "Url", null);

        MovimientoCaja movDomain = mapper.toMovimientoDomain(movEntity);

        assertNotNull(movDomain);
        assertEquals("USD", movDomain.getMoneda());
    }

    @Test
    void toMovimientoDomain_ShouldDefaultToPEN_WhenMonedaIsNull() {
        BilleteraEntity billeteraEntity = new BilleteraEntity(billeteraId.getValue(), proyectoId, "PEN", BigDecimal.TEN,
                1);
        MovimientoCajaEntity movEntity = new MovimientoCajaEntity(UUID.randomUUID(), billeteraEntity, BigDecimal.TEN,
                null, // Null moneda
                TipoMovimiento.INGRESO, null, "Ref", "Url", null);

        MovimientoCaja movDomain = mapper.toMovimientoDomain(movEntity);

        assertNotNull(movDomain);
        assertEquals("PEN", movDomain.getMoneda());
    }
}
