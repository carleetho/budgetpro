# Estrategia de Resolución — Dominio BudgetPro

## Objetivo
Definir una estrategia operativa para resolver los ítems listados en `docs/PLAN_DE_RESOLUCION_DE_DOMINIO_BUDGETPRO.md` sin introducir reglas nuevas y reportando cada cambio en un `.md`.

## Principios
- Solo resolver lo listado en el plan vigente.
- Mantener trazabilidad por cambio (un reporte `.md` por cambio).
- No mezclar decisiones de dominios distintos en un solo cambio.
- Prioridad por severidad: CRÍTICO → ALTO → MEDIO.

## Secuencia por severidad
### CRÍTICO
1. CD-01: Estados de Proyecto inconsistentes.
2. CD-02: Estados de Presupuesto inconsistentes.
3. CD-04 / RS-01: Cobro sin evidencia.
4. RH-CRIT-01 / RH-CRIT-02: reglas críticas sin módulo canónico.

### ALTO
1. VD-01 / FG-02: anticipo sin saldo real.
2. CD-03: compra sin partida vs validación obligatoria.
3. RS-02: bloqueo por billetera ausente.
4. RS-03 / VD-02 / FG-03: sobreconsumo y gobierno de saldo.
5. FG-01: billetera sin módulo canónico.

### MEDIO
1. RH-01 / RH-02: reglas huérfanas sin documentación canónica.

## Estructura mínima de cada cambio
Para cada ítem:
- Decisión explícita (definir / sí / no).
- Evidencia de fuente canónica.
- Cambio puntual (documento o código según corresponda).
- Reporte `.md` del cambio con:
  - ID del problema
  - Resumen del cambio
  - Archivos modificados
  - Impacto y riesgos

## Reporte por cambio
Ubicación sugerida: `docs/audits/`
Convención de nombre:
`YYYY-MM-DD_REPORTE_CAMBIO_<ID>.md`

