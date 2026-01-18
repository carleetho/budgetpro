package com.budgetpro.application.almacen.usecase;

import com.budgetpro.application.almacen.dto.MovimientoAlmacenResponse;
import com.budgetpro.application.almacen.port.in.RegistrarMovimientoAlmacenUseCase;
import com.budgetpro.domain.logistica.almacen.model.*;
import com.budgetpro.domain.logistica.almacen.port.out.AlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import com.budgetpro.domain.logistica.almacen.port.out.RegistroKardexRepository;
import com.budgetpro.domain.logistica.almacen.service.GestionKardexService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Implementación del caso de uso para registrar movimientos de almacén.
 */
@Service
public class RegistrarMovimientoAlmacenUseCaseImpl implements RegistrarMovimientoAlmacenUseCase {
    
    private final GestionKardexService gestionKardexService;
    private final MovimientoAlmacenRepository movimientoRepository;
    private final RegistroKardexRepository kardexRepository;
    private final AlmacenRepository almacenRepository;
    
    public RegistrarMovimientoAlmacenUseCaseImpl(
            GestionKardexService gestionKardexService,
            MovimientoAlmacenRepository movimientoRepository,
            RegistroKardexRepository kardexRepository,
            AlmacenRepository almacenRepository) {
        this.gestionKardexService = gestionKardexService;
        this.movimientoRepository = movimientoRepository;
        this.kardexRepository = kardexRepository;
        this.almacenRepository = almacenRepository;
    }
    
    @Override
    @Transactional
    public MovimientoAlmacenResponse registrar(UUID almacenId, UUID recursoId, String tipoMovimiento,
                                              LocalDate fechaMovimiento, BigDecimal cantidad,
                                              BigDecimal precioUnitario, String numeroDocumento,
                                              UUID partidaId, UUID centroCostoId, String observaciones) {
        // Validar que el almacén existe
        Almacen almacen = almacenRepository.buscarPorId(AlmacenId.of(almacenId))
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado: " + almacenId));
        
        if (!almacen.isActivo()) {
            throw new IllegalStateException("El almacén no está activo");
        }
        
        // Crear movimiento
        MovimientoAlmacenId movimientoId = MovimientoAlmacenId.generate();
        MovimientoAlmacen movimiento;
        
        TipoMovimientoAlmacen tipo = TipoMovimientoAlmacen.valueOf(tipoMovimiento);
        
        if (tipo == TipoMovimientoAlmacen.ENTRADA) {
            movimiento = MovimientoAlmacen.crearEntrada(
                movimientoId,
                AlmacenId.of(almacenId),
                recursoId,
                fechaMovimiento,
                cantidad,
                precioUnitario,
                numeroDocumento,
                observaciones
            );
        } else {
            movimiento = MovimientoAlmacen.crearSalida(
                movimientoId,
                AlmacenId.of(almacenId),
                recursoId,
                fechaMovimiento,
                cantidad,
                precioUnitario,
                partidaId,
                centroCostoId,
                numeroDocumento,
                observaciones
            );
        }
        
        // Obtener último registro de Kárdex
        RegistroKardex ultimoRegistro = kardexRepository.buscarUltimoPorAlmacenIdYRecursoId(almacenId, recursoId)
                .orElse(null);
        
        BigDecimal saldoCantidadAnterior = ultimoRegistro != null ? ultimoRegistro.getSaldoCantidad() : BigDecimal.ZERO;
        BigDecimal saldoValorAnterior = ultimoRegistro != null ? ultimoRegistro.getSaldoValor() : BigDecimal.ZERO;
        BigDecimal cppAnterior = ultimoRegistro != null ? ultimoRegistro.getCostoPromedioPonderado() : BigDecimal.ZERO;
        
        // Procesar movimiento y generar registro de Kárdex
        RegistroKardex nuevoRegistro;
        
        if (tipo == TipoMovimientoAlmacen.ENTRADA) {
            nuevoRegistro = gestionKardexService.procesarEntrada(
                almacenId,
                recursoId,
                cantidad,
                precioUnitario,
                movimientoId.getValue(),
                saldoCantidadAnterior,
                saldoValorAnterior
            );
        } else {
            nuevoRegistro = gestionKardexService.procesarSalida(
                almacenId,
                recursoId,
                cantidad,
                movimientoId.getValue(),
                saldoCantidadAnterior,
                saldoValorAnterior,
                cppAnterior
            );
        }
        
        // Persistir movimiento y registro de Kárdex
        movimientoRepository.guardar(movimiento);
        kardexRepository.guardar(nuevoRegistro);
        
        // Mapear a DTO de respuesta
        return new MovimientoAlmacenResponse(
            movimiento.getId().getValue(),
            movimiento.getAlmacenId().getValue(),
            movimiento.getRecursoId(),
            movimiento.getTipoMovimiento().name(),
            movimiento.getFechaMovimiento(),
            movimiento.getCantidad(),
            movimiento.getPrecioUnitario(),
            movimiento.getImporteTotal(),
            movimiento.getNumeroDocumento(),
            movimiento.getPartidaId(),
            movimiento.getCentroCostoId(),
            movimiento.getObservaciones(),
            nuevoRegistro.getSaldoCantidad(),
            nuevoRegistro.getCostoPromedioPonderado()
        );
    }
}
