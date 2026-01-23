# Quick Start - Domain Validator

## 🚀 Inicio Rápido

### 1. Ejecutar Validación

```bash
cd tools/domain-validator
./validate.sh --format json --output report.json
```

### 2. Analizar Resultados

```bash
./analyze-report.sh report.json
```

### 3. Revisar Plan de Acción

```bash
# Plan inmediato (sprint actual)
cat PLAN_ACCION_INMEDIATO.md

# Plan estratégico completo
cat PLAN_ESTRATEGICO_VALIDACION.md
```

## 📋 Comandos Esenciales

```bash
# Validación básica
./validate.sh

# Validación estricta (advertencias también bloquean)
./validate.sh --strict

# Validación con reporte JSON
./validate.sh --format json --output report.json

# Análisis del reporte
./analyze-report.sh report.json
```

## 🎯 Próximos Pasos

1. **Revisar falsos positivos identificados** en `PLAN_ACCION_INMEDIATO.md`
2. **Corregir detección** de Billetera, EstadoPresupuesto, NaturalezaGasto
3. **Completar módulos baseline** (proyecto, presupuesto, tiempo)
4. **Re-validar** para medir progreso

## 📚 Documentación

- **README.md**: Documentación completa
- **PLAN_ESTRATEGICO_VALIDACION.md**: Plan a largo plazo
- **PLAN_ACCION_INMEDIATO.md**: Acciones prioritarias
- **RESUMEN_ANALISIS_REPORTE.md**: Resumen del análisis actual
