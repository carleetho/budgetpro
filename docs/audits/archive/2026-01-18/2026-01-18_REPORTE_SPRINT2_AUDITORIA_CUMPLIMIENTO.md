# Reporte — Sprint 2 (Auditorías de Cumplimiento)

## Responsable
- Auditor Técnico

## Alcance
- COMPRAS
- INVENTARIOS
- PRODUCCIÓN

## Fuentes canónicas
- `docs/modules/COMPRAS_SPECS.md`
- `docs/modules/INVENTARIOS_SPECS.md`
- `docs/modules/PRESUPUESTO_SPECS.md`
- `docs/decisiones/DECISION_SALDO_DISPONIBLE_PARTIDA_APU.md`
- `docs/audits/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md`
- `docs/audits/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md`

## Resultado por módulo

### COMPRAS
- Cumplimiento: parcial.
- Evidencia de no cálculo de saldo disponible ni cuantificación contractual:
  - `ProcesarCompraService` mantiene validación de saldo como opcional y sin bloqueo.
- Desviaciones:
  - No se observa mecanismo de alerta/quantificación de exceso por APU (solo comentarios).

### INVENTARIOS
- Cumplimiento: parcial.
- Evidencia de ausencia de cálculo contractual de saldo disponible:
  - `RegistrarMovimientoAlmacenUseCaseImpl` solo maneja saldos de kárdex (cantidad/valor).
- Desviaciones:
  - No se observa alerta de sobreconsumo por APU en salidas de inventario.

### PRODUCCIÓN
- Cumplimiento: parcial.
- Evidencia de control por metrado (avance físico), no por APU/costo:
  - `ProduccionValidator.validarNoExcesoMetrado`.
- Desviaciones:
  - Mensaje menciona “saldo disponible” pero la validación es por metrado, no por APU.

## Reportes complementarios
- `docs/audits/RE_AUDITORIA_SOBRECONSUMO_SALDO_PARTIDA_APU.md`

## Lista de desviaciones confirmadas
- No hay mecanismo explícito de alertas de sobreconsumo por APU en COMPRAS/INVENTARIOS.
- No existe cuantificación contractual de sobreconsumo (según decisión canónica).
