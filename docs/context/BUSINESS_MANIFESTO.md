# BUSINESS MANIFESTO — BUDGETPRO

## 1. Naturaleza del Sistema

BudgetPro no es una aplicación administrativa ni un ERP genérico.
Es un sistema disciplinario de ejecución de obra, diseñado para proteger
la utilidad del proyecto mediante reglas de negocio ejecutables,
trazabilidad forense y control preventivo del presupuesto.

El sistema no existe para facilitar la improvisación.
Existe para impedir pérdidas silenciosas.

---

## 2. Principio Supremo: El Presupuesto es Ley

El presupuesto aprobado constituye un contrato digital inmutable.
Una vez congelado, gobierna toda la ejecución del proyecto.

Ningún proceso operativo puede existir fuera del presupuesto:
- compras,
- inventarios,
- mano de obra,
- avances físicos,
- pagos.

Si una acción no puede vincularse a una partida presupuestaria válida,
esa acción es ilegal dentro del sistema.

---

## 3. Principio de Saldo Disponible Preventivo

El saldo disponible de una partida se calcula con la fórmula canónica:

Saldo_Disponible = Presupuesto_Asignado - (Gastos_Reales + Compromisos_Pendientes)

Los compromisos pendientes se registran en el instante de la aprobación
de la compra, no en el pago.

---

## 4. Principio de Origen del Costo

No existe costo sin origen técnico.

Todo gasto debe tener:
- una partida de origen,
- un motivo explícito,
- un responsable identificable.

El sistema rechaza cualquier transacción que no pueda explicar
por qué existe y a qué contrato responde.

---

## 5. Principio de Verdad No Retroactiva

La historia del proyecto no se reescribe.

Los datos históricos no se corrigen, se explican mediante eventos formales
como órdenes de cambio o excepciones justificadas.

Cualquier intento de modificar el pasado sin evidencia
constituye una violación del sistema.

---

## 6. Principio de Excepción Formal

Las desviaciones de la línea base no son errores del sistema,
son decisiones humanas.

Por lo tanto:
- toda excepción debe clasificarse,
- debe requerir autorización explícita,
- debe quedar registrada de forma permanente.

Las excepciones nunca sobrescriben la línea base.
La modifican solo mediante capas trazables.

---

## 7. Principio de Protección de Utilidad

El objetivo primario del sistema es proteger la utilidad del proyecto,
no la comodidad del usuario.

Cuando existe conflicto entre facilidad de uso y control financiero,
el sistema prioriza el control.

La fricción temprana es preferible a la pérdida tardía.

---

## 8. Relación entre Dominios Principales

El sistema se organiza bajo una jerarquía clara:

- El Proyecto es el contenedor contractual.
- El Presupuesto define los límites de ejecución.
- Las Partidas autorizan costos específicos.
- Las Compras y la Mano de Obra ejecutan el contrato.
- El Inventario es un medio operativo, no un fin contable.
- El Tiempo contextualiza el costo.
- El EVM diagnostica la salud financiera.

Ningún dominio puede operar de forma autónoma
ni ignorar esta jerarquía.

---

## 9. Principio de Integridad Criptográfica (Swiss-Grade)

El presupuesto aprobado es sellado criptográficamente mediante hashes SHA-256.

### Sellado Dual-Hash

Una vez aprobado, el presupuesto queda protegido por dos hashes:

- **Hash de Aprobación (Inmutable)**: Captura la estructura completa del presupuesto
  - Atributos del Presupuesto (ID, nombre, proyecto, estado, contractual)
  - Estructura jerárquica de Partidas (WBS)
  - APU Snapshots con rendimiento vigente e insumos
  - Merkle Tree de todas las Partidas para validación eficiente
  
- **Hash de Ejecución (Dinámico)**: Captura el estado financiero actual
  - Encadenado al hash de aprobación (base inmutable)
  - Estado financiero por Partida (gastos reales, compromisos pendientes)
  - Se actualiza después de cada transacción financiera

### Garantías de Integridad

- **Inmutabilidad Estructural**: El presupuesto aprobado no puede modificarse estructuralmente
- **Detección de Tampering**: Cualquier modificación no autorizada se detecta antes de permitir transacciones
- **Audit Trail Completo**: Todos los eventos de integridad se registran para análisis forense
- **Validación Preventiva**: Las compras y egresos validan integridad antes de ejecutarse

### Reglas de Negocio

1. **Hard-Freeze Pattern**: Después de aprobación, modificaciones estructurales bloquean transacciones
2. **Validación Pre-Transacción**: Toda compra o egreso valida integridad antes de ejecutarse
3. **Actualización de Hash de Ejecución**: Después de cada transacción financiera, el hash de ejecución se recalcula
4. **Violación = Bloqueo**: Cualquier violación de integridad bloquea la transacción y genera alerta crítica

---

## 10. Decisión Arquitectónica No Negociable

BudgetPro opera exclusivamente en modalidad online.

La integridad contractual y la validación sincrónica
tienen prioridad sobre la disponibilidad offline.

Esta decisión es estratégica y no será revertida
por conveniencia técnica.
