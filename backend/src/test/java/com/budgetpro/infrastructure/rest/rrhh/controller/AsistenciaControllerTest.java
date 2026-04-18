package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.AsistenciaResponse;
import com.budgetpro.application.rrhh.exception.AsistenciaSuperpuestaException;
import com.budgetpro.application.rrhh.port.in.ConsultarAsistenciaUseCase;
import com.budgetpro.application.rrhh.port.in.RegistrarAsistenciaUseCase;
import com.budgetpro.domain.rrhh.exception.InactiveWorkerException;
import com.budgetpro.domain.rrhh.exception.TrabajadorNoAsignadoAlProyectoException;
import com.budgetpro.domain.rrhh.model.AsistenciaId;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.rrhh.model.EstadoEmpleado;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import com.budgetpro.infrastructure.rest.controller.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AsistenciaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RegistrarAsistenciaUseCase registrarAsistenciaUseCase;

    @Mock
    private ConsultarAsistenciaUseCase consultarAsistenciaUseCase;

    @BeforeEach
    void setUp() {
        AsistenciaController controller = new AsistenciaController(registrarAsistenciaUseCase,
                consultarAsistenciaUseCase);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/rrhh/asistencias: trabajador inactivo → 400 INACTIVE_WORKER")
    void postTrabajadorInactivo_retorna400() throws Exception {
        UUID empleado = UUID.randomUUID();
        UUID proyecto = UUID.randomUUID();
        when(registrarAsistenciaUseCase.registrarAsistencia(any())).thenThrow(
                new InactiveWorkerException(EmpleadoId.of(empleado), EstadoEmpleado.INACTIVO, "inactivo"));

        String body = """
                {"empleadoId":"%s","proyectoId":"%s","fecha":"2025-04-01","horaEntrada":"2025-04-01T08:00:00","horaSalida":"2025-04-01T17:00:00"}
                """.formatted(empleado, proyecto);

        mockMvc.perform(post("/api/v1/rrhh/asistencias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INACTIVE_WORKER"));
    }

    @Test
    @DisplayName("POST /api/v1/rrhh/asistencias: sin asignación a proyecto → 422 EMPLEADO_NO_ASIGNADO_PROYECTO")
    void postSinAsignacion_retorna422() throws Exception {
        UUID empleado = UUID.randomUUID();
        UUID proyecto = UUID.randomUUID();
        when(registrarAsistenciaUseCase.registrarAsistencia(any()))
                .thenThrow(new TrabajadorNoAsignadoAlProyectoException(EmpleadoId.of(empleado),
                        ProyectoId.from(proyecto), LocalDate.of(2025, 4, 1), "no asignado"));

        String body = """
                {"empleadoId":"%s","proyectoId":"%s","fecha":"2025-04-01","horaEntrada":"2025-04-01T08:00:00","horaSalida":"2025-04-01T17:00:00"}
                """.formatted(empleado, proyecto);

        mockMvc.perform(post("/api/v1/rrhh/asistencias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("EMPLEADO_NO_ASIGNADO_PROYECTO"));
    }

    @Test
    @DisplayName("POST /api/v1/rrhh/asistencias: solape horario → 409 ASISTENCIA_SUPERPUESTA")
    void postSolape_retorna409() throws Exception {
        UUID empleado = UUID.randomUUID();
        UUID proyecto = UUID.randomUUID();
        when(registrarAsistenciaUseCase.registrarAsistencia(any()))
                .thenThrow(new AsistenciaSuperpuestaException("superpuesto"));

        String body = """
                {"empleadoId":"%s","proyectoId":"%s","fecha":"2025-04-01","horaEntrada":"2025-04-01T08:00:00","horaSalida":"2025-04-01T17:00:00"}
                """.formatted(empleado, proyecto);

        mockMvc.perform(post("/api/v1/rrhh/asistencias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ASISTENCIA_SUPERPUESTA"));
    }

    @Test
    @DisplayName("POST /api/v1/rrhh/asistencias: registro correcto → 201 + Location")
    void postFeliz_retorna201() throws Exception {
        UUID empleado = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID proyecto = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(registrarAsistenciaUseCase.registrarAsistencia(any())).thenReturn(new AsistenciaResponse(AsistenciaId.of(id),
                LocalDate.of(2025, 4, 1), LocalDateTime.of(2025, 4, 1, 8, 0), LocalDateTime.of(2025, 4, 1, 17, 0), 8.0,
                0.0));

        String body = """
                {"empleadoId":"%s","proyectoId":"%s","fecha":"2025-04-01","horaEntrada":"2025-04-01T08:00:00","horaSalida":"2025-04-01T17:00:00"}
                """.formatted(empleado, proyecto);

        mockMvc.perform(post("/api/v1/rrhh/asistencias").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id.value").value(id.toString()));
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
