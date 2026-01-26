package com.budgetpro.infrastructure.rest.auth.controller;

import com.budgetpro.infrastructure.persistence.entity.seguridad.UsuarioEntity;
import com.budgetpro.infrastructure.persistence.entity.seguridad.RolUsuario;
import com.budgetpro.infrastructure.persistence.repository.seguridad.UsuarioJpaRepository;
import com.budgetpro.infrastructure.rest.auth.dto.AuthMeResponse;
import com.budgetpro.infrastructure.rest.auth.dto.AuthResponse;
import com.budgetpro.infrastructure.rest.auth.dto.LoginRequest;
import com.budgetpro.infrastructure.rest.auth.dto.RegisterRequest;
import com.budgetpro.infrastructure.security.jwt.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controlador de autenticación.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioJpaRepository usuarioJpaRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UsuarioEntity usuario = usuarioJpaRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtService.generarToken(usuario);
        AuthResponse response = new AuthResponse(
                token,
                usuario.getId().toString(),
                usuario.getEmail(),
                usuario.getRol().name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioJpaRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado.");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(java.util.UUID.randomUUID());
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(RolUsuario.RESIDENTE);
        usuario.setActivo(true);

        UsuarioEntity saved = usuarioJpaRepository.save(usuario);
        String token = jwtService.generarToken(saved);
        AuthResponse response = new AuthResponse(
                token,
                saved.getId().toString(),
                saved.getEmail(),
                saved.getRol().name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        UsuarioEntity usuario = usuarioJpaRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        AuthMeResponse response = new AuthMeResponse(
                usuario.getId().toString(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol().name()
        );
        return ResponseEntity.ok(response);
    }
}
