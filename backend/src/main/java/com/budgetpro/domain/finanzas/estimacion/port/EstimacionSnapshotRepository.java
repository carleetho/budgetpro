package com.budgetpro.domain.finanzas.estimacion.port;

import com.budgetpro.domain.finanzas.estimacion.model.EstimacionSnapshot;
import com.budgetpro.domain.finanzas.estimacion.model.EstimacionId;
import java.util.Optional;

public interface EstimacionSnapshotRepository {

    EstimacionSnapshot save(EstimacionSnapshot snapshot);

    Optional<EstimacionSnapshot> findByEstimacionId(EstimacionId estimacionId);
}
