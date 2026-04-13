package com.budgetpro.infrastructure.rest.rrhh.controller;

import com.budgetpro.application.rrhh.dto.AsignacionProyectoResponse;
import com.budgetpro.application.rrhh.exception.AsignacionProyectoConflictoException;
import com.budgetpro.application.rrhh.port.in.AsignarEmpleadoProyectoUseCase;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.application.rrhh.port.in.ActualizarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.ConsultarEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.CrearEmpleadoUseCase;
import com.budgetpro.application.rrhh.port.in.InactivarEmpleadoUseCase;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmpleadoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CrearEmpleadoUseCase crearEmpleadoUseCase;
    @Mock
    private ActualizarEmpleadoUseCase actualizarEmpleadoUseCase;
    @Mock
    private ConsultarEmpleadoUseCase consultarEmpleadoUseCase;
    @Mock
    private InactivarEmpleadoUseCase inactivarEmpleadoUseCase;
    @Mock
    private AsignarEmpleadoProyectoUseCase asignarEmpleadoProyectoUseCase;

    @BeforeEach
    void setUp() {
        EmpleadoController controller = new EmpleadoController(crearEmpleadoUseCase, actualizarEmpleadoUseCase,
                consultarEmpleadoUseCase, inactivarEmpleadoUseCase, asignarEmpleadoProyectoUseCase);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/rrhh/empleados/{id}/asignaciones delega al use case y retorna 201")
    void postAsignacion_retorna201() throws Exception {
        UUID empleadoId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID proyectoId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID asignacionId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        UUID proxyId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        when(asignarEmpleadoProyectoUseCase.asignar(any())).thenReturn(new AsignacionProyectoResponse(asignacionId,
                empleadoId, proyectoId, proxyId, LocalDate.of(2025, 4, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("40"), "OPERARIO"));

        String body = """
                {"proyectoId":"%s","fechaInicio":"2025-04-01","fechaFin":"2025-12-31","tarifaHora":40,"rolProyecto":"OPERARIO"}
                """.formatted(proyectoId);

        mockMvc.perform(post("/api/v1/rrhh/empleados/{empleadoId}/asignaciones", empleadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/rrhh/empleados/%s/asignaciones/%s"
                        .formatted(empleadoId, asignacionId)))
                .andExpect(jsonPath("$.id").value(asignacionId.toString()))
                .andExpect(jsonPath("$.proyectoId").value(proyectoId.toString()));
    }

    @Test
    @DisplayName("POST asignaciones cuando el use case lanza conflicto retorna 409")
    void postAsignacion_conflicto_retorna409() throws Exception {
        UUID empleadoId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID proyectoId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        when(asignarEmpleadoProyectoUseCase.asignar(any())).thenThrow(new AsignacionProyectoConflictoException(
                EmpleadoId.of(empleadoId), LocalDate.of(2025, 4, 1), LocalDate.of(2025, 12, 31)));

        String body = """
                {"proyectoId":"%s","fechaInicio":"2025-04-01","fechaFin":"2025-12-31"}
                """.formatted(proyectoId);

        mockMvc.perform(post("/api/v1/rrhh/empleados/{empleadoId}/asignaciones", empleadoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ASIGNACION_PROYECTO_CONFLICTO"));
    }
}
