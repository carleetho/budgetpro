package com.budgetpro.infrastructure.rest.alertas;

import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
class AlertasEndToEndIT extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    PartidaJpaRepository partidaJpaRepository;

    @Test
    void debeAnalizarPresupuesto_yRetornarResponse() throws Exception {
        UUID proyectoId = UUID.randomUUID();
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("Proyecto Alertas IT " + proyectoId.toString().substring(0, 8));
        proyecto.setEstado(EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(BigDecimal.valueOf(1000));
        proyecto.setVersion(0);
        proyectoJpaRepository.save(proyecto);

        UUID presupuestoId = UUID.randomUUID();
        PresupuestoEntity presupuesto = new PresupuestoEntity();
        presupuesto.setId(presupuestoId);
        presupuesto.setProyectoId(proyectoId);
        presupuesto.setProyecto(proyecto);
        presupuesto.setNombre("Presupuesto IT");
        presupuesto.setEstado(EstadoPresupuesto.BORRADOR);
        presupuesto.setEsLineaBase(false);
        presupuesto.setEsContractual(false);
        presupuesto.setVersion(0);
        presupuestoJpaRepository.save(presupuesto);

        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.randomUUID());
        partida.setPresupuesto(presupuesto);
        partida.setCodigo("1.1");
        partida.setItem("1.1");
        partida.setDescripcion("Partida IT");
        partida.setUnidad("m3");
        partida.setMetradoOriginal(BigDecimal.ONE);
        partida.setMetradoVigente(BigDecimal.ONE);
        partida.setPrecioUnitario(BigDecimal.TEN);
        partida.setGastosReales(BigDecimal.ZERO);
        partida.setCompromisosPendientes(BigDecimal.ZERO);
        partida.setNivel(1);
        partida.setVersion(0);
        partidaJpaRepository.save(partida);

        mockMvc.perform(get("/api/v1/analisis/alertas/{presupuestoId}", presupuestoId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presupuestoId").value(presupuestoId.toString()));
    }
}

