package com.budgetpro.infrastructure.persistence.adapter.sobrecosto;

import com.budgetpro.application.rrhh.port.out.ConfiguracionLaboralRepositoryPort;
import com.budgetpro.domain.finanzas.sobrecosto.port.out.LaborFsRReaderPort;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class LaborFsRReaderRrhhAdapter implements LaborFsRReaderPort {

    private final ConfiguracionLaboralRepositoryPort configuracionLaboralRepositoryPort;

    public LaborFsRReaderRrhhAdapter(ConfiguracionLaboralRepositoryPort configuracionLaboralRepositoryPort) {
        this.configuracionLaboralRepositoryPort = configuracionLaboralRepositoryPort;
    }

    @Override
    public Optional<BigDecimal> findFsRForProyecto(UUID proyectoId) {
        return configuracionLaboralRepositoryPort.findActiveByProyecto(ProyectoId.from(proyectoId.toString()))
                .map(c -> c.calcularFSRExtendido());
    }

    @Override
    public Optional<BigDecimal> findFsRGlobal() {
        return configuracionLaboralRepositoryPort.findGlobalActive().map(c -> c.calcularFSRExtendido());
    }
}
