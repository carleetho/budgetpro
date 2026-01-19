# MIGRATION_GUIDE

## V1–V3: Proxy y Snapshots de Catálogo

- V1__create_recurso_proxy.sql: tabla `recurso_proxy` con constraint único `(external_id, catalog_source)` e índices por estado y tipo. Incluye `created_by`.
- V2__create_apu_snapshot.sql: tabla `apu_snapshot` con tracking de rendimiento y relación con `partida`. Incluye `created_by`.
- V3__create_apu_insumo_snapshot.sql: tabla `apu_insumo_snapshot` con datos denormalizados del recurso y cascade por APU. Incluye `created_by`.

Notas:
- Fase dual-write: no se elimina ni modifica ninguna tabla existente.
- Tipos monetarios usan DECIMAL(19,4) y cantidades DECIMAL(19,6).
