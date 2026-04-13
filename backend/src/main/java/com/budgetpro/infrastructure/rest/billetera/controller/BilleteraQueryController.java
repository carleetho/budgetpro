package com.budgetpro.infrastructure.rest.billetera.controller;

import com.budgetpro.infrastructure.persistence.entity.billetera.BilleteraEntity;
import com.budgetpro.infrastructure.persistence.entity.billetera.MovimientoCajaEntity;
import com.budgetpro.infrastructure.persistence.repository.billetera.BilleteraJpaRepository;
import com.budgetpro.infrastructure.rest.billetera.dto.BilleteraSaldoResponse;
import com.budgetpro.infrastructure.rest.billetera.dto.MovimientoCajaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billeteras")
public class BilleteraQueryController {

    private final BilleteraJpaRepository billeteraJpaRepository;

    public BilleteraQueryController(BilleteraJpaRepository billeteraJpaRepository) {
        this.billeteraJpaRepository = billeteraJpaRepository;
    }

    @GetMapping("/{billeteraId}/saldo")
    public ResponseEntity<BilleteraSaldoResponse> obtenerSaldo(@PathVariable UUID billeteraId) {
        BilleteraEntity billetera = billeteraJpaRepository.findById(billeteraId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Billetera no encontrada."));
        return ResponseEntity.ok(new BilleteraSaldoResponse(
                billetera.getId(),
                billetera.getProyectoId(),
                billetera.getMoneda(),
                billetera.getSaldoActual()
        ));
    }

    @GetMapping("/{billeteraId}/movimientos")
    public ResponseEntity<List<MovimientoCajaResponse>> listarMovimientos(@PathVariable UUID billeteraId) {
        BilleteraEntity billetera = billeteraJpaRepository.findWithMovimientosById(billeteraId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Billetera no encontrada."));

        List<MovimientoCajaResponse> movimientos = billetera.getMovimientos().stream()
                .sorted(Comparator.comparing(MovimientoCajaEntity::getFecha).reversed())
                .map(m -> new MovimientoCajaResponse(
                        m.getId(),
                        m.getMonto(),
                        m.getMoneda(),
                        m.getTipo(),
                        m.getFecha(),
                        m.getReferencia(),
                        m.getEvidenciaUrl(),
                        m.getEstado() != null ? m.getEstado().name() : null
                ))
                .toList();

        return ResponseEntity.ok(movimientos);
    }
}

