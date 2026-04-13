package com.budgetpro.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecuredPingController.class)
@Import(SecuritySmokeTest.TestSecurityConfig.class)
class SecuritySmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeRechazarRequestSinAutenticacion() throws Exception {
        mockMvc.perform(get("/__secured/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void debePermitirRequestConAutenticacion() throws Exception {
        mockMvc.perform(get("/__secured/ping").with(user("user@test.local")))
                .andExpect(status().isOk());
    }

    @Configuration
    static class TestSecurityConfig {
        @Bean
        SecuredPingController securedPingController() {
            return new SecuredPingController();
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(basic -> {})
                    .build();
        }
    }
}

