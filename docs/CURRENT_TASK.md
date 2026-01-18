CURRENT_TASK.md
Auditoría de Código — Sobreconsumo y Saldo Disponible (VD-02 / RS-03 / FG-03)
Rol de Cursor

Actúa exclusivamente como Auditor Técnico de Dominio.
No eres implementador.
No eres diseñador.
No propones mejoras.

Tu función es leer código existente y verificar comportamiento real contra documentación canónica.

Objetivo

Re-auditar el backend para confirmar:

que NO existe cálculo de saldo disponible por Partida/APU,

que NO existe cuantificación contractual de sobreconsumo,

que NO se infieren fórmulas implícitas,

que el sistema solo alerta o registra, sin bloquear por saldo.

Esta auditoría cierra definitivamente VD-02 / RS-03 / FG-03.

Fuentes documentales canónicas (obligatorias)

Debes usar únicamente estos documentos como verdad de dominio:

docs/modules/COMPRAS_SPECS.md

docs/modules/INVENTARIOS_SPECS.md

docs/modules/PRESUPUESTO_SPECS.md

docs/decisiones/DECISION_SALDO_DISPONIBLE_PARTIDA_APU.md

docs/audits/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md

docs/audits/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md

Si algo no está definido ahí, se considera NO DEFINIDO.

Alcance de lectura de código

Audita como mínimo:

COMPRAS

ProcesarCompraService

cualquier método que:

valide cantidades,

compare contra APU,

mencione “saldo”, “disponible”, “exceso”, “cap”, “tope”.

INVENTARIOS

servicios de salida de inventario,

cualquier referencia a:

“saldo APU”,

“disponible”,

“exceso”.

PRODUCCIÓN (solo referencia)

validaciones de metrado existentes,

confirmar que NO aplican a APU/costo.

Qué debes verificar (checklist estricto)

Para cada módulo indica SÍ / NO / NO APLICA, con evidencia:

¿Existe cálculo explícito de saldo disponible por Partida?

¿Existe cálculo explícito de saldo disponible por APU?

¿Existe fórmula que cuantifique sobreconsumo contractual?

¿Se bloquea una compra por exceder APU?

¿Se bloquea una salida de inventario por exceder APU?

¿Existen alertas sin cálculo contractual?

¿Alguna validación infiere reglas no documentadas?

Prohibiciones absolutas

❌ No propongas fórmulas.

❌ No sugieras cambios.

❌ No corrijas código.

❌ No “completes” reglas faltantes.

❌ No interpretes intención del desarrollador.

Si no existe definición documental, responde “NO DEFINIDO CANÓNICAMENTE”.

Formato de salida (OBLIGATORIO)

Debes generar un único archivo:

docs/audits/RE_AUDITORIA_SOBRECONSUMO_SALDO_PARTIDA_APU.md

Estructura obligatoria del reporte

Resumen ejecutivo

Confirmación clara:

“El sistema NO implementa cálculo de saldo disponible ni cuantificación contractual de sobreconsumo.”

Hallazgos por módulo

COMPRAS

INVENTARIOS

PRODUCCIÓN

Matriz de verificación

checklist SÍ / NO / NO APLICA

con referencia a clases/métodos

Conclusión de dominio

confirmar si el código:

respeta los SPECS,

no viola decisiones canónicas,

no introduce lógica implícita.

Criterio de aceptación

La auditoría es válida solo si:

No se detecta ningún cálculo oculto.

No se detecta ningún bloqueo por saldo.

No se detecta inferencia de reglas no documentadas.

El reporte no propone soluciones, solo constata hechos.