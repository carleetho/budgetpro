package com.budgetpro.infrastructure.security.service;

import com.budgetpro.infrastructure.persistence.repository.seguridad.UsuarioJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * UserDetailsService basado en UsuarioEntity.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public CustomUserDetailsService(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioJpaRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
