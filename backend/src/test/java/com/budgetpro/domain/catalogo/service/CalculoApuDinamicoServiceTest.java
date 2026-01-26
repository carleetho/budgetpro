package com.budgetpro.domain.catalogo.service;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.ComposicionCuadrillaSnapshot;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CalculoApuDinamicoServiceTest {

    private CalculoApuDinamicoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new CalculoApuDinamicoService();
    }

    @Test
    void deberiaCalcularCostoMaterialConDesperdicio() {
        APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.of(UUID.randomUUID()),
                "MAT-001",
                "Cemento",
                new BigDecimal("9.73"),
                new BigDecimal("22.50"),
                TipoRecurso.MATERIAL,
                1,
                new BigDecimal("9.73"),
                "BOL",
                "KG",
                new BigDecimal("42.5"),
                "BOL",
                "PEN",
                BigDecimal.ONE,
                new BigDecimal("22.50"),
                BigDecimal.ZERO,
                new BigDecimal("22.50"),
                new BigDecimal("0.05"), // 5% desperdicio
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        BigDecimal costo = servicio.calcularCostoInsumo(
                insumo,
                new BigDecimal("25.0"),
                BigDecimal.ZERO,
                "PEN"
        );

        // Precio 22.50 × Aporte 9.73 × (1 + 0.05) = 229.94 (redondeado a 2 decimales)
        // Cálculo exacto: 22.50 × 9.73 × 1.05 = 229.87125 → 229.87
        assertEquals(new BigDecimal("229.87"), costo);
    }

    @Test
    void deberiaCalcularCostoManoObraConCuadrillaCompuesta() {
        List<ComposicionCuadrillaSnapshot> cuadrilla = List.of(
                new ComposicionCuadrillaSnapshot("PERS-001", "Capataz", new BigDecimal("0.1"), new BigDecimal("120.00"), "PEN"),
                new ComposicionCuadrillaSnapshot("PERS-002", "Operario", new BigDecimal("1.0"), new BigDecimal("80.00"), "PEN"),
                new ComposicionCuadrillaSnapshot("PERS-003", "Peón", new BigDecimal("2.0"), new BigDecimal("60.00"), "PEN")
        );

        APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.of(UUID.randomUUID()),
                "MO-001",
                "Mano de Obra",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                TipoRecurso.MANO_OBRA,
                2,
                BigDecimal.ONE,
                "CUADRILLA",
                null,
                null,
                null,
                "PEN",
                BigDecimal.ONE,
                null,
                null,
                null,
                null,
                cuadrilla,
                null,
                8,
                null,
                null,
                null,
                null
        );

        BigDecimal costo = servicio.calcularCostoInsumo(
                insumo,
                new BigDecimal("25.0"), // rendimiento 25 m³/día
                BigDecimal.ZERO,
                "PEN"
        );

        // Costo día cuadrilla: (0.1 × 120) + (1.0 × 80) + (2.0 × 60) = 212
        // Costo unitario: 212 / 25 × 1.0 = 8.48 (redondeado a 2 decimales)
        // Cálculo exacto: 212 / 25 = 8.48
        assertTrue(costo.compareTo(new BigDecimal("8.47")) > 0 && 
                   costo.compareTo(new BigDecimal("8.49")) < 0);
    }

    @Test
    void deberiaCalcularCostoEquipoHerramienta() {
        APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(
                APUInsumoSnapshotId.of(UUID.randomUUID()),
                "HERR-001",
                "Herramientas",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                TipoRecurso.EQUIPO_HERRAMIENTA,
                4,
                null,
                null,
                null,
                null,
                null,
                "PEN",
                BigDecimal.ONE,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("0.03"), // 3%
                null
        );

        BigDecimal costoTotalMO = new BigDecimal("8.48");
        BigDecimal costo = servicio.calcularCostoInsumo(
                insumo,
                new BigDecimal("25.0"),
                costoTotalMO,
                "PEN"
        );

        // Costo herramienta: 8.48 × 0.03 = 0.25
        assertEquals(new BigDecimal("0.25"), costo);
    }

    @Test
    void deberiaNormalizarPrecioAMonedaProyecto() {
        BigDecimal precio = new BigDecimal("850.00");
        BigDecimal tipoCambio = new BigDecimal("3.75");

        BigDecimal precioNormalizado = servicio.normalizarPrecioAMonedaProyecto(
                precio,
                "USD",
                tipoCambio,
                "PEN"
        );

        // 850 × 3.75 = 3187.50 (redondeado a 2 decimales)
        // El método normalizarPrecioAMonedaProyecto retorna con precisión intermedia (10 decimales)
        // pero el test compara con precisión final, así que comparamos con tolerancia
        assertTrue(precioNormalizado.compareTo(new BigDecimal("3187.49")) > 0 && 
                   precioNormalizado.compareTo(new BigDecimal("3187.51")) < 0);
    }

    @Test
    void deberiaCalcularCostoDiaCuadrilla() {
        List<ComposicionCuadrillaSnapshot> cuadrilla = List.of(
                new ComposicionCuadrillaSnapshot("PERS-001", "Capataz", new BigDecimal("0.1"), new BigDecimal("120.00"), "PEN"),
                new ComposicionCuadrillaSnapshot("PERS-002", "Operario", new BigDecimal("1.0"), new BigDecimal("80.00"), "PEN"),
                new ComposicionCuadrillaSnapshot("PERS-003", "Peón", new BigDecimal("2.0"), new BigDecimal("60.00"), "PEN")
        );

        BigDecimal costoDia = servicio.calcularCostoDiaCuadrilla(cuadrilla, "PEN");

        // (0.1 × 120) + (1.0 × 80) + (2.0 × 60) = 12 + 80 + 120 = 212
        // El método calcularCostoDiaCuadrilla retorna con precisión intermedia (10 decimales)
        assertTrue(costoDia.compareTo(new BigDecimal("211.99")) > 0 && 
                   costoDia.compareTo(new BigDecimal("212.01")) < 0);
    }
}
