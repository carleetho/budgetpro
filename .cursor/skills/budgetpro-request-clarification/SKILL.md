---
name: budgetpro-request-clarification
description: Convierte peticiones ambiguas o escuetas en criterios de aceptación mediante preguntas con opciones a/b/c/d/otro, estilo BrainGrid. Usar cuando falte módulo, alcance, riesgo, entregable o definición de hecho.
---

# Clarificación de petición (BrainGrid-style)

## Cuándo activar

Activa este flujo si **cualquiera** de estos huecos existe (no implementes ni refactorices masivo hasta aclarar):

- Objetivo o **alcance** impreciso (“mejora X”, “arregla RRHH” sin síntoma).
- **Módulo** / bounded context no identificable.
- **Modo** mezclado (¿auditoría as-built vs cambio de negocio?).
- **Entregable** incierto (solo código, tests, docs, PR, migración).
- **Criterios de aceptación** o “definición de hecho” ausentes.
- **Riesgo** o entorno (prod vs solo tests) no dicho.

Si el usuario **rechaza** preguntas y pide actuar ya: resume **asunciones explícitas** en viñetas y pide **una confirmación** (“¿procedo con estas asunciones? Sí/No”).

## Formato de cada pregunta

Para **cada** pregunta usa exactamente este esqueleto:

1. **Pregunta** en una frase clara.
2. Opciones numeradas con letras:
   - **a)** …
   - **b)** …
   - **c)** …
   - **d)** …
   - **otro)** _(pide una sola frase o bullet si eligen otro)_

Máximo **5–7 preguntas** en la primera ronda (agrupa temas relacionados en una pregunta si hace falta).

## Banco sugerido (elige las relevantes; no las repitas todas)

1. **Tipo de trabajo:** ¿Modo A (¿qué hay implementado?) o Modo B (cambiar comportamiento / contratos)?  
2. **Módulo canónico** principal (Presupuesto, RRHH, EVM, …) o “transversal”.  
3. **Prioridad:** corrección rápida vs solución durable vs solo análisis.  
4. **Entregables:** código + tests, solo tests, solo informe, actualización canónica autorizada, etc.  
5. **Límites:** archivos/capas prohibidas, no tocar dominio, ventana de tiempo, compatibilidad hacia atrás.  
6. **Verificación:** ¿qué prueba o comando demuestra “listo”? (p. ej. `./axiom.sh --dry-run`, suite concreta).  
7. **Contexto adicional:** issue, ticket, captura, rama base.

## Cierre del ciclo

Tras las respuestas del usuario:

1. Redacta **criterios de aceptación** en 3–8 viñetas verificables (“Dado… cuando… entonces…” o checklist).
2. Indica si el trabajo cae en **Modo A** o **Modo B** y el **módulo** asumido.
3. Pide **confirmación explícita**: “¿Confirmas estos criterios antes de que implemente / audite?”.

Solo entonces continúa con el skill adecuado (`budgetpro-canonical-implement`, `budgetpro-as-built-audit`, etc.) o con herramientas.

## Coherencia con gobernanza

- Si en Modo B falta spec en canónicos tras clarificar intención: sigue aplicando **STOP / ASK** según `docs/canonical/AI_AGENT_PROTOCOL.md`.
- No uses este skill para **posponer indefinidamente**: si con dos preguntas basta, no alargues.
