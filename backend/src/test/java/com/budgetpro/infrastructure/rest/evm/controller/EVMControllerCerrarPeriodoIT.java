package com.budgetpro.infrastructure.rest.evm.controller;

import com.budgetpro.domain.finanzas.proyecto.model.FrecuenciaControl;
import com.budgetpro.domain.proyecto.model.EstadoProyecto;
import com.budgetpro.domain.proyecto.model.Proyecto;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.domain.proyecto.port.out.ProyectoRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para POST /api/v1/evm/{proyectoId}/cerrar-periodo (REQ-64, AC-E04-INV).
 */
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class EVMControllerCerrarPeriodoIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        if (objectMapper != null) {
            objectMapper.findAndRegisterModules();
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
    }

    @Nested
    @DisplayName("AC-E04-INV-01: fecha alineada SEMANAL")
    class FechaAlineada {

        @Test
        @DisplayName("retorna HTTP 200 con proyectoId, periodoId, fechaCorte, status CERRADO")
        void retorna200ConCuerpoCorrecto() throws Exception {
            ProyectoId proyectoId = ProyectoId.nuevo();
            LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);
            LocalDate fechaCorte = LocalDate.of(2025, 1, 13);
            Proyecto proyecto = Proyecto.reconstruir(
                    proyectoId, "Proyecto SEMANAL", "Loc", EstadoProyecto.ACTIVO,
                    fechaInicio, FrecuenciaControl.SEMANAL);
            proyectoRepository.save(proyecto);

            String requestBody = "{\"fechaCorte\": \"2025-01-13\"}";

            mockMvc.perform(post("/api/v1/evm/{proyectoId}/cerrar-periodo", proyectoId.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.proyectoId").value(proyectoId.getValue().toString()))
                    .andExpect(jsonPath("$.periodoId").value("PER-2025-01-13"))
                    .andExpect(jsonPath("$.fechaCorte").value("2025-01-13"))
                    .andExpect(jsonPath("$.status").value("CERRADO"));
        }
    }

    @Nested
    @DisplayName("AC-E04-INV-02: fecha desalineada SEMANAL")
    class FechaDesalineada {

        @Test
        @DisplayName("retorna HTTP 422 con error conteniendo nombre de frecuencia")
        void retorna422ConError() throws Exception {
            ProyectoId proyectoId = ProyectoId.nuevo();
            LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 6, 0, 0);
            Proyecto proyecto = Proyecto.reconstruir(
                    proyectoId, "Proyecto SEMANAL", "Loc", EstadoProyecto.ACTIVO,
                    fechaInicio, FrecuenciaControl.SEMANAL);
            proyectoRepository.save(proyecto);

            String requestBody = "{\"fechaCorte\": \"2025-01-10\"}";

            mockMvc.perform(post("/api/v1/evm/{proyectoId}/cerrar-periodo", proyectoId.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error", containsString("SEMANAL")))
                    .andExpect(jsonPath("$.fechaCorte").value("2025-01-10"));
        }
    }

    @Nested
    @DisplayName("AC-E04-INV-03: frecuenciaControl null")
    class SinFrecuencia {

        @Test
        @DisplayName("retorna HTTP 200 sin validación de frecuencia")
        void retorna200SinValidacion() throws Exception {
            ProyectoId proyectoId = ProyectoId.nuevo();
            Proyecto proyecto = Proyecto.reconstruir(
                    proyectoId, "Proyecto Sin Frecuencia", "Loc", EstadoProyecto.ACTIVO,
                    null, null);
            proyectoRepository.save(proyecto);

            String requestBody = "{\"fechaCorte\": \"2025-06-15\"}";

            mockMvc.perform(post("/api/v1/evm/{proyectoId}/cerrar-periodo", proyectoId.getValue())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.proyectoId").value(proyectoId.getValue().toString()))
                    .andExpect(jsonPath("$.periodoId").value("PER-2025-06-15"))
                    .andExpect(jsonPath("$.fechaCorte").value("2025-06-15"))
                    .andExpect(jsonPath("$.status").value("CERRADO"));
        }
    }

    @Nested
    @DisplayName("proyecto inexistente")
    class ProyectoInexistente {

        @Test
        @DisplayName("retorna HTTP 404 con error Proyecto no encontrado")
        void retorna404() throws Exception {
            UUID unknownId = UUID.randomUUID();
            String requestBody = "{\"fechaCorte\": \"2025-01-13\"}";

            mockMvc.perform(post("/api/v1/evm/{proyectoId}/cerrar-periodo", unknownId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Proyecto no encontrado"))
                    .andExpect(jsonPath("$.proyectoId").value(unknownId.toString()));
        }
    }
}
