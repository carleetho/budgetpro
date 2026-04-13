package com.budgetpro.infrastructure.rest.reajuste;

import com.budgetpro.application.reajuste.dto.EstimacionReajusteResponse;
import com.budgetpro.application.reajuste.port.in.CalcularReajusteUseCase;
import com.budgetpro.infrastructure.rest.reajuste.controller.ReajusteController;
import com.budgetpro.infrastructure.rest.reajuste.dto.CalcularReajusteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReajusteController.class
)
@org.springframework.test.context.ContextConfiguration(classes = ReajusteControllerTest.TestApp.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ReajusteControllerTest {

    @SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    static class TestApp {
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CalcularReajusteUseCase calcularReajusteUseCase;

    @MockBean
    com.budgetpro.infrastructure.security.jwt.JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void debeCalcularReajuste_yRetornar201() throws Exception {
        UUID id = UUID.randomUUID();
        UUID proyectoId = UUID.randomUUID();
        UUID presupuestoId = UUID.randomUUID();
        LocalDate fechaCorte = LocalDate.now();

        EstimacionReajusteResponse response = new EstimacionReajusteResponse(
                id,
                proyectoId,
                presupuestoId,
                1,
                fechaCorte,
                "INPC",
                LocalDate.now().minusMonths(2),
                "INPC",
                LocalDate.now().minusMonths(1),
                BigDecimal.ONE,
                BigDecimal.valueOf(2),
                BigDecimal.TEN,
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                "BORRADOR",
                null,
                List.of()
        );

        when(calcularReajusteUseCase.calcular(eq(proyectoId), eq(presupuestoId), any(LocalDate.class), anyString(), any(LocalDate.class), anyString(), any(LocalDate.class)))
                .thenReturn(response);

        CalcularReajusteRequest request = new CalcularReajusteRequest(
                proyectoId,
                presupuestoId,
                fechaCorte,
                "INPC",
                LocalDate.now().minusMonths(2),
                "INPC",
                LocalDate.now().minusMonths(1)
        );

        mockMvc.perform(post("/api/v1/reajustes/calcular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.presupuestoId").value(presupuestoId.toString()));
    }
}

