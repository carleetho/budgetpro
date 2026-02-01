# CROSS CUTTING Module - Canonical Specification

> **Status**: Completed (90%)
> **Owner**: Architecture Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State     | Deliverables                                |
| ----------- | --------- | ---------------- | ------------------------------------------- |
| **Current** | Now       | 90% (Foundation) | Auth, Validation, Hexagonal Core, Logging   |
| **Next**    | +1 Month  | 95%              | Advanced Audit, RBAC Granularity            |
| **Target**  | +3 Months | 100%             | Tenant Isolation (Multitenancy preparation) |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                | Status         |
| ---- | ------------------------------------------------------------------- | -------------- |
| X-01 | **Hexagonal Purity**: Domain must not depend on Infrastructure.     | ✅ Implemented |
| X-02 | **Fail-Fast**: Validation must occur at Boundary and Inside Domain. | ✅ Implemented |
| X-03 | **Audit**: Critical operations (Freeze, Approve) must be traceable. | ✅ Implemented |

## 3. Domain Events

| Event Name            | Trigger       | Content (Payload) | Status |
| --------------------- | ------------- | ----------------- | ------ |
| `UserRegisteredEvent` | New user      | `userId`, `email` | ✅     |
| `SystemErrorEvent`    | 500 Exception | `stacktrace`      | ✅     |

## 4. State Constraints

- N/A (Stateless util services)

## 5. Data Contracts

### Entity: User

- `id`: UUID
- `email`: String
- `roles`: List<String>

## 6. Use Cases

| ID     | Use Case                | Priority | Status |
| ------ | ----------------------- | -------- | ------ |
| UC-X01 | User Login/Register     | P0       | ✅     |
| UC-X02 | Permission Check        | P0       | ✅     |
| UC-X03 | Send Email Notification | P1       | ✅     |

## 7. Domain Services

- **Service**: `AuthService`, `NotificationService`
- **Responsibility**: Security and Comms.

## 8. REST Endpoints

| Method | Path                    | Description | Status |
| ------ | ----------------------- | ----------- | ------ |
| POST   | `/api/v1/auth/login`    | JWT Login   | ✅     |
| POST   | `/api/v1/auth/register` | Sign up     | ✅     |

## 9. Observability

- **Metrics**: `http.request.count`, `p99.latency`
- **Logs**: Structured JSON logging.

## 10. Integration Points

- **Consumes**: N/A
- **Exposes**: `SecurityContext` to ALL modules.

## 11. Technical Debt & Risks

- [ ] **RBAC**: Current Roles are simple strings. Need dynamic Permission sets. (Medium)
