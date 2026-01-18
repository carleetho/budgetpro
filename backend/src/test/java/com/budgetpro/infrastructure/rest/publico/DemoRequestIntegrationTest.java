package com.budgetpro.infrastructure.rest.publico;

import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.repository.marketing.LeadJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para solicitudes públicas de demo.
 */
class DemoRequestIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LeadJpaRepository leadJpaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        leadJpaRepository.deleteAll();
    }

    @Test
    void crearDemoRequest_ok() throws Exception {
        String payload = """
                {
                  "nombreContacto": "Andrea Salas",
                  "email": "andrea@constructora.com",
                  "telefono": "+51 999 999 999",
                  "nombreEmpresa": "Constructora Andina",
                  "rol": "Gerente de Proyectos"
                }
                """;

        mockMvc.perform(post("/api/public/v1/demo-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("NUEVO"))
                .andExpect(jsonPath("$.fechaSolicitud").isNotEmpty());
    }
}
