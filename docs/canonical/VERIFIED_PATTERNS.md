# Verified Patterns & Architecture Constraints

> **DOCTRINE:** Code Reality First.
> This document captures patterns that have been **executed and verified** in the codebase.
> Deviating from these patterns requires a "Deep Radiography" justification.

## 1. Testing Strategy

### 1.1 API Integration Tests (Slice Testing)

Due to environment instability with Docker/Testcontainers, the _Verified Pattern_ for API testing is **Slice Testing**.

- **Annotation**: `@WebMvcTest(TargetController.class)`
- **Configuration**: Must use `@ContextConfiguration(classes = TestApplication.class)`
- **Security**: Mocked via `@AutoConfigureMockMvc(addFilters = false)` for functional validation.
- **Dependencies**: Bypasses `DataSource` and `JPA` initialization.
- **Status**: ✅ VERIFIED in `BilleteraIntegrationTest.java`.

## 2. Exception Handling

### 2.1 Domain Invariants

- **Pattern**: Use standard Java Runtime Exceptions for validation constraints.
- **Implementation**: `IllegalArgumentException` for invalid arguments (e.g., Currency Mismatch).
- **Presentation**: Mapped to `400 Bad Request` via Global or Local `ControllerAdvice`.
- **Status**: ✅ VERIFIED.

## 3. Persistence Layer

### 3.1 Mapper Integrity

- **Pattern**: Defensive Mapping (No Lazy Code).
- **Rule**: Mappers provided with `null` inputs MUST throw specific exceptions.
- **Forbidden**: returning `null` silently.
- **Verification**: Enforced by AXIOM Sentinel.
- **Status**: ✅ VERIFIED in `BilleteraMapper.java`.

## 4. Domain Layer

### 4.1 Invariants

- **Validation**: Enforced inside Domain Entity methods (`ingresar`, `egresar`).
- **Status**: ✅ VERIFIED.
