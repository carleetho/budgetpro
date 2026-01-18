# Estrategia de documentación en 5 etapas

## Contexto y problema
El desarrollo actual fue guiado por prompts de IA (Gemini/ChatGPT), lo que generó una **brecha de contexto** entre el sistema implementado y la **realidad del negocio**.  
El objetivo de este plan es **reconstruir el mapa real del proyecto**: qué hace cada módulo, qué reglas aplica, qué límites existen, qué errores se disparan y qué dolores de negocio resuelve.

## Principios de documentación
- **Primero la verdad del código**: todo debe tener trazabilidad a clases, endpoints, migraciones o tests.
- **Lenguaje de dominio**: usar términos reales (Partidas, APU, Presupuesto, RPC, Orden de Cambio).
- **No suposiciones**: lo no implementado se etiqueta como “No existe”.
- **Orientado a negocio**: cada módulo debe mapearse a dolor/valor.

## Objetivo de esta estrategia
Crear una documentación que permita:
- Entender el **contexto funcional real** del sistema.
- Identificar divergencias entre **código** y **plan de negocio**.
- Consolidar reglas, límites, errores y dependencias.
- Recuperar trazabilidad por módulo/feature.

---

## Etapa 1 — Inventario técnico (mapa del código)
**Objetivo:** Enumerar la estructura real del sistema por módulos y capas.

**Qué documentar:**
- Módulos del backend y frontend.
- Paquetes/clases clave.
- Endpoints, servicios y repositorios.
- Entidades y migraciones.
- Dependencias externas (APIs, servicios, colas, storage).

**Salida esperada:**
- Tabla de módulos → paquetes → responsabilidades.
- Lista de endpoints activos y su propósito.
- Mapa de dependencias técnicas por módulo.

---

## Etapa 2 — Comportamiento real (reglas y límites)
**Objetivo:** Capturar **qué reglas de negocio existen realmente** en el código.

**Qué documentar:**
- Validaciones (incluyendo condiciones de error).
- Estados permitidos y transiciones.
- Reglas de cálculo y límites (ej: exceso de metrado).
- Excepciones y mensajes relevantes.
- Side effects (eventos, auditoría, outbox, notificaciones).

**Salida esperada:**
- Catálogo de reglas de negocio por módulo.
- Matriz de errores y mensajes.
- Tabla de estados y transiciones reales.

---

## Etapa 3 — Flujo de usuario (lo que el usuario puede hacer)
**Objetivo:** Documentar el flujo real del usuario final.

**Qué documentar:**
- Entradas principales (landing, login, registro, etc.).
- Flujos clave por rol.
- Pantallas disponibles y su funcionalidad.
- Integraciones visibles para el usuario.
- Flujos bloqueados o incompletos (pantallas “placeholder”).

**Salida esperada:**
- Mapa de navegación real.
- Flujos críticos (happy path + errores).
- Matriz de rol → capacidades reales.

---

## Etapa 4 — Dolor resuelto (valor de negocio actual)
**Objetivo:** Conectar el código con la propuesta de valor.

**Qué documentar:**
- Qué dolores resuelve cada módulo hoy.
- Qué queda pendiente o no existe aún.
- Supuestos que el sistema está haciendo.
- Métricas del negocio que sí están soportadas vs. faltantes.

**Salida esperada:**
- Tabla: Dolor → Módulo → Evidencia en código.
- Tabla: Métrica → Origen → Disponibilidad (Sí/No).

---

## Etapa 5 — Brecha con plan de negocio
**Objetivo:** Identificar la distancia entre lo desarrollado y lo planeado.

**Qué documentar:**
- Funcionalidades críticas ausentes.
- Reglas del negocio no implementadas.
- Inconsistencias en terminología o modelos.
- Riesgos de datos (inconsistencias, duplicidad, no trazabilidad).

**Salida esperada:**
- Lista priorizada de brechas.
- Recomendaciones de ajuste del roadmap.
- Top 5 riesgos críticos con impacto y dueño.

---

## Método de trabajo
Para cada módulo:
1) Leer clases principales.  
2) Identificar endpoints y casos de uso.  
3) Extraer reglas y errores.  
4) Mapear a dolor de negocio.  
5) Registrar brechas vs plan.

## Evidencias mínimas obligatorias
- Ruta a clase/archivo.
- Endpoint o DTO relacionado.
- Mensaje de error o validación exacta.
- Estado/enum si aplica.

## Cadencia y mantenimiento
- Frecuencia sugerida: **semanal** (o por sprint).
- Cada cambio relevante debe actualizar la BD de reglas y brechas.
- Documentación “viva”: no se considera completa si no se actualiza tras cambios de dominio.

---

## Entregable final esperado
Una documentación viva que permita:
- Reorientar el proyecto hacia el negocio.
- Mantener coherencia entre código y plan.
- Servir como base para backlog y auditoría.

## Formato recomendado en Notion
- Base de datos por etapa (1–4).
- Página resumen con indicadores:
  - % reglas cumplidas
  - # brechas críticas
  - módulos sin lógica

