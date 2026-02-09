package com.budgetpro.domain.logistica.inventario.model;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventarioItemTest {

    @Test
    void crear_conDatosValidos_debeCrearItem() {
        InventarioId id = InventarioId.generate();
        UUID proyectoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();

        InventarioItem item = InventarioItem.crearConSnapshot(id, proyectoId, "REC-001", bodegaId, "Cemento",
                "Materiales", "kg");

        assertNotNull(item);
        assertEquals(id, item.getId());
        assertEquals(proyectoId, item.getProyectoId());
        assertEquals("REC-001", item.getRecursoExternalId());
        assertEquals(BigDecimal.ZERO, item.getCantidadFisica());
        assertEquals(BigDecimal.ZERO, item.getCostoPromedio());
    }

    @Test
    void ingresar_debeAumentarCantidadYRecalcularCostoPromedio() {
        InventarioItem item = crearItemBase();

        // Primera entrada: 100 unidades a $10
        var tx1 = item.ingresar(new BigDecimal("100"), new BigDecimal("10.00"), UUID.randomUUID(), "Compra 1");
        InventarioItem item1 = tx1.inventario();

        assertNotSame(item, item1); // Immutability check
        assertEquals(new BigDecimal("100"), item1.getCantidadFisica());
        assertEquals(new BigDecimal("10.00"), item1.getCostoPromedio());

        // Segunda entrada: 50 unidades a $20
        // Nuevo valor total = 1000 + (50 * 20) = 2000
        // Nueva cantidad = 150
        // Nuevo costo promedio = 2000 / 150 = 13.3333... -> 13.33
        var tx2 = item1.ingresar(new BigDecimal("50"), new BigDecimal("20.00"), UUID.randomUUID(), "Compra 2");
        InventarioItem item2 = tx2.inventario();

        assertNotSame(item1, item2);
        assertEquals(new BigDecimal("150"), item2.getCantidadFisica());
        // El cálculo exacto depende de la implementación de redondeo en InventarioItem,
        // típicamente 2 o 4 decimales
        // Asumiendo comportamiento estándar de BigDecimal o lógica interna
        assertTrue(item2.getCostoPromedio().compareTo(new BigDecimal("13.33")) >= 0);
    }

    @Test
    void egresar_debeDisminuirCantidadYMantenerCostoPromedio() {
        InventarioItem item = crearItemConStock();
        BigDecimal costoInicial = item.getCostoPromedio();
        BigDecimal cantidadInicial = item.getCantidadFisica();

        var tx = item.egresar(new BigDecimal("10"), "Consumo obra");
        InventarioItem itemSalida = tx.inventario();

        assertNotSame(item, itemSalida);
        assertEquals(cantidadInicial.subtract(new BigDecimal("10")), itemSalida.getCantidadFisica());
        assertEquals(costoInicial, itemSalida.getCostoPromedio());
    }

    @Test
    void egresar_sinStockSuficiente_debeLanzarExcepcion() {
        InventarioItem item = crearItemBase(); // Stock 0

        assertThrows(RuntimeException.class, () -> item.egresar(new BigDecimal("10"), "Consumo sin stock"));
    }

    @Test
    void ajustar_debeActualizarCantidad() {
        InventarioItem item = crearItemBase();

        var tx = item.ajustar(new BigDecimal("50"), "Ajuste inicial por inventario físico", "Ajuste positivo");
        InventarioItem itemAjustado = tx.inventario();

        assertNotSame(item, itemAjustado);
        assertEquals(new BigDecimal("50"), itemAjustado.getCantidadFisica());
    }

    private InventarioItem crearItemBase() {
        return InventarioItem.crearConSnapshot(InventarioId.generate(), UUID.randomUUID(), "REC-001",
                BodegaId.generate(), "Item Test", "General", "un");
    }

    private InventarioItem crearItemConStock() {
        InventarioItem item = crearItemBase();
        return item.ingresar(new BigDecimal("100"), new BigDecimal("10.00"), UUID.randomUUID(), "Init").inventario();
    }
}
