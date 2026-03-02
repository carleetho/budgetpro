package com.budgetpro.infrastructure.rest.evm.controller;

import com.budgetpro.application.evm.service.EVMCalculationService;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerSCurveUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.application.finanzas.evm.port.in.SCurveResult;
import com.budgetpro.infrastructure.rest.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EVMControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EVMCalculationService evmCalculationService;

    @Mock
    private ObtenerSCurveUseCase obtenerSCurveUseCase;

    @BeforeEach
    void setUp() {
        EVMController controller = new EVMController(evmCalculationService, obtenerSCurveUseCase);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/evm/{proyectoId}/s-curve retorna 200 con dataPoints ordenados")
    void deberiaRetornarSCurveConDatos() throws Exception {
        UUID proyectoId = UUID.randomUUID();
        SCurveResult result = new SCurveResult(
                proyectoId,
                "USD",
                new BigDecimal("500000.00"),
                new BigDecimal("520000.00"),
                List.of(
                        new SCurveResult.SCurveDataPoint(
                                LocalDate.of(2025, 1, 31), 1,
                                new BigDecimal("100000.00"), new BigDecimal("95000.00"), new BigDecimal("98000.00"),
                                new BigDecimal("0.9694"), new BigDecimal("0.9500")),
                        new SCurveResult.SCurveDataPoint(
                                LocalDate.of(2025, 2, 28), 2,
                                new BigDecimal("200000.00"), new BigDecimal("190000.00"), new BigDecimal("198000.00"),
                                new BigDecimal("0.9596"), new BigDecimal("0.9500")),
                        new SCurveResult.SCurveDataPoint(
                                LocalDate.of(2025, 3, 31), 3,
                                new BigDecimal("300000.00"), new BigDecimal("285000.00"), new BigDecimal("296000.00"),
                                new BigDecimal("0.9628"), new BigDecimal("0.9500"))));
        when(obtenerSCurveUseCase.obtener(eq(proyectoId), isNull(), isNull())).thenReturn(result);

        mockMvc.perform(get("/api/v1/evm/{proyectoId}/s-curve", proyectoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()))
                .andExpect(jsonPath("$.moneda").value("USD"))
                .andExpect(jsonPath("$.dataPoints.length()").value(3))
                .andExpect(jsonPath("$.dataPoints[0].fechaCorte").value("2025-01-31"))
                .andExpect(jsonPath("$.dataPoints[1].fechaCorte").value("2025-02-28"))
                .andExpect(jsonPath("$.dataPoints[2].fechaCorte").value("2025-03-31"));
    }

    @Test
    @DisplayName("GET /api/v1/evm/{proyectoId}/s-curve retorna 200 con estado vacío")
    void deberiaRetornarEstadoVacio() throws Exception {
        UUID proyectoId = UUID.randomUUID();
        SCurveResult result = new SCurveResult(
                proyectoId,
                null,
                new BigDecimal("500000.00"),
                new BigDecimal("520000.00"),
                List.of());
        when(obtenerSCurveUseCase.obtener(eq(proyectoId), isNull(), isNull())).thenReturn(result);

        mockMvc.perform(get("/api/v1/evm/{proyectoId}/s-curve", proyectoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/evm/{proyectoId}/s-curve retorna 404 cuando proyecto no existe")
    void deberiaRetornar404SiProyectoNoExiste() throws Exception {
        UUID proyectoId = UUID.randomUUID();
        when(obtenerSCurveUseCase.obtener(eq(proyectoId), isNull(), isNull()))
                .thenThrow(new ProyectoNotFoundException(proyectoId));

        mockMvc.perform(get("/api/v1/evm/{proyectoId}/s-curve", proyectoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Proyecto no encontrado"))
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/evm/{proyectoId}/s-curve parsea startDate/endDate y filtra rango")
    void deberiaAceptarParametrosDeFechaOpcionales() throws Exception {
        UUID proyectoId = UUID.randomUUID();
        LocalDate startDate = LocalDate.of(2025, 3, 1);
        LocalDate endDate = LocalDate.of(2025, 4, 30);
        SCurveResult result = new SCurveResult(
                proyectoId,
                "USD",
                new BigDecimal("500000.00"),
                new BigDecimal("520000.00"),
                List.of(
                        new SCurveResult.SCurveDataPoint(
                                LocalDate.of(2025, 3, 31), 3,
                                new BigDecimal("300000.00"), new BigDecimal("285000.00"), new BigDecimal("296000.00"),
                                new BigDecimal("0.9628"), new BigDecimal("0.9500")),
                        new SCurveResult.SCurveDataPoint(
                                LocalDate.of(2025, 4, 30), 4,
                                new BigDecimal("400000.00"), new BigDecimal("380000.00"), new BigDecimal("396000.00"),
                                new BigDecimal("0.9596"), new BigDecimal("0.9500"))));
        when(obtenerSCurveUseCase.obtener(eq(proyectoId), eq(startDate), eq(endDate))).thenReturn(result);

        mockMvc.perform(get("/api/v1/evm/{proyectoId}/s-curve", proyectoId)
                        .queryParam("startDate", "2025-03-01")
                        .queryParam("endDate", "2025-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPoints.length()").value(2))
                .andExpect(jsonPath("$.dataPoints[0].fechaCorte").value("2025-03-31"))
                .andExpect(jsonPath("$.dataPoints[1].fechaCorte").value("2025-04-30"));

        verify(obtenerSCurveUseCase).obtener(proyectoId, startDate, endDate);
    }
}
