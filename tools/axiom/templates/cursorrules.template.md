# BUDGETPRO - REGLAS MAESTRAS DE COMPORTAMIENTO PARA LA IA

# 1. ROL Y PERSONALIDAD

- Actúa como: {{ system.role }}
- TUS PRIORIDADES SON:
  {% for priority in system.priorities %}
  - {{ priority }}
    {% endfor %}

# 2. PROHIBICIONES ESTRICTAS (LO QUE NUNCA DEBES HACER)

{% for prohibition in axioms.prohibitions %}

- ⛔ {{ prohibition }}
  {% endfor %}

# 3. ZONAS DE PROTECCIÓN (PROTECTION ZONES)

Estas reglas definen dónde y cómo puedes modificar código.

{% for zone in axioms.protection_zones %}

### {{ zone.name }} [NIVEL: {{ zone.level }}]

- **Descripción**: {{ zone.description }}
- **Archivos**:
  {% for path in zone.paths %}
  - `{{ path }}`
    {% endfor %}
    {% if zone.level == 'RED' %}
- **REGLA**: NO MODIFICAR sin autorización explícita o override. Solo lectura.
  {% elif zone.level == 'YELLOW' %}
- **REGLA**: MODIFICAR CON PRECAUCIÓN. Requiere validación exhaustiva.
  {% elif zone.level == 'GREEN' %}
- **REGLA**: MODIFICACIÓN PERMITIDA. Sigue las prácticas estándar.
  {% endif %}
  {% endfor %}

# 4. LÍMITES ARQUITECTÓNICOS (HEXAGONAL BOUNDARIES)

Respeta estrictamente las dependencias entre capas.

**PERMITIDO (✅):**
{% for flow in axioms.hexagonal_boundaries.permitted %}

- `{{ flow.from }}` → `{{ flow.to }}`
  {% endfor %}

**PROHIBIDO (❌):**
{% for flow in axioms.hexagonal_boundaries.forbidden %}

- `{{ flow.from }}` → `{{ flow.to }}`
  {% endfor %}

# 5. PALABRAS CLAVE DE EXCEPCIÓN (OVERRIDES)

Usa estas palabras clave en commits o comentarios cuando necesites excepciones autorizadas.

{% for keyword, context in axioms.override_keywords.items() %}

- **{{ keyword }}**: {{ context }}
  {% endfor %}

# 6. PROTOCOLO DE OPERACIÓN

Antes de cada modificación:

1. Identifica en qué ZONA DE PROTECCIÓN estás trabajando.
2. Verifica si tu cambio viola alguna PROHIBICIÓN.
3. Asegura que las dependencias respeten los LÍMITES ARQUITECTÓNICOS.
4. Si necesitas violar una regla, busca un OVERRIDE válido.

# 7. CONTEXTO HISTÓRICO (LECCIONES APRENDIDAS)

Aprende de los errores del pasado para no repetirlos.

**Violaciones previas a evitar:**
{% for violation in axioms.historical_context.baseline_violations %}

- {{ violation }}
  {% endfor %}

**Lecciones clave:**
{% for lesson in axioms.historical_context.lessons_learned %}

- {{ lesson }}
  {% endfor %}
