# SEGURIDAD MODULE CANONICAL NOTEBOOK

## 1. Propósito del Módulo
El módulo de Seguridad gestiona la autenticación, autorización y protección de rutas de la aplicación. Utiliza JWT (JSON Web Tokens) para sesiones stateless y RBAC (Role-Based Access Control) para permisos.

## 2. Invariantes y Reglas de Negocio

### REGLA-051: Configuración de Secreto JWT

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** CRITICAL

**Description:**
JWT_SECRET es obligatorio y debe tener al menos 32 caracteres.

**Implementation:**
- **Entity/Class:** `JwtService`
- **Method:** Constructor
- **Validation:** length >= 32

**Code Evidence:**
```java
if (secret.length() < 32) {
    throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
}
```

### REGLA-052: Protección de Rutas

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** CRITICAL

**Description:**
Las rutas `/api/public/**`, `/api/v1/auth/**` y documentación auth son públicas; el resto requiere autenticación.

**Implementation:**
- **Entity/Class:** `SecurityConfig`
- **Method:** `securityFilterChain`
- **Validation:** Spring Security `requestMatchers`

**Code Evidence:**
```java
.requestMatchers("/api/public/**", "/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
.anyRequest().authenticated()
```

### REGLA-053: Política CORS

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** HIGH

**Description:**
CORS permite origen `http://localhost:3000` y métodos estándar.

**Implementation:**
- **Entity/Class:** `SecurityConfig`
- **Method:** `corsConfigurationSource`
- **Validation:** `CorsConfiguration`

**Code Evidence:**
```java
config.setAllowedOrigins(List.of("http://localhost:3000"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
```

### REGLA-055: Unicidad de Email

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** CRITICAL

**Description:**
La entidad Usuario exige email único.

**Implementation:**
- **Entity/Class:** `UsuarioEntity`
- **Method:** Annotation
- **Validation:** `@UniqueConstraint`, Database Index

**Code Evidence:**
```java
@Table(name = "usuarios",
        uniqueConstraints = @UniqueConstraint(name = "uq_usuarios_email", columnNames = "email"))
```

### REGLA-056: Roles Válidos

**Status:** ✅ Verified (Enum)
**Type:** Gobierno
**Severity:** HIGH

**Description:**
El rol de usuario debe estar en {ADMIN, RESIDENTE, GERENTE, AUDITOR}.

**Implementation:**
- **Entity/Class:** `RolUsuario` / Database
- **Method:** Enum definition / Check constraint
- **Validation:** Java Enum / SQL Check

**Code Evidence:**
```java
public enum RolUsuario {
    ADMIN, RESIDENTE, GERENTE, AUDITOR
}
```

### REGLA-075: Campos Obligatorios Usuario

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En usuario: nombre, email y password no pueden estar vacíos; email debe ser válido; rol y activo no nulos.

**Implementation:**
- **Entity/Class:** `UsuarioEntity`
- **Method:** Annotations
- **Validation:** `@NotBlank`, `@Email`, `@NotNull`

**Code Evidence:**
```java
@NotBlank @Column(name = "nombre_completo")
private String nombreCompleto;
@NotBlank @Email @Column(name = "email")
private String email;
@NotBlank @Column(name = "password")
private String password;
```

### REGLA-078: Validación Login

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** HIGH

**Description:**
Para login: email debe ser válido y no vacío; password es obligatoria.

**Implementation:**
- **Entity/Class:** `LoginRequest` (DTO)
- **Method:** Annotations
- **Validation:** `@Email`, `@NotBlank`

**Code Evidence:**
```java
@Email(message = "El email debe ser válido")
@NotBlank(message = "El email es obligatorio")
String email,
@NotBlank(message = "La contraseña es obligatoria")
String password
```

### REGLA-079: Validación Registro

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** HIGH

**Description:**
Para registro: nombreCompleto obligatorio; email válido y obligatorio; password obligatoria con tamaño 6-72.

**Implementation:**
- **Entity/Class:** `RegisterRequest` (DTO)
- **Method:** Annotations
- **Validation:** `@NotBlank`, `@Email`, `@Size(min=6, max=72)`

**Code Evidence:**
```java
@NotBlank @Size(max = 150) String nombreCompleto,
@Email @NotBlank @Size(max = 200) String email,
@NotBlank @Size(min = 6, max = 72) String password
```

### REGLA-138: Expiración JWT

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** HIGH

**Description:**
JWT expira en 24 horas por defecto (jwt.expiration-hours: 24).

**Implementation:**
- **Entity/Class:** `JwtService`
- **Method:** Constructor
- **Validation:** `Duration.ofHours(24)`

**Code Evidence:**
```java
@Value("${jwt.expiration-hours:24}") long expirationHours
// ...
this.expiration = Duration.ofHours(expirationHours);
```
