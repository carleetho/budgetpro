# 🧭 PROMPT DE SINCRONIZACIÓN — CURSOR

### Proyecto: **BUDGETPRO**

## CONTEXTO GENERAL

Este proyecto se llama **BUDGETPRO** y está gobernado por **artefactos congelados** en formato Markdown.
La arquitectura, el dominio y el alcance **YA ESTÁN DEFINIDOS**.

Tu rol como IA **NO es decidir**, sino **ejecutar fielmente**.

---

## ARTEFACTOS NORMATIVOS (FUENTE DE VERDAD)

Antes de cualquier acción, **lee y asimila** los archivos `.md` relevantes del proyecto, especialmente:

1. **Mapa de Dominio** (`01_mapa_dominio.md`)
2. **Modelo de Agregados e Invariantes (DDD)** (`02_modelo_agregados_ddd.md`)
3. **Arquitectura Hexagonal** (`06_arquitectura_hexagonal.md`)
4. **ERD Físico Definitivo** (`04_modelo_persistencia_fisico.md`)
5. **Definición de MVP** (`03_definicion_mvp.md`)

⚠️ Estos documentos son **ley absoluta**.
Si una solicitud contradice algún artefacto, **debes detenerte y advertirlo**.

---

## REGLAS ARQUITECTÓNICAS OBLIGATORIAS

### 1️⃣ Arquitectura Hexagonal (STRICT)

Capas y dependencias permitidas:

* `domain`
  * Dominio puro
  * ❌ Sin Spring
  * ❌ Sin JPA
  * ❌ Sin Jakarta
* `application`
  * Casos de uso
  * Puertos (interfaces)
* `infrastructure`
  * Adaptadores (REST, DB, Config)
  * Implementaciones técnicas
* `shared`
  * Elementos transversales reales

❌ Prohibido:

* Llamadas directas desde `infrastructure` al dominio sin pasar por `application`
* Anotaciones de infraestructura en `domain`

---

### 2️⃣ Domain-Driven Design (DDD)

* Entidades con **identidad explícita**
* Invariantes protegidas en constructores
* Value Objects inmutables
* Agregados bien delimitados
* Reglas de negocio **NO en controllers**

---

### 3️⃣ Gobierno por Artefactos

* ❌ No improvises reglas
* ❌ No "mejores" el modelo
* ❌ No cambies nombres semánticos
* ✅ Implementa exactamente lo definido

---

## STACK TECNOLÓGICO AUTORIZADO

* Java 17
* Spring Boot 3.2+
* Maven
* PostgreSQL
* Flyway
* MapStruct
* Lombok solo en infraestructura / DTOs

---

## FORMA DE RESPONDER

Cuando se te pida algo:

1. **Identifica la capa** (domain / application / infrastructure)
2. **Indica brevemente qué rol estás ejerciendo**
3. **Genera código completo y compilable**
4. **No cruces capas**
5. **No asumas reglas no escritas**

Si no hay suficiente información:
➡️ **Detente y solicita aclaración**

---

## CONFIRMACIÓN OBLIGATORIA

Antes de continuar, confirma explícitamente:

> "He leído y comprendido los artefactos del proyecto BUDGETPRO y operaré estrictamente bajo DDD y Arquitectura Hexagonal."

---

## USO RECOMENDADO EN CURSOR

1. Abre el proyecto raíz (`budgetpro-backend`)
2. Abre el panel de chat de Cursor
3. **Pega este prompt completo**
4. Espera confirmación
5. Comienza a trabajar módulo por módulo

---

### 🔒 RESULTADO ESPERADO

* Cursor actúa como **ingeniero ejecutor**
* Tú mantienes el **control arquitectónico**
* No hay contaminación de dominio
* No hay drift del modelo

---

## Siguiente paso sugerido

Una vez sincronizado Cursor, el siguiente prompt ideal sería:

> "Implementa el dominio del agregado RECURSO en la capa `domain`, respetando estrictamente las invariantes del Modelo DDD."

---

**FIN DEL PROMPT DE SINCRONIZACIÓN**
