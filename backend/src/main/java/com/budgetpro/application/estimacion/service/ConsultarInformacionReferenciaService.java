package com.budgetpro.application.estimacion.service;

import com.budgetpro.application.estimacion.dto.ConsumoMaterialResponse;
import com.budgetpro.application.estimacion.dto.HorasLaborResponse;
import com.budgetpro.application.estimacion.port.out.InventarioIntegrationPort;
import com.budgetpro.application.estimacion.port.out.RRHHIntegrationPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ConsultarInformacionReferenciaService {

    private final InventarioIntegrationPort inventarioIntegrationPort;
    private final RRHHIntegrationPort rrhhIntegrationPort;

    public ConsultarInformacionReferenciaService(InventarioIntegrationPort inventarioIntegrationPort,
            RRHHIntegrationPort rrhhIntegrationPort) {
        this.inventarioIntegrationPort = inventarioIntegrationPort;
        this.rrhhIntegrationPort = rrhhIntegrationPort;
    }

    public InformacionReferencia consultar(UUID proyectoId, UUID partidaId, LocalDate inicio, LocalDate fin) {
        // Run in parallel ideally, but sequential is safer for now
        ConsumoMaterialResponse consumo = inventarioIntegrationPort.consultarConsumo(proyectoId, partidaId, inicio,
                fin);
        HorasLaborResponse horas = rrhhIntegrationPort.consultarHoras(proyectoId, partidaId, inicio, fin);

        return new InformacionReferencia(consumo, horas);
    }

    public static class InformacionReferencia {
        private final ConsumoMaterialResponse consumo;
        private final HorasLaborResponse horas;

        public InformacionReferencia(ConsumoMaterialResponse consumo, HorasLaborResponse horas) {
            this.consumo = consumo;
            this.horas = horas;
        }

        public ConsumoMaterialResponse getConsumo() {
            return consumo;
        }

        public HorasLaborResponse getHoras() {
            return horas;
        }
    }
}
