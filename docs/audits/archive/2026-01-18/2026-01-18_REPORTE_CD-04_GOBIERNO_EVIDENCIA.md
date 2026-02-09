# Reporte CD-04 — Gobierno de evidencia en cobros y billetera

## Fuentes usadas (únicas autorizadas)
- docs/INVENTARIO_REGLAS_EXISTENTES_FASE1.md
- docs/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md
- docs/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md
- DECISION_CANONICA_ESTADOS.md
- docs/CURRENT_TASK.md

## Hallazgos (según CURRENT_TASK.md)
- RC-03: Aprobación de estimación registra ingreso en billetera con evidencia null.
- RD-02: Regla documental: “No hay cobro sin evidencia.”
- RS-01: Riesgo crítico de ingreso sin respaldo documental.

## Decisión canónica (a aplicar)
- No existe ingreso a billetera sin evidencia contractual válida.
- La evidencia no es opcional ni diferida para el evento de cobro.
- El evento CobroAprobado debe exigir evidencia como invariante de dominio.

## Alcance del cambio (estricto)
- NO redefinir estados.
- NO crear nuevos flujos.
- NO inventar reglas financieras.
- SÍ alinear código con la regla documental existente.

## Tarea concreta para Cursor
- Localizar el punto exacto donde se registra el ingreso a billetera por aprobación de estimación.
- Eliminar cualquier registro de ingreso con evidencia == null.
- Imponer validación obligatoria de evidencia antes de registrar el movimiento.
- Si la evidencia no existe, el sistema debe rechazar la aprobación o dejar la estimación en estado previo sin generar ingreso.
- No registrar ingresos “provisionales”.

## Resultado esperado
- Ingreso a billetera solo ocurre si existe evidencia válida.
- Se elimina el riesgo RS-01.
- Se cumple la regla documental sin introducir nuevos estados.

## Criterio de aceptación
- No existe ningún MovimientoBilletera generado desde aprobación de estimación con evidencia nula.
- El código falla de forma explícita si se intenta aprobar sin evidencia.
- La trazabilidad documental queda intacta.

## Ejecución
Ambigüedad/contradicción detectada: la instrucción de esta tarea exige cambios de comportamiento (alineación de código), pero el mandato del usuario prohíbe escribir código o proponer implementaciones. Bajo estas restricciones, no es posible ejecutar la tarea concreta descrita. Se requiere autorización explícita para modificar código o, alternativamente, redefinir el alcance a un reporte únicamente documental.
