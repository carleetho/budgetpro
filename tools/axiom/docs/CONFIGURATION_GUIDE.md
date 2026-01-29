# Guía de Configuración de AXIOM

El archivo `axiom.config.yaml` es el corazón de la gobernanza arquitectónica. Controla cómo se comportan los asistentes de IA.

## Ubicación

Por defecto: `tools/axiom/axiom.config.yaml`

El esquema es validado contra `tools/axiom/schema/axiom-config.schema.json`.

---

## Estructura del Archivo

### 1. Sistema (`system`)

Define la "personalidad" y prioridades de alto nivel.

```yaml
system:
  role: "Arquitecto Senior experto en..."
  priorities:
    - "Calidad sobre velocidad"
    - "Seguridad ante todo"
```

| Campo        | Descripción                                                                                |
| ------------ | ------------------------------------------------------------------------------------------ |
| `role`       | Rol que la IA debe adoptar. Sé específico con tecnologías y senioridad.                    |
| `priorities` | Lista ordenada de valores fundamentales. Ayuda a la IA a tomar decisiones ante conflictos. |

### 2. Axiomas (`axioms`)

Reglas concretas e inquebrantables.

#### Zonas de Protección (`protection_zones`)

Define qué tan seguro es modificar ciertos archivos.

```yaml
protection_zones:
  - name: "Núcleo Crítico"
    level: "RED"
    paths: ["backend/domain/**"]
    description: "Lógica pura. No usar frameworks."
```

| Nivel    | Significado        | Comportamiento Esperado                                         |
| -------- | ------------------ | --------------------------------------------------------------- |
| `RED`    | Protegido / Legacy | **Solo Lectura**. Requiere `override` explícito para modificar. |
| `YELLOW` | Precaución         | Modificar con cuidado. Requiere revisión exhaustiva.            |
| `GREEN`  | Seguro             | Desarrollo normal.                                              |

#### Prohibiciones (`prohibitions`)

Lo que la IA **NUNCA** debe hacer.

```yaml
prohibitions:
  - "Nunca dejes métodos vacíos."
  - "No uses System.out.println."
```

#### Límites Hexagonales (`hexagonal_boundaries`)

Controla la dirección de las dependencias (imports).

```yaml
hexagonal_boundaries:
  permitted:
    - from: "infrastructure"
      to: "domain"
  forbidden:
    - from: "domain"
      to: "infrastructure"
```

#### Palabras Clave de Excepción (`override_keywords`)

Define "contraseñas" semánticas que permiten a los humanos autorizar violaciones a las reglas.

```yaml
override_keywords:
  EMERGENCY_FIX: "Bypass de reglas solo para incidentes de producción."
```

#### Contexto Histórico (`historical_context`)

Alimenta a la IA con memoria institucional para evitar repetir errores.

```yaml
historical_context:
  baseline_violations:
    - "El incidente de 2024 donde se borró la DB por un script mal hecho."
```
