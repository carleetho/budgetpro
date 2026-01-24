package com.budgetpro.domain.logistica.inventario.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del agregado MovimientoInventario.
 */
class MovimientoInventarioTest {

        @Test
        void crearSalidaConRequisicion_conDatosValidos_debeCrearMovimientoConReferencias() {
                UUID inventarioItemId = UUID.randomUUID();
                UUID requisicionId = UUID.randomUUID();
                UUID requisicionItemId = UUID.randomUUID();
                UUID partidaId = UUID.randomUUID();
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();

                MovimientoInventario movimiento = MovimientoInventario.crearSalidaConRequisicion(movimientoId,
                                inventarioItemId, new BigDecimal("50"), new BigDecimal("25.50"), requisicionId,
                                requisicionItemId, partidaId, "Despacho según requisición REQ-001");

                assertNotNull(movimiento);
                assertEquals(movimientoId, movimiento.getId());
                assertEquals(inventarioItemId, movimiento.getInventarioItemId());
                assertEquals(TipoMovimientoInventario.SALIDA_CONSUMO, movimiento.getTipo());
                assertEquals(new BigDecimal("50"), movimiento.getCantidad());
                assertEquals(new BigDecimal("25.50"), movimiento.getCostoUnitario());
                assertEquals(new BigDecimal("1275.00"), movimiento.getCostoTotal());
                assertEquals(requisicionId, movimiento.getRequisicionId());
                assertEquals(requisicionItemId, movimiento.getRequisicionItemId());
                assertEquals(partidaId, movimiento.getPartidaId());
                assertTrue(movimiento.esSalida());
        }

        @Test
        void crearAjuste_sinJustificacion_debeLanzarExcepcion() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();

                assertThrows(IllegalArgumentException.class,
                                () -> MovimientoInventario.crearAjuste(movimientoId, inventarioItemId,
                                                new BigDecimal("10"), new BigDecimal("15.00"), null,
                                                "Ajuste de inventario"));
        }

        @Test
        void crearAjuste_justificacionCorta_debeLanzarExcepcion() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();

                assertThrows(IllegalArgumentException.class,
                                () -> MovimientoInventario.crearAjuste(movimientoId, inventarioItemId,
                                                new BigDecimal("10"), new BigDecimal("15.00"), "Corta", // Menos de 20
                                                                                                        // caracteres
                                                "Ajuste de inventario"));
        }

        @Test
        void crearAjuste_conJustificacionValida_debeCrearMovimiento() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();

                MovimientoInventario movimiento = MovimientoInventario.crearAjuste(movimientoId, inventarioItemId,
                                new BigDecimal("10"), new BigDecimal("15.00"),
                                "Esta es una justificación detallada que tiene más de 20 caracteres para explicar el ajuste",
                                "Ajuste de inventario");

                assertNotNull(movimiento);
                assertEquals(TipoMovimientoInventario.AJUSTE, movimiento.getTipo());
                assertNotNull(movimiento.getJustificacion());
                assertTrue(movimiento.getJustificacion().length() >= 20);
        }

        @Test
        void crearSalidaTransferencia_conTransferenciaId_debeCrearMovimiento() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();
                UUID transferenciaId = UUID.randomUUID();

                MovimientoInventario movimiento = MovimientoInventario.crearSalidaTransferencia(movimientoId,
                                inventarioItemId, new BigDecimal("30"), new BigDecimal("20.00"), transferenciaId,
                                "Transferencia a bodega B");

                assertNotNull(movimiento);
                assertEquals(TipoMovimientoInventario.SALIDA_TRANSFERENCIA, movimiento.getTipo());
                assertEquals(transferenciaId, movimiento.getTransferenciaId());
                assertTrue(movimiento.esSalida());
        }

        @Test
        void crearEntradaTransferencia_conTransferenciaId_debeCrearMovimiento() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();
                UUID transferenciaId = UUID.randomUUID();

                MovimientoInventario movimiento = MovimientoInventario.crearEntradaTransferencia(movimientoId,
                                inventarioItemId, new BigDecimal("30"), new BigDecimal("20.00"), transferenciaId,
                                "Transferencia desde bodega A");

                assertNotNull(movimiento);
                assertEquals(TipoMovimientoInventario.ENTRADA_TRANSFERENCIA, movimiento.getTipo());
                assertEquals(transferenciaId, movimiento.getTransferenciaId());
                assertTrue(movimiento.esEntrada());
        }

        @Test
        void crearSalidaPrestamo_debeCrearMovimiento() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();

                MovimientoInventario movimiento = MovimientoInventario.crearSalidaPrestamo(movimientoId,
                                inventarioItemId, new BigDecimal("15"), new BigDecimal("18.50"), UUID.randomUUID(),
                                "Préstamo temporal a otro proyecto");

                assertNotNull(movimiento);
                assertEquals(TipoMovimientoInventario.SALIDA_PRESTAMO, movimiento.getTipo());
                assertTrue(movimiento.esSalida());
        }

        @Test
        void crearEntradaPrestamo_debeCrearMovimiento() {
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();
                UUID inventarioItemId = UUID.randomUUID();

                MovimientoInventario movimiento = MovimientoInventario.crearEntradaPrestamo(movimientoId,
                                inventarioItemId, new BigDecimal("15"), new BigDecimal("18.50"), UUID.randomUUID(),
                                "Devolución de préstamo");

                assertNotNull(movimiento);
                assertEquals(TipoMovimientoInventario.ENTRADA_PRESTAMO, movimiento.getTipo());
                assertTrue(movimiento.esEntrada());
        }

        @Test
        void esEntrada_paraTodosLosTiposEntrada_debeRetornarTrue() {
                UUID inventarioItemId = UUID.randomUUID();
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();

                MovimientoInventario entradaCompra = MovimientoInventario.crearEntradaPorCompra(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), UUID.randomUUID(),
                                "Compra");
                assertTrue(entradaCompra.esEntrada());

                MovimientoInventario entradaTransferencia = MovimientoInventario.crearEntradaTransferencia(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), UUID.randomUUID(),
                                "Transferencia");
                assertTrue(entradaTransferencia.esEntrada());

                MovimientoInventario entradaPrestamo = MovimientoInventario.crearEntradaPrestamo(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), UUID.randomUUID(),
                                "Préstamo");
                assertTrue(entradaPrestamo.esEntrada());

                MovimientoInventario ajuste = MovimientoInventario.crearAjuste(movimientoId, inventarioItemId,
                                new BigDecimal("10"), new BigDecimal("5.00"),
                                "Esta es una justificación detallada que tiene más de 20 caracteres", "Ajuste");
                assertTrue(ajuste.esEntrada());
        }

        @Test
        void esSalida_paraTodosLosTiposSalida_debeRetornarTrue() {
                UUID inventarioItemId = UUID.randomUUID();
                MovimientoInventarioId movimientoId = MovimientoInventarioId.generate();

                MovimientoInventario salidaConsumo = MovimientoInventario.crearSalidaPorConsumo(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), "Consumo");
                assertTrue(salidaConsumo.esSalida());

                MovimientoInventario salidaTransferencia = MovimientoInventario.crearSalidaTransferencia(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), UUID.randomUUID(),
                                "Transferencia");
                assertTrue(salidaTransferencia.esSalida());

                MovimientoInventario salidaPrestamo = MovimientoInventario.crearSalidaPrestamo(movimientoId,
                                inventarioItemId, new BigDecimal("10"), new BigDecimal("5.00"), UUID.randomUUID(),
                                "Préstamo");
                assertTrue(salidaPrestamo.esSalida());
        }
}
