# Reporte de Auditoría — Sobreconsumo existente (VD-02 / RS-03 / FG-03)

## Alcance
Lectura de backend completa según `docs/CURRENT_TASK.md`.

## Qué cálculos existen hoy
- **Compras**: no hay cálculo de saldo disponible por Partida/APU. Solo se generan consumos y se descuenta billetera.
- **Producción**: existe validación de **metrado vigente** por partida (no APU/costo), compara acumulado aprobado vs metrado.
- **Inventarios**: no se encontró cálculo de saldo disponible por Partida/APU ni validaciones de sobreconsumo.

## Dónde están
- `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`
  - Comentarios de validación de saldo como “opcional/MVP”.
  - Método `validarSaldoPartidas` devuelve lista vacía (no calcula).
- `backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java`
  - `validarNoExcesoMetrado`: compara `acumuladoAprobado + cantidadNueva` contra `metradoVigente`.

## Explícitos o implícitos
- **Explícito**: control de exceso de metrado en producción (no APU, no costo).
- **Implícito**: no se encontró cálculo de saldo disponible por Partida/APU.
- **Compras**: comentarios “opcional/MVP/no bloquea” sin implementación efectiva.

## Fuente canónica documentada
- **No**: no se encontró fuente canónica en docs que describa cálculos actuales de saldo disponible por Partida/APU.

## Riesgos detectados
- **Riesgo crítico**: ausencia total de cálculo de saldo disponible por Partida/APU en compras (sobreconsumo no gobernado).
- **Riesgo crítico**: la única validación existente es por metrado en producción; no cubre costos ni APU.

## Confirmación explícita del estado actual
- **Se calcula parcialmente**: solo validación de exceso de metrado en producción.
- **No se calcula**: saldo disponible por Partida/APU en compras.
