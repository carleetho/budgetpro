# BILLETERA_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Billetera |
| % oficial (tablero) | **70%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [BILLETERA_MODULE_CANONICAL.md](../../modules/BILLETERA_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `BilleteraController`, `BilleteraQueryController`, `RegistrarMovimientoCajaUseCase` |

## 2. Superficie de código (evidencia)

- **REST mutación:** `BilleteraController` — `POST /api/v1/billeteras/{billeteraId}/movimientos` → `RegistrarMovimientoCajaUseCase` (201 + cuerpo `MovimientoCaja`).
- **REST consulta:** `BilleteraQueryController` — `GET .../saldo`, `GET .../movimientos` usando **`BilleteraJpaRepository`** y entidades `BilleteraEntity` / `MovimientoCajaEntity` (capa persistencia expuesta directamente en el adaptador REST).
- **Dominio / aplicación:** `com.budgetpro.domain.finanzas.model` (`Billetera`, `MovimientoCaja`); `RegistrarMovimientoCajaUseCaseImpl` (deuda **EGRESO** ya en **O-03** y canónico UC-B02).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Resolución `billeteraId` por proyecto** | Canónico §8 nota: no hay `GET .../proyectos/{id}/billetera`; cliente debe conocer UUID o resolver fuera de este controller | P2 (UX) |
| GF-02 | **Listado movimientos sin paginación** | `findWithMovimientosById` + orden en memoria; lista completa en respuesta | P2 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-042 / invariantes saldo: coherentes con dominio; **UC-B02 EGRESO** seguir **O-03** |
| GR-02 | Evento canónico `SaldoInsuficienteEvent` 🟡: en código existe **`SaldoInsuficienteException`** (dominio/compra); no se auditó bus de eventos de dominio separado en esta pasada |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|-------|
| DT-01 | **Hexagonal / capas** | Consultas vía JPA en `BilleteraQueryController` — ver **O-15** |
| DT-02 | Concurrencia / optimistic locking | Canónico §11 |
| DT-03 | Métrica `wallet.balance.current` §9 | Sin evidencia Micrometer dedicada en escaneo rápido (patrón similar a inventario) |

## 6. Candidatos de cierre (priorizado)

1. **P0/P1**: Cerrar **O-03** (EGRESO alineado a `Billetera.egresar` + contrato API).
2. **P2**: Extraer puerto de consulta de saldo/movimientos a application + tests; mantener JPA solo en adaptadores de salida.
3. **P2**: Paginación o límites en `GET .../movimientos`.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: UC-B02 cerrado + endpoint opcional por `proyectoId` o documentación OpenAPI explícita de resolución de billetera.
- **~70%** mientras O-03 permanezca abierto.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-03**, **O-15**).
