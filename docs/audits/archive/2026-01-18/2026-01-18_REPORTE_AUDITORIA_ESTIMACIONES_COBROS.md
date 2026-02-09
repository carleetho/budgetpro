# Reporte de Auditoría — ESTIMACIONES / COBROS

Fecha: 2026-01-18  
Alcance: `backend/src/main/java/**/estimacion/**` y casos de aprobación de estimaciones.

## Flujos detectados
- **Generación de estimación**: se crea `Estimacion` en estado `BORRADOR`, se calculan montos y se persiste.
- **Amortización de anticipo**: se calcula con un `saldoAnticipoPendiente` placeholder (`999999999`).
- **Aprobación de estimación**: cambia estado a `APROBADA` y registra ingreso en billetera.
- **Pago**: existe transición a `PAGADA` en el modelo, pero no se observan casos de uso que la ejecuten.

## Reglas incumplidas
- **No hay cobro sin evidencia**: el ingreso en billetera por aprobación de estimación usa `evidenciaUrl = null`.
- **Anticipos tienen saldo pendiente real**: la amortización usa placeholder (`saldoAnticipoPendiente = 999999999`).
- **Ingreso ≠ aprobación administrativa**: el caso de uso de aprobación registra ingreso inmediato en billetera.
- **Amortización reduce saldo correctamente**: no existe vínculo a un saldo real de anticipo ni se persiste reducción explícita del saldo pendiente.

## Riesgos contractuales
- **Cobro sin evidencia contractual**: ingreso registrado sin respaldo documental.
- **Amortización sin saldo real**: riesgo de sobre-amortizar y reportar cobros incorrectos.
- **Confusión de hitos administrativos vs financieros**: aprobación administrativa dispara ingreso efectivo.
- **Estado de pago no operacionalizado**: la transición a `PAGADA` no está respaldada por un flujo de pago verificable.

