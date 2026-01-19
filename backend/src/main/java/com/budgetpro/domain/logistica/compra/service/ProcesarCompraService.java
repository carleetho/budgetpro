package com.budgetpro.domain.logistica.compra.service;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;
import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Dominio para procesar compras.
 * 
 * Orquesta la transacción completa:
 * - Valida que las Partidas existan
 * - Valida si las Partidas tienen saldo suficiente
 * - Genera los registros de ConsumoPartida
 * - Descuenta de la Billetera
 * - Registra entrada en Inventario (Kardex físico)
 * 
 * No persiste, solo orquesta la lógica de dominio.
 */
public class ProcesarCompraService {

    private final PartidaRepository partidaRepository;
    private final GestionInventarioService gestionInventarioService;

    public ProcesarCompraService(PartidaRepository partidaRepository,
                                GestionInventarioService gestionInventarioService) {
        this.partidaRepository = partidaRepository;
        this.gestionInventarioService = gestionInventarioService;
    }

    /**
     * Procesa una compra y genera los consumos presupuestales correspondientes.
     * 
     * @param compra La compra a procesar
     * @param billetera La billetera del proyecto
     * @return Lista de consumos generados
     * @throws IllegalArgumentException si alguna partida no existe
     * @throws com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException si la billetera no tiene saldo suficiente
     */
    public List<ConsumoPartida> procesar(Compra compra, Billetera billetera) {
        List<ConsumoPartida> consumos = new ArrayList<>();
        
        List<CompraDetalle> detallesConPartida = new ArrayList<>();
        List<PartidaId> partidasIds = new ArrayList<>();
        List<Partida> partidasCargadas = new ArrayList<>();
        List<java.math.BigDecimal> totalesPorPartida = new ArrayList<>();

        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() == null) {
                continue; // Compra sin partida: no genera consumo presupuestal
            }
            PartidaId partidaId = PartidaId.from(detalle.getPartidaId());
            Partida partida = partidaRepository.findById(partidaId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Partida no encontrada: %s", detalle.getPartidaId())));
            detallesConPartida.add(detalle);
            int index = partidasIds.indexOf(partida.getId());
            if (index < 0) {
                partidasIds.add(partida.getId());
                partidasCargadas.add(partida);
                totalesPorPartida.add(detalle.getSubtotal());
            } else {
                totalesPorPartida.set(index, totalesPorPartida.get(index).add(detalle.getSubtotal()));
            }
        }

        // Validar saldos disponibles sin mutar estado (atomicidad)
        for (int i = 0; i < partidasCargadas.size(); i++) {
            Partida partida = partidasCargadas.get(i);
            java.math.BigDecimal totalPartida = totalesPorPartida.get(i);
            if (totalPartida.compareTo(partida.getSaldoDisponible()) > 0) {
                throw new SaldoInsuficienteException(
                        compra.getProyectoId(),
                        partida.getSaldoDisponible(),
                        totalPartida,
                        String.format("Partida %s", partida.getId().getValue())
                );
            }
        }

        // Reservar saldo y persistir partidas
        for (int i = 0; i < partidasCargadas.size(); i++) {
            Partida partida = partidasCargadas.get(i);
            java.math.BigDecimal totalPartida = totalesPorPartida.get(i);
            partida.reservarSaldo(totalPartida);
            partidaRepository.save(partida);
        }

        // Crear consumos por detalle
        for (CompraDetalle detalle : detallesConPartida) {
            ConsumoPartidaId consumoId = ConsumoPartidaId.nuevo();
            ConsumoPartida consumo = ConsumoPartida.crearPorCompra(
                    consumoId,
                    detalle.getPartidaId(),
                    detalle.getId().getValue(),
                    detalle.getSubtotal(),
                    compra.getFecha()
            );
            consumos.add(consumo);
        }
        
        // Descontar de la billetera
        // Usar el método egresar que valida saldo y crea el movimiento
        billetera.egresar(
                compra.getTotal(),
                String.format("Compra #%s - %s", compra.getId().getValue(), compra.getProveedor()),
                null // evidenciaUrl opcional
        );
        
        // Aprobar la compra
        compra.aprobar();
        
        // CRÍTICO: Registrar entrada en Inventario (Kardex físico)
        // Esto actualiza el stock físico y crea movimientos de inventario
        gestionInventarioService.registrarEntradaPorCompra(compra);
        
        return consumos;
    }

    /**
     * Valida si todas las partidas tienen saldo suficiente.
     * 
     * En MVP, esto solo alerta, no bloquea.
     * 
     * @param compra La compra a validar
     * @return Lista de partidas con saldo insuficiente (vacía si todas tienen saldo)
     */
    public List<PartidaId> validarSaldoPartidas(Compra compra) {
        List<PartidaId> partidasSinSaldo = new ArrayList<>();
        
        List<PartidaId> partidasIds = new ArrayList<>();
        List<Partida> partidasCargadas = new ArrayList<>();
        List<java.math.BigDecimal> totalesPorPartida = new ArrayList<>();

        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() == null) {
                continue;
            }
            PartidaId partidaId = PartidaId.from(detalle.getPartidaId());
            Partida partida = partidaRepository.findById(partidaId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Partida no encontrada: %s", detalle.getPartidaId())));
            int index = partidasIds.indexOf(partida.getId());
            if (index < 0) {
                partidasIds.add(partida.getId());
                partidasCargadas.add(partida);
                totalesPorPartida.add(detalle.getSubtotal());
            } else {
                totalesPorPartida.set(index, totalesPorPartida.get(index).add(detalle.getSubtotal()));
            }
        }

        for (int i = 0; i < partidasCargadas.size(); i++) {
            Partida partida = partidasCargadas.get(i);
            if (totalesPorPartida.get(i).compareTo(partida.getSaldoDisponible()) > 0) {
                partidasSinSaldo.add(partida.getId());
            }
        }

        return partidasSinSaldo;
    }
}
