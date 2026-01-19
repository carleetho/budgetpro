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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        // Agregar totales por partida usando Map para mejor type safety
        Map<PartidaId, PartidaData> partidasData = agregarTotalesPorPartida(compra);
        List<CompraDetalle> detallesConPartida = filtrarDetallesConPartida(compra);

        // Validar saldos disponibles sin mutar estado (atomicidad)
        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
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
        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
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
        
        // Reutilizar método helper para agregación
        Map<PartidaId, PartidaData> partidasData = agregarTotalesPorPartida(compra);

        for (Map.Entry<PartidaId, PartidaData> entry : partidasData.entrySet()) {
            Partida partida = entry.getValue().partida;
            BigDecimal totalPartida = entry.getValue().total;
            if (totalPartida.compareTo(partida.getSaldoDisponible()) > 0) {
                partidasSinSaldo.add(partida.getId());
            }
        }

        return partidasSinSaldo;
    }

    /**
     * Helper method que agrega totales por partida desde los detalles de compra.
     * 
     * Este método centraliza la lógica de agregación que se usa tanto en procesar()
     * como en validarSaldoPartidas(), evitando duplicación de código.
     * 
     * @param compra La compra de la cual extraer los detalles
     * @return Map con PartidaId como clave y PartidaData (partida + total agregado) como valor
     */
    private Map<PartidaId, PartidaData> agregarTotalesPorPartida(Compra compra) {
        Map<PartidaId, PartidaData> partidasData = new HashMap<>();

        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() == null) {
                continue; // Compra sin partida: no genera consumo presupuestal
            }
            
            PartidaId partidaId = PartidaId.from(detalle.getPartidaId());
            Partida partida = partidaRepository.findById(partidaId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Partida no encontrada: %s", detalle.getPartidaId())));
            
            partidasData.compute(partidaId, (key, existing) -> {
                if (existing == null) {
                    return new PartidaData(partida, detalle.getSubtotal());
                } else {
                    return new PartidaData(partida, existing.total.add(detalle.getSubtotal()));
                }
            });
        }

        return partidasData;
    }

    /**
     * Filtra los detalles de compra que tienen partida asignada.
     * 
     * @param compra La compra de la cual extraer los detalles
     * @return Lista de detalles que tienen partidaId no nulo
     */
    private List<CompraDetalle> filtrarDetallesConPartida(Compra compra) {
        List<CompraDetalle> detallesConPartida = new ArrayList<>();
        for (CompraDetalle detalle : compra.getDetalles()) {
            if (detalle.getPartidaId() != null) {
                detallesConPartida.add(detalle);
            }
        }
        return detallesConPartida;
    }

    /**
     * Clase interna para agrupar Partida y su total agregado.
     * Facilita el uso de Map en lugar de listas paralelas.
     */
    private static class PartidaData {
        final Partida partida;
        final BigDecimal total;

        PartidaData(Partida partida, BigDecimal total) {
            this.partida = partida;
            this.total = total;
        }
    }
}
