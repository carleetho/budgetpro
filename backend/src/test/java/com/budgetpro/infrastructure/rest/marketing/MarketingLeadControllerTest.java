package com.budgetpro.infrastructure.rest.marketing;

import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEntity;
import com.budgetpro.infrastructure.persistence.entity.marketing.LeadEstado;
import com.budgetpro.infrastructure.persistence.repository.marketing.LeadJpaRepository;
import com.budgetpro.infrastructure.rest.marketing.controller.MarketingLeadController;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MarketingLeadController.class
)
@org.springframework.test.context.ContextConfiguration(classes = MarketingLeadControllerTest.TestApp.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class MarketingLeadControllerTest {

    @SpringBootApplication(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    static class TestApp {
    }

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LeadJpaRepository leadJpaRepository;

    @MockBean
    com.budgetpro.infrastructure.security.jwt.JwtService jwtService;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    void debeListarLeads() throws Exception {
        UUID id = UUID.randomUUID();
        LeadEntity lead = new LeadEntity();
        lead.setId(id);
        lead.setEstado(LeadEstado.NUEVO);
        lead.setFechaSolicitud(LocalDateTime.now());

        when(leadJpaRepository.findAll()).thenReturn(List.of(lead));

        mockMvc.perform(get("/api/v1/marketing/leads").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].estado").value("NUEVO"));
    }

    @Test
    void debeObtenerLeadPorId() throws Exception {
        UUID id = UUID.randomUUID();
        LeadEntity lead = new LeadEntity();
        lead.setId(id);
        lead.setEstado(LeadEstado.NUEVO);
        lead.setFechaSolicitud(LocalDateTime.now());

        when(leadJpaRepository.findById(id)).thenReturn(Optional.of(lead));

        mockMvc.perform(get("/api/v1/marketing/leads/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.estado").value("NUEVO"));
    }
}

