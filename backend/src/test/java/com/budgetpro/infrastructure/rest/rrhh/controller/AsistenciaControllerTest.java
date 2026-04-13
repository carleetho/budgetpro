package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.port.in.ConsultarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.infrastructure.rest.controller.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AsistenciaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegistrarAsistenciaUseCase registrarAsistenciaUseCase;

    @Mock
    private ConsultarAsistenciaUseCase consultarAsistenciaUseCase;

    @BeforeEach
    void setUp() {
        AsistenciaController controller = new AsistenciaController(registrarAsistenciaUseCase,
                consultarAsistenciaUseCase);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/rrhh/asistencias con empleadoId y proyectoId en blanco retorna 400")
    void listarConParametrosEnBlanco_retorna400() throws Exception {
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fin = LocalDate.of(2025, 4, 30);
        mockMvc.perform(get("/api/v1/rrhh/asistencias")
                        .param("empleadoId", "   ")
                        .param("proyectoId", "")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", fin.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_ATTENDANCE_FILTERS"));
    }

    @Test
    @DisplayName("GET /api/v1/rrhh/asistencias sin empleadoId ni proyectoId retorna 400 y MISSING_ATTENDANCE_FILTERS")
    void listarSinFiltros_retorna400() throws Exception {
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fin = LocalDate.of(2025, 4, 30);
        mockMvc.perform(get("/api/v1/rrhh/asistencias")
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", fin.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("MISSING_ATTENDANCE_FILTERS"));
    }

    @Test
    @DisplayName("GET /api/v1/rrhh/asistencias con empleadoId válido no lanza por filtros")
    void listarConEmpleadoId_no400PorFiltros() throws Exception {
        UUID empleado = UUID.randomUUID();
        LocalDate inicio = LocalDate.of(2025, 4, 1);
        LocalDate fin = LocalDate.of(2025, 4, 30);
        when(consultarAsistenciaUseCase.consultarPorEmpleado(any(), eq(inicio), eq(fin)))
                .thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/rrhh/asistencias")
                        .param("empleadoId", empleado.toString())
                        .param("fechaInicio", inicio.toString())
                        .param("fechaFin", fin.toString()))
                .andExpect(status().isOk());
    }
}
