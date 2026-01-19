package com.budgetpro.domain.logistica.compra.service;

import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.compra.model.RelacionContractual;
import com.budgetpro.domain.logistica.compra.model.RubroInsumo;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcesarCompraServiceTest {

    @Mock
    private PartidaRepository partidaRepository;

    @Mock
    private GestionInventarioService gestionInventarioService;

    private ProcesarCompraService service;

    @BeforeEach
    void setUp() {
        service = new ProcesarCompraService(partidaRepository, gestionInventarioService);
    }

    @Test
    void procesar_conSaldoSuficiente_debeReservarYaprobar() {
        Partida partida = crearPartidaConSaldo(new BigDecimal("1000.00"));
        CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"), new BigDecimal("100.00"));
        Compra compra = crearCompra(List.of(detalle));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));

        when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                .thenReturn(Optional.of(partida));

        service.procesar(compra, billetera);

        assertEquals(new BigDecimal("200.00"), partida.getCompromisosPendientes());
        assertEquals(new BigDecimal("800.00"), partida.getSaldoDisponible());
        assertEquals(true, compra.isAprobada());
        verify(partidaRepository).save(partida);
        verify(gestionInventarioService).registrarEntradaPorCompra(compra);
    }

    @Test
    void procesar_conSaldoInsuficiente_debeLanzarExcepcion() {
        Partida partida = crearPartidaConSaldo(new BigDecimal("100.00"));
        CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"), new BigDecimal("100.00"));
        Compra compra = crearCompra(List.of(detalle));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));

        when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                .thenReturn(Optional.of(partida));

        assertThrows(SaldoInsuficienteException.class, () -> service.procesar(compra, billetera));
        assertEquals(BigDecimal.ZERO, partida.getCompromisosPendientes());
        assertFalse(compra.isAprobada());
        verify(partidaRepository, never()).save(any());
        verify(gestionInventarioService, never()).registrarEntradaPorCompra(any());
    }

    @Test
    void procesar_partidaNoEncontrada_debeLanzarExcepcion() {
        CompraDetalle detalle = crearDetalle(UUID.randomUUID(), new BigDecimal("1"), new BigDecimal("50.00"));
        Compra compra = crearCompra(List.of(detalle));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));

        when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.procesar(compra, billetera));
    }

    @Test
    void procesar_multipleDetalles_unSaldoInsuficiente_debeRechazarTodo() {
        Partida partidaOk = crearPartidaConSaldo(new BigDecimal("500.00"));
        Partida partidaBad = crearPartidaConSaldo(new BigDecimal("50.00"));

        CompraDetalle detalleOk = crearDetalle(partidaOk.getId().getValue(), new BigDecimal("1"), new BigDecimal("100.00"));
        CompraDetalle detalleBad = crearDetalle(partidaBad.getId().getValue(), new BigDecimal("1"), new BigDecimal("100.00"));
        Compra compra = crearCompra(List.of(detalleOk, detalleBad));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("1000.00"));

        when(partidaRepository.findById(PartidaId.from(detalleOk.getPartidaId())))
                .thenReturn(Optional.of(partidaOk));
        when(partidaRepository.findById(PartidaId.from(detalleBad.getPartidaId())))
                .thenReturn(Optional.of(partidaBad));

        assertThrows(SaldoInsuficienteException.class, () -> service.procesar(compra, billetera));
        assertEquals(BigDecimal.ZERO, partidaOk.getCompromisosPendientes());
        assertEquals(BigDecimal.ZERO, partidaBad.getCompromisosPendientes());
        verify(partidaRepository, never()).save(any());
    }

    private Partida crearPartidaConSaldo(BigDecimal presupuestoAsignado) {
        Partida partida = Partida.crearRaiz(
                PartidaId.nuevo(),
                UUID.randomUUID(),
                "01.01",
                "Partida test",
                "UND",
                BigDecimal.ONE
        );
        partida.actualizarPresupuestoAsignado(presupuestoAsignado);
        partida.actualizarGastosReales(BigDecimal.ZERO);
        partida.actualizarCompromisosPendientes(BigDecimal.ZERO);
        return partida;
    }

    private CompraDetalle crearDetalle(UUID partidaId, BigDecimal cantidad, BigDecimal precioUnitario) {
        return CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                "MAT-001", // recursoExternalId
                "Cemento Portland", // recursoNombre
                partidaId,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                cantidad,
                precioUnitario
        );
    }

    private Compra crearCompra(List<CompraDetalle> detalles) {
        return Compra.crear(
                CompraId.nuevo(),
                UUID.randomUUID(),
                LocalDate.now(),
                "Proveedor test",
                detalles
        );
    }

    private Billetera crearBilleteraConSaldo(BigDecimal saldo) {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), UUID.randomUUID());
        billetera.ingresar(saldo, "Ingreso test", "http://evidencia/ok");
        return billetera;
    }
}
