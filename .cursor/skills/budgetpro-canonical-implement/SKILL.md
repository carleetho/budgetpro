---
name: budgetpro-canonical-implement
description: Implementa o modifica comportamiento de negocio en BudgetPro siguiendo notebooks canónicos y AI_AGENT_PROTOCOL. Usar cuando el usuario pida feature, invariante, use case, API de dominio o cambio de contrato entre capas.
---

# Implementación canónica (Modo B)

## Objetivo

Reducir invención: todo comportamiento de negocio debe alinearse con la especificación canónica y con la arquitectura acordada.

## Pasos (orden fijo)

1. **Confirmar módulo** con el usuario si no es obvio por el path o la petición (Presupuesto, RRHH, EVM, etc.).
2. **Leer con la herramienta de lectura** (no asumir contenido):
   - `docs/canonical/AI_AGENT_PROTOCOL.md`
   - `docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md`
   - `docs/canonical/modules/<MODULO>_MODULE_CANONICAL.md`
3. **Índice opcional**: `.cursor/canonical_context.md` para saltar a Data Model, Integration o Invariants si la tarea lo exige.
4. **Escanear** el notebook cargado en busca de `[AMBIGUITY_DETECTED]`. Si aparece: **parar** y pedir aclaración con cita de sección.
5. **Invariantes**: si necesitas una regla que no está escrita: **parar** y pedirla (no inventar).
6. **Conflicto código vs notebook**: declarar en el chat; objetivo de cambio = notebook salvo instrucción explícita del usuario en el hilo.
7. **Implementar** con el menor blast radius razonable; respetar hexágono (dominio sin frameworks).
8. **Verificar** con tests existentes o nuevos mínimos; si el proyecto usa AXIOM en el flujo, ejecutar `./axiom.sh --dry-run` cuando aplique antes de dar por cerrada la tarea.

## Salida esperada

- Resumen de archivos tocados y por qué.
- Si hubo conflicto canónico vs código: la frase explícita acordada en el protocolo.
- Sin afirmaciones sobre el comportamiento del sistema sin haber leído código o tests que lo respalden.
