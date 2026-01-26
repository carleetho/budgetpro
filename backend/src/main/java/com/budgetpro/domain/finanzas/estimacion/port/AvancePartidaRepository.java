package com.budgetpro.domain.finanzas.estimacion.port;

import com.budgetpro.domain.finanzas.estimacion.model.AvancePartida;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AvancePartidaRepository {

    AvancePartida save(AvancePartida avancePartida);

    List<AvancePartida> findByPartidaId(UUID partidaId);

    /**
     * Calcula el porcentaje acumulado total para una partida histórica. Retorna el
     * valor bruto (0-100 o más si hubo error) para validación.
     */
    BigDecimal calcularAvanceAcumulado(UUID partidaId);
}
