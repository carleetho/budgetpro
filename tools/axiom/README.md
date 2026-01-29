# AXIOM: Integración de Asistente IA

AXIOM es el sistema de gobernanza arquitectónica de BudgetPro. Este módulo permite integrar las reglas del proyecto directamente con asistentes de IA (como Cursor, Copilot, etc.) mediante la generación automática de archivos de contexto como `.cursorrules`.

## 🚀 Inicio Rápido

### Instalación

Para configurar AXIOM y activar la integración con IA:

```bash
bash tools/axiom/install.sh
```

El script de instalación:

1. Instalará las dependencias necesarias.
2. Verificará tu archivo `axiom.config.yaml`.
3. Te preguntará si deseas generar el archivo `.cursorrules`. **Responde 'y' (sí)**.

### Sincronización Manual

Si modificas las reglas en `axiom.config.yaml`, debes regenerar el archivo de reglas:

```bash
python3 tools/axiom/sync_cursorrules.py
```

Usa `--force` para evitar la confirmación de sobrescritura.

## 📖 Documentación

- [Guía de Configuración](docs/CONFIGURATION_GUIDE.md): Explicación detallada de todas las opciones.
- [Ejemplos](docs/EXAMPLES.md): Casos de uso comunes y configuraciones de referencia.
- [Solución de Problemas](docs/TROUBLESHOOTING.md): Errores comunes y cómo resolverlos.

## 🤔 ¿Por qué usar esto?

Los asistentes de IA son poderosos pero a menudo ignoran el contexto arquitectónico específico del proyecto. AXIOM permite definir una "Fuente de Verdad" (`axiom.config.yaml`) que:

1. **Define la Identidad**: Le dice a la IA qué rol debe asumir (ej. "Arquitecto Senior").
2. **Protege el Código**: Establece zonas rojas (no tocar) y zonas verdes (seguras).
3. **Mantiene la Arquitectura**: Enforce los límites de la Arquitectura Hexagonal.
4. **Preserva el Conocimiento**: Transforma lecciones aprendidas en reglas activas.

## Estructura del Proyecto

```
tools/axiom/
├── axiom.config.yaml       # Fuente de verdad (TU CONFIGURACIÓN)
├── schema/                 # Esquemas de validación
├── templates/              # Plantillas Jinja2 para convertir config a texto
├── lib/                    # Lógica de generación
└── docs/                   # Documentación detallada
```
