package com.budgetpro.infrastructure.rest.billetera;

import com.budgetpro.application.finanzas.billetera.port.in.RegistrarMovimientoCajaUseCase;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;
import com.budgetpro.infrastructure.rest.billetera.controller.BilleteraController;
import com.budgetpro.infrastructure.rest.billetera.dto.RegistrarMovimientoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BilleteraController.class)
@org.springframework.test.context.ContextConfiguration(classes = TestApplication.class)
@org.springframework.context.annotation.Import(BilleteraIntegrationTest.TestControllerAdvice.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class BilleteraIntegrationTest {

        @org.springframework.web.bind.annotation.RestControllerAdvice
        public static class TestControllerAdvice {
                @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
                public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> handleIllegalArgument(
                                IllegalArgumentException ex) {
                        java.util.Map<String, Object> body = new java.util.HashMap<>();
                        body.put("message", ex.getMessage());
                        body.put("error", "INVALID_ARGUMENT");
                        body.put("status", org.springframework.http.HttpStatus.BAD_REQUEST.value());
                        return org.springframework.http.ResponseEntity.badRequest().body(body);
                }
        }

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private RegistrarMovimientoCajaUseCase registrarMovimientoUseCase;

        @Autowired
        private ObjectMapper objectMapper;

        private BilleteraId billeteraId;

        @BeforeEach
        void setUp() {
                billeteraId = BilleteraId.of(UUID.randomUUID());
        }

        @Test
        void shouldReturn400_WhenMonedaIsMissing() throws Exception {
                RegistrarMovimientoRequest request = new RegistrarMovimientoRequest(BigDecimal.TEN, null, // Missing
                                                                                                          // moneda
                                "INGRESO", "Ref", null);

                mockMvc.perform(post("/api/v1/billeteras/{id}/movimientos", billeteraId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_WhenMonedaIsInvalidCode() throws Exception {
                RegistrarMovimientoRequest request = new RegistrarMovimientoRequest(BigDecimal.TEN, "XYZ", // Invalid
                                                                                                           // currency
                                                                                                           // code
                                "INGRESO", "Ref", null);

                mockMvc.perform(post("/api/v1/billeteras/{id}/movimientos", billeteraId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest());
        }

        @Test
        void shouldNormalizeCurrencyAndSuccess_WhenLowercaseProvided() throws Exception {
                RegistrarMovimientoRequest request = new RegistrarMovimientoRequest(BigDecimal.TEN, "pen", // Lowercase
                                                                                                           // currency
                                "INGRESO", "Ref", null);

                MovimientoCaja mockMovimiento = createMockMovimiento("PEN");

                // Mock the case where normalization happens before calling useCase
                when(registrarMovimientoUseCase.registrar(any(BilleteraId.class), any(BigDecimal.class), eq("PEN"), // Expect
                                                                                                                    // normalized
                                                                                                                    // uppercase
                                any(TipoMovimiento.class), any(String.class), any())).thenReturn(mockMovimiento);

                mockMvc.perform(post("/api/v1/billeteras/{id}/movimientos", billeteraId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                                .andExpect(jsonPath("$.moneda", is("PEN")));
        }

        @Test
        void shouldReturn400_WhenCurrencyMismatch_Ingreso() throws Exception {
                RegistrarMovimientoRequest request = new RegistrarMovimientoRequest(BigDecimal.TEN, "USD", // Mismatch
                                                                                                           // with
                                                                                                           // Wallet PEN
                                "INGRESO", "Ref", null);

                // Simulate domain exception
                when(registrarMovimientoUseCase.registrar(any(BilleteraId.class), any(BigDecimal.class), eq("USD"),
                                any(TipoMovimiento.class), any(String.class), any()))
                                                .thenThrow(new IllegalArgumentException("Currency mismatch"));

                mockMvc.perform(post("/api/v1/billeteras/{id}/movimientos", billeteraId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().is4xxClientError());
        }

        @Test
        void shouldReturnSuccess_WhenCurrencyMatches() throws Exception {
                RegistrarMovimientoRequest request = new RegistrarMovimientoRequest(BigDecimal.TEN, "PEN", // Matches
                                                                                                           // Wallet PEN
                                "INGRESO", "Success Ref", null);

                MovimientoCaja mockMovimiento = createMockMovimiento("PEN");

                when(registrarMovimientoUseCase.registrar(any(BilleteraId.class), any(BigDecimal.class), eq("PEN"),
                                any(TipoMovimiento.class), any(String.class), any())).thenReturn(mockMovimiento);

                mockMvc.perform(post("/api/v1/billeteras/{id}/movimientos", billeteraId.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                                .andExpect(jsonPath("$.moneda", is("PEN")));
        }

        private MovimientoCaja createMockMovimiento(String moneda) {
                return MovimientoCaja.reconstruir(UUID.randomUUID(), billeteraId, BigDecimal.TEN, moneda,
                                TipoMovimiento.INGRESO, LocalDateTime.now(), "Ref", null, null);
        }
}
