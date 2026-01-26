package com.budgetpro.infrastructure.persistence.mapper.evm;

import com.budgetpro.domain.finanzas.evm.model.EVMSnapshot;
import com.budgetpro.domain.finanzas.evm.model.EVMSnapshotId;
import com.budgetpro.infrastructure.persistence.entity.evm.EVMSnapshotEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre EVMSnapshot (dominio) y EVMSnapshotEntity
 * (persistencia).
 */
@Component
public class EVMSnapshotMapper {

    public EVMSnapshotEntity toEntity(EVMSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        return EVMSnapshotEntity.builder().id(snapshot.getId().getValue()).proyectoId(snapshot.getProyectoId())
                .fechaCorte(snapshot.getFechaCorte()).fechaCalculo(snapshot.getFechaCalculo()).pv(snapshot.getPv())
                .ev(snapshot.getEv()).ac(snapshot.getAc()).bac(snapshot.getBac()).cv(snapshot.getCv())
                .sv(snapshot.getSv()).cpi(snapshot.getCpi()).spi(snapshot.getSpi()).eac(snapshot.getEac())
                .etc(snapshot.getEtc()).vac(snapshot.getVac()).interpretacion(snapshot.getInterpretacion()).build();
    }

    public EVMSnapshot toDomain(EVMSnapshotEntity entity) {
        if (entity == null) {
            return null;
        }

        return EVMSnapshot.reconstruir(EVMSnapshotId.from(entity.getId()), entity.getProyectoId(),
                entity.getFechaCorte(), entity.getFechaCalculo(), entity.getPv(), entity.getEv(), entity.getAc(),
                entity.getBac(), entity.getCv(), entity.getSv(), entity.getCpi(), entity.getSpi(), entity.getEac(),
                entity.getEtc(), entity.getVac(), entity.getInterpretacion());
    }
}
