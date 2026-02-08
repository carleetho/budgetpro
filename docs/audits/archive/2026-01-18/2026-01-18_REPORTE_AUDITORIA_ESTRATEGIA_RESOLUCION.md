# Auditoría — Ejecución de Estrategia de Resolución

Fecha: 2026-01-18  
Fuente: `docs/ESTRATEGIA_RESOLUCION_DOMINIO_BUDGETPRO.md`

## Estado por ítem (plan vigente)

| ID | Estado | Evidencia | Observación |
| --- | --- | --- | --- |
| CD-01 | RESUELTO | `EstadoProyecto` + `EstadoProyectoConverter` + `ProduccionValidator` | Dominio usa BORRADOR/ACTIVO/SUSPENDIDO/CERRADO y legacy se mapea solo en converter. |
| CD-02 | RESUELTO | `EstadoPresupuesto` + `EstadoPresupuestoConverter` + `AprobarPresupuestoUseCaseImpl` | Dominio usa BORRADOR/CONGELADO/INVALIDADO y legacy se mapea solo en converter. |
| CD-04 | RESUELTO | `Estimacion` + `AprobarEstimacionUseCaseImpl` | Aprobación exige evidencia previa y deja de registrar ingresos sin respaldo. |
| RS-01 | RESUELTO | `Billetera.ingresar` | Se valida evidencia obligatoria; no hay ingreso con evidencia nula. |
| RH-CRIT-01 | RESUELTO | `docs/modules/FINANZAS_BILLETERA_SPECS.md` | Documentación canónica creada. |
| RH-CRIT-02 | RESUELTO | `docs/modules/FINANZAS_BILLETERA_SPECS.md` | Regla documentada en módulo canónico. |
| VD-01 | RESUELTO | `GenerarEstimacionUseCaseImpl` + `AnticipoMovimientoRepository` | Amortización usa saldo real de anticipo. |
| VD-02 | PENDIENTE | `ProcesarCompraService` | Validación de saldo por partida/APU sigue opcional/no implementada. |
| CD-03 | RESUELTO | `CompraDetalle` + `CompraDetalleCommand` | Compra sin partida permitida con clasificación obligatoria. |
| RS-02 | RESUELTO | `RegistrarCompraUseCaseImpl` | Compra sigue requiriendo billetera previa (no auto-creación). |
| RS-03 | PENDIENTE | `ProcesarCompraService` | Sobreconsumo no gobernado por falta de validación. |
| FG-01 | RESUELTO | `docs/modules/FINANZAS_BILLETERA_SPECS.md` | Módulo canónico creado. |
| FG-02 | RESUELTO | `AnticipoMovimientoRepository` | Saldo pendiente calculado desde movimientos. |
| FG-03 | PENDIENTE | `ProcesarCompraService` | No hay gobierno explícito de saldo disponible. |
| RH-01 | RESUELTO | `docs/modules/FINANZAS_BILLETERA_SPECS.md` | Reglas huérfanas documentadas. |
| RH-02 | RESUELTO | `docs/modules/FINANZAS_ALERTAS_SPECS.md` | Reglas huérfanas documentadas. |

## Reportes de cambio existentes
- `docs/audits/2026-01-18_REPORTE_CAMBIO_DOCUMENTACION_BILLETERA_ALERTAS.md`

## Pendientes críticos inmediatos
- VD-02 / RS-03 / FG-03: falta mecanismo de alerta y cuantificación de sobreconsumo por partida/APU.

