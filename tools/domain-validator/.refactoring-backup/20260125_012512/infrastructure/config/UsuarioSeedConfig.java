package com.budgetpro.infrastructure.config;

import com.budgetpro.infrastructure.persistence.entity.seguridad.RolUsuario;
import com.budgetpro.infrastructure.persistence.entity.seguridad.UsuarioEntity;
import com.budgetpro.infrastructure.persistence.repository.seguridad.UsuarioJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * Seed inicial de usuario administrador.
 */
@Configuration
public class UsuarioSeedConfig {

    @Bean
    public CommandLineRunner seedAdminUser(UsuarioJpaRepository usuarioJpaRepository,
                                           PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "admin@budgetpro.com";
            if (usuarioJpaRepository.existsByEmailIgnoreCase(email)) {
                return;
            }
            UsuarioEntity admin = new UsuarioEntity();
            admin.setId(UUID.randomUUID());
            admin.setNombreCompleto("Administrador BudgetPro");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol(RolUsuario.ADMIN);
            admin.setActivo(true);
            usuarioJpaRepository.save(admin);
        };
    }
}
