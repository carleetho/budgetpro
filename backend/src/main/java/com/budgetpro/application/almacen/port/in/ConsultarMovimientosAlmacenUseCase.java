package com.budgetpro.application.almacen.port.in;

import com.budgetpro.application.almacen.dto.MovimientoAlmacenResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultarMovimientosAlmacenUseCase {
    List<MovimientoAlmacenResponse> listar(UUID almacenId, UUID recursoId);
}

