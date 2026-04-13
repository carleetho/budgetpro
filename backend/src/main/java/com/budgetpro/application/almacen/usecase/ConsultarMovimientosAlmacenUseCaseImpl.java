package com.budgetpro.application.almacen.usecase;

import com.budgetpro.application.almacen.dto.MovimientoAlmacenResponse;
import com.budgetpro.application.almacen.port.in.ConsultarMovimientosAlmacenUseCase;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacen;
import com.budgetpro.domain.logistica.almacen.port.out.MovimientoAlmacenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultarMovimientosAlmacenUseCaseImpl implements ConsultarMovimientosAlmacenUseCase {

    private final MovimientoAlmacenRepository movimientoAlmacenRepository;

    public ConsultarMovimientosAlmacenUseCaseImpl(MovimientoAlmacenRepository movimientoAlmacenRepository) {
        this.movimientoAlmacenRepository = movimientoAlmacenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoAlmacenResponse> listar(UUID almacenId, UUID recursoId) {
        if (almacenId == null) {
            throw new IllegalArgumentException("almacenId es obligatorio");
        }
        List<MovimientoAlmacen> movimientos = (recursoId == null)
                ? movimientoAlmacenRepository.buscarPorAlmacenId(almacenId)
                : movimientoAlmacenRepository.buscarPorAlmacenIdYRecursoId(almacenId, recursoId);

        return movimientos.stream().map(ConsultarMovimientosAlmacenUseCaseImpl::toResponse).toList();
    }

    private static MovimientoAlmacenResponse toResponse(MovimientoAlmacen mov) {
        return new MovimientoAlmacenResponse(
                mov.getId().getValue(),
                mov.getAlmacenId().getValue(),
                mov.getRecursoId(),
                mov.getTipoMovimiento().name(),
                mov.getFechaMovimiento(),
                mov.getCantidad(),
                mov.getPrecioUnitario(),
                mov.getImporteTotal(),
                mov.getNumeroDocumento(),
                mov.getPartidaId(),
                mov.getCentroCostoId(),
                mov.getObservaciones(),
                null,
                null
        );
    }
}

