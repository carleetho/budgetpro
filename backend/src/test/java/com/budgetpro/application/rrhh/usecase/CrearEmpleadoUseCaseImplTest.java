package com.budgetpro.application.rrhh.usecase;

import com.budgetpro.application.rrhh.dto.CrearEmpleadoCommand;
import com.budgetpro.application.rrhh.event.PersonalContratadoEvent;
import com.budgetpro.application.rrhh.exception.NumeroIdentificacionDuplicadoException;
import com.budgetpro.application.rrhh.port.out.EmpleadoRepositoryPort;
import com.budgetpro.domain.rrhh.model.Empleado;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearEmpleadoUseCaseImplTest {

    @Mock
    private EmpleadoRepositoryPort empleadoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CrearEmpleadoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CrearEmpleadoUseCaseImpl(empleadoRepository, eventPublisher);
    }

    @Test
    void ejecutar_trasGuardar_publicaPersonalContratadoEventConSieteCampos() {
        when(empleadoRepository.existsByNumeroIdentificacion("ID-1")).thenReturn(false);
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearEmpleadoCommand command = new CrearEmpleadoCommand("Ana", "García", "ID-1", "a@test.com", null, null,
                LocalDate.of(2025, 3, 1), new BigDecimal("3500.00"), "Ingeniero", TipoEmpleado.PERMANENTE);

        useCase.ejecutar(command);

        ArgumentCaptor<PersonalContratadoEvent> captor = ArgumentCaptor.forClass(PersonalContratadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PersonalContratadoEvent event = captor.getValue();
        assertEquals("Ana", event.getNombre());
        assertEquals("García", event.getApellido());
        assertEquals("Ingeniero", event.getCargo());
        assertEquals(TipoEmpleado.PERMANENTE.name(), event.getTipo());
        assertEquals(LocalDate.of(2025, 3, 1), event.getFechaContratacion());
        assertNotNull(event.getEmpleadoId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void ejecutar_identificacionDuplicada_noPublicaEvento() {
        when(empleadoRepository.existsByNumeroIdentificacion("ID-1")).thenReturn(true);

        CrearEmpleadoCommand command = new CrearEmpleadoCommand("Ana", "García", "ID-1", "a@test.com", null, null,
                LocalDate.of(2025, 3, 1), new BigDecimal("3500.00"), "Ingeniero", TipoEmpleado.PERMANENTE);

        assertThrows(NumeroIdentificacionDuplicadoException.class, () -> useCase.ejecutar(command));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
