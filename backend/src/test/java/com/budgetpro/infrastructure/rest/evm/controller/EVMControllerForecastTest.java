package com.budgetpro.infrastructure.rest.evm.controller;

import com.budgetpro.application.evm.service.EVMCalculationService;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerForecastFechaUseCase;
import com.budgetpro.application.finanzas.evm.port.in.ProyectoNotFoundException;
import com.budgetpro.application.finanzas.evm.port.in.ObtenerSCurveUseCase;
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

import com.budgetpro.infrastructure.rest.evm.controller.EVMController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EVMControllerForecastTest {

    private static final UUID UNKNOWN_PROJECT_ID = new UUID(0, 0);

    private MockMvc mockMvc;

    @Mock
    private EVMCalculationService evmCalculationService;

    @Mock
    private ObtenerSCurveUseCase obtenerSCurveUseCase;

    @Mock
    private ObtenerForecastFechaUseCase obtenerForecastFechaUseCase;

    @Mock
    private com.budgetpro.application.finanzas.evm.port.in.CerrarPeriodoUseCase cerrarPeriodoUseCase;

    @BeforeEach
    void setUp() {
        EVMController controller = new EVMController(
                evmCalculationService,
                obtenerSCurveUseCase,
                obtenerForecastFechaUseCase,
                cerrarPeriodoUseCase);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("AC-E05-05: GET /forecast retorna 404 cuando proyecto no existe")
    void deberiaRetornar404CuandoProyectoNoExiste() throws Exception {
        when(obtenerForecastFechaUseCase.obtener(eq(UNKNOWN_PROJECT_ID)))
                .thenThrow(new ProyectoNotFoundException(UNKNOWN_PROJECT_ID));

        mockMvc.perform(get("/api/v1/evm/{proyectoId}/forecast", UNKNOWN_PROJECT_ID))
                .andExpect(status().isNotFound());
    }
}
