package com.budgetpro.domain.finanzas.estimacion.exception;

import java.util.UUID;

public class AvanceAcumuladoExcedidoException extends RuntimeException {

    public AvanceAcumuladoExcedidoException(UUID partidaId, String totalAvance) {
        super(String.format("El avance acumulado para la partida %s excede el 100%%. Total calculado: %s", partidaId,
                totalAvance));
    }
}
