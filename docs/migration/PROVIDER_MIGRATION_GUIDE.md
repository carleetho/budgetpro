# Guía de Migración: Proveedores de Texto Libre a Entidad Formal

**Fecha**: 2026-02-15  
**Versión**: 1.0.0  
**Objetivo**: Migrar proveedores de texto libre en la tabla `compra` a la entidad formal `Proveedor`

---

## 1. Resumen Ejecutivo

### ¿Por qué migrar?

La tabla `compra` actualmente almacena proveedores como texto libre (`VARCHAR(200)`), lo que causa:
- **Duplicación de datos**: "CEMEX S.A." vs "Cemex SA" vs "CEMEX" se tratan como proveedores diferentes
- **Falta de validación**: No se puede validar si un proveedor está activo (L-04)
- **Falta de metadatos**: No hay RUC, contacto, dirección
- **Imposibilidad de reportes**: Difícil generar reportes consolidados por proveedor

### ¿Qué cambia?

- ✅ Nueva entidad `Proveedor` con RUC, razón social, estado, contacto, dirección
- ✅ Tabla `orden_compra` usa `proveedor_id` (foreign key) en lugar de texto libre
- ✅ Tabla `compra` **mantiene** el campo `proveedor` (texto libre) para compatibilidad hacia atrás
- ✅ Nueva validación L-04: Solo se puede comprar a proveedores ACTIVOS

### Impacto

- **Sin impacto en producción**: La tabla `compra` mantiene su estructura actual
- **Nuevas órdenes de compra**: Requieren proveedor formal (entidad `Proveedor`)
- **Migración opcional**: Los datos históricos en `compra` pueden migrarse gradualmente

---

## 2. Pre-Migración: Checklist

Antes de comenzar la migración, verifica:

- [ ] **Backup de base de datos** completado
- [ ] **Ambiente de pruebas** configurado con copia de producción
- [ ] **Acceso a base de datos** con permisos de lectura/escritura
- [ ] **Lista de proveedores únicos** extraída de `compra.proveedor`
- [ ] **Datos de proveedores** recopilados (RUC, razón social, contacto, dirección)
- [ ] **Validación de RUC** realizada (verificar con SUNAT o entidad equivalente)
- [ ] **Equipo de operaciones** notificado del cambio

---

## 3. Análisis de Datos Existentes

### 3.1. Extraer Proveedores Únicos

Ejecuta esta consulta SQL para obtener la lista de proveedores únicos:

```sql
-- Extraer proveedores únicos de la tabla compra
SELECT DISTINCT 
    TRIM(UPPER(proveedor)) AS proveedor_normalizado,
    proveedor AS proveedor_original,
    COUNT(*) AS cantidad_compras
FROM compra
WHERE proveedor IS NOT NULL 
  AND TRIM(proveedor) != ''
GROUP BY TRIM(UPPER(proveedor)), proveedor
ORDER BY cantidad_compras DESC;
```

### 3.2. Identificar Duplicados

Ejecuta esta consulta para identificar posibles duplicados:

```sql
-- Identificar posibles duplicados (variaciones del mismo proveedor)
SELECT 
    TRIM(UPPER(proveedor)) AS proveedor_normalizado,
    STRING_AGG(DISTINCT proveedor, ', ') AS variaciones,
    COUNT(DISTINCT proveedor) AS num_variaciones,
    COUNT(*) AS total_compras
FROM compra
WHERE proveedor IS NOT NULL 
  AND TRIM(proveedor) != ''
GROUP BY TRIM(UPPER(proveedor))
HAVING COUNT(DISTINCT proveedor) > 1
ORDER BY num_variaciones DESC;
```

### 3.3. Validar Calidad de Datos

```sql
-- Verificar proveedores con problemas de calidad
SELECT 
    proveedor,
    LENGTH(proveedor) AS longitud,
    COUNT(*) AS cantidad
FROM compra
WHERE proveedor IS NOT NULL
GROUP BY proveedor, LENGTH(proveedor)
HAVING LENGTH(proveedor) > 200  -- Excede límite de VARCHAR(200)
   OR LENGTH(TRIM(proveedor)) = 0  -- Solo espacios
ORDER BY cantidad DESC;
```

---

## 4. Proceso de Migración

### 4.1. Paso 1: Limpiar y Normalizar Datos

**Objetivo**: Preparar los datos para la migración

```sql
-- Crear tabla temporal con proveedores normalizados
CREATE TEMP TABLE proveedores_temp AS
SELECT DISTINCT
    TRIM(UPPER(proveedor)) AS razon_social_normalizada,
    proveedor AS razon_social_original,
    COUNT(*) AS cantidad_compras
FROM compra
WHERE proveedor IS NOT NULL 
  AND TRIM(proveedor) != ''
GROUP BY TRIM(UPPER(proveedor)), proveedor;

-- Verificar resultados
SELECT * FROM proveedores_temp ORDER BY cantidad_compras DESC;
```

### 4.2. Paso 2: Recopilar Información de Proveedores

**Acción manual requerida**: Para cada proveedor en `proveedores_temp`, recopilar:

1. **RUC/NIT**: Número de identificación tributaria (obligatorio, único)
2. **Razón Social**: Nombre oficial de la empresa (normalizado)
3. **Contacto**: Nombre de contacto, teléfono, email
4. **Dirección**: Dirección física de la empresa
5. **Estado**: ACTIVO, INACTIVO, o BLOQUEADO (por defecto: ACTIVO)

**Herramientas útiles**:
- Consulta RUC en SUNAT (Perú) o entidad equivalente
- Base de datos de proveedores existente
- Documentos de compras anteriores

### 4.3. Paso 3: Crear Proveedores mediante API

**Método recomendado**: Usar la API REST para crear proveedores (valida datos automáticamente)

```bash
# Ejemplo: Crear proveedor mediante API
curl -X POST http://localhost:8080/api/v1/proveedores \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "razonSocial": "CEMEX PERU S.A.",
    "ruc": "20100070970",
    "contacto": "Juan Pérez",
    "direccion": "Av. Principal 123, Lima"
  }'
```

**Script de creación masiva** (ejemplo en Python):

```python
import requests
import json

# Configuración
API_BASE_URL = "http://localhost:8080/api/v1/proveedores"
AUTH_TOKEN = "your-jwt-token"

# Lista de proveedores a crear
proveedores = [
    {
        "razonSocial": "CEMEX PERU S.A.",
        "ruc": "20100070970",
        "contacto": "Juan Pérez",
        "direccion": "Av. Principal 123, Lima"
    },
    # ... más proveedores
]

headers = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {AUTH_TOKEN}"
}

for proveedor in proveedores:
    response = requests.post(API_BASE_URL, json=proveedor, headers=headers)
    if response.status_code == 201:
        print(f"✅ Proveedor creado: {proveedor['razonSocial']}")
    else:
        print(f"❌ Error al crear {proveedor['razonSocial']}: {response.text}")
```

### 4.4. Paso 4: Crear Proveedores mediante SQL (Alternativa)

**⚠️ ADVERTENCIA**: Este método omite validaciones de dominio. Usar solo si es necesario.

```sql
-- Script SQL para crear proveedores (requiere datos recopilados manualmente)
-- IMPORTANTE: Reemplazar los valores con datos reales

INSERT INTO proveedor (
    id,
    razon_social,
    ruc,
    estado,
    contacto,
    direccion,
    version,
    created_at,
    updated_at,
    created_by,
    updated_by
)
VALUES
    (
        gen_random_uuid(),  -- PostgreSQL
        'CEMEX PERU S.A.',
        '20100070970',
        'ACTIVO',
        'Juan Pérez',
        'Av. Principal 123, Lima',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000000',  -- Usuario sistema
        '00000000-0000-0000-0000-000000000000'
    ),
    -- Agregar más proveedores aquí
    (
        gen_random_uuid(),
        'HOLCIM PERU S.A.',
        '20100070971',
        'ACTIVO',
        'María González',
        'Av. Industrial 456, Lima',
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000000'
    );

-- Verificar proveedores creados
SELECT id, razon_social, ruc, estado FROM proveedor ORDER BY razon_social;
```

### 4.5. Paso 5: Mapear Proveedores Existentes (Opcional)

Si deseas migrar datos históricos de `compra` a usar `proveedor_id`:

```sql
-- Crear tabla de mapeo proveedor_texto -> proveedor_id
CREATE TEMP TABLE mapeo_proveedores AS
SELECT DISTINCT
    c.proveedor AS proveedor_texto,
    p.id AS proveedor_id,
    p.razon_social,
    p.ruc
FROM compra c
INNER JOIN proveedor p ON TRIM(UPPER(c.proveedor)) = TRIM(UPPER(p.razon_social))
WHERE c.proveedor IS NOT NULL;

-- Verificar mapeo
SELECT * FROM mapeo_proveedores;

-- NOTA: La tabla compra mantiene el campo proveedor (texto libre) para compatibilidad.
-- No es necesario migrar datos históricos a menos que se requiera.
```

---

## 5. Validación Post-Migración

### 5.1. Verificar Proveedores Creados

```sql
-- Verificar cantidad de proveedores creados
SELECT 
    estado,
    COUNT(*) AS cantidad
FROM proveedor
GROUP BY estado;

-- Verificar proveedores sin RUC
SELECT id, razon_social, ruc 
FROM proveedor 
WHERE ruc IS NULL OR TRIM(ruc) = '';

-- Verificar duplicados de RUC
SELECT ruc, COUNT(*) AS cantidad
FROM proveedor
GROUP BY ruc
HAVING COUNT(*) > 1;
```

### 5.2. Probar Creación de Orden de Compra

```bash
# Crear orden de compra con proveedor formal
curl -X POST http://localhost:8080/api/v1/ordenes-compra \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "proyectoId": "550e8400-e29b-41d4-a716-446655440000",
    "proveedorId": "<proveedor-id-creado>",
    "fecha": "2024-02-15",
    "condicionesPago": "30 días crédito",
    "detalles": [
      {
        "partidaId": "770e8400-e29b-41d4-a716-446655440000",
        "descripcion": "Cemento Portland",
        "cantidad": 100.00,
        "unidad": "KG",
        "precioUnitario": 0.50
      }
    ]
  }'
```

### 5.3. Validar Regla L-04

```bash
# Intentar crear orden con proveedor INACTIVO (debe fallar)
# 1. Crear proveedor INACTIVO
# 2. Intentar crear orden de compra
# 3. Verificar que retorna error 422 con mensaje L-04
```

---

## 6. Guía de Limpieza de Datos

### 6.1. Normalización de Nombres

**Problema común**: Variaciones del mismo nombre

```
"CEMEX S.A." → "CEMEX PERU S.A."
"Cemex SA" → "CEMEX PERU S.A."
"CEMEX" → "CEMEX PERU S.A."
```

**Solución**: Crear un solo proveedor con el nombre oficial y mapear variaciones.

### 6.2. Validación de RUC

**Formato esperado**: 
- Perú: 11 dígitos (ej: `20100070970`)
- Otros países: Ajustar según formato local

**Validación**:
- Consultar RUC en SUNAT (Perú) o entidad equivalente
- Verificar que el RUC corresponde a la razón social
- Marcar proveedores con RUC inválido como BLOQUEADO

### 6.3. Manejo de Proveedores Desconocidos

**Estrategia**:
1. **Proveedores con RUC conocido**: Crear proveedor formal
2. **Proveedores sin RUC**: 
   - Opción A: Crear con RUC temporal (ej: `TEMP-001`, `TEMP-002`)
   - Opción B: Marcar como BLOQUEADO hasta obtener RUC real
3. **Proveedores históricos**: Mantener en `compra.proveedor` (texto libre)

---

## 7. Script SQL de Backfill (Opcional)

Script completo para extraer y crear proveedores automáticamente:

```sql
-- ============================================
-- SCRIPT DE BACKFILL DE PROVEEDORES
-- ============================================
-- ADVERTENCIA: Este script requiere datos adicionales (RUC, contacto, dirección)
-- que deben recopilarse manualmente antes de ejecutar.

-- Paso 1: Crear tabla temporal con proveedores únicos
CREATE TEMP TABLE proveedores_backfill AS
SELECT DISTINCT
    TRIM(UPPER(proveedor)) AS razon_social_normalizada,
    proveedor AS razon_social_original,
    COUNT(*) AS cantidad_compras,
    MIN(fecha) AS primera_compra,
    MAX(fecha) AS ultima_compra
FROM compra
WHERE proveedor IS NOT NULL 
  AND TRIM(proveedor) != ''
GROUP BY TRIM(UPPER(proveedor)), proveedor;

-- Paso 2: Generar INSERT statements (requiere datos adicionales)
-- NOTA: Reemplazar 'TBD' con datos reales recopilados manualmente
SELECT 
    'INSERT INTO proveedor (id, razon_social, ruc, estado, contacto, direccion, version, created_at, updated_at, created_by, updated_by) VALUES (' ||
    'gen_random_uuid(), ' ||
    quote_literal(razon_social_original) || ', ' ||
    quote_literal('TBD') || ', ' ||  -- RUC: Reemplazar con valor real
    quote_literal('ACTIVO') || ', ' ||
    quote_literal('TBD') || ', ' ||  -- Contacto: Reemplazar con valor real
    quote_literal('TBD') || ', ' ||  -- Dirección: Reemplazar con valor real
    '0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ' ||
    quote_literal('00000000-0000-0000-0000-000000000000') || ', ' ||
    quote_literal('00000000-0000-0000-0000-000000000000') ||
    ');' AS insert_statement
FROM proveedores_backfill
ORDER BY cantidad_compras DESC;

-- Paso 3: Ejecutar los INSERT statements generados (después de completar datos)
-- Paso 4: Verificar proveedores creados
SELECT COUNT(*) AS total_proveedores FROM proveedor;
```

---

## 8. Plan de Rollback

Si es necesario revertir la migración:

### 8.1. Eliminar Proveedores Creados

```sql
-- ⚠️ ADVERTENCIA: Solo ejecutar si no hay órdenes de compra asociadas
-- Verificar dependencias primero
SELECT COUNT(*) 
FROM orden_compra 
WHERE proveedor_id IN (SELECT id FROM proveedor);

-- Si no hay dependencias, eliminar proveedores
DELETE FROM proveedor WHERE id IN (
    SELECT id FROM proveedor 
    WHERE created_at > '2024-02-15'  -- Fecha de inicio de migración
);
```

### 8.2. Restaurar Backup

Si la migración causó problemas mayores:

```bash
# Restaurar backup completo de base de datos
pg_restore -d budgetpro_db backup_pre_migration.dump
```

---

## 9. Checklist de Migración

### Pre-Migración
- [ ] Backup de base de datos completado
- [ ] Ambiente de pruebas configurado
- [ ] Lista de proveedores únicos extraída
- [ ] Datos de proveedores recopilados (RUC, contacto, dirección)
- [ ] Validación de RUC realizada

### Migración
- [ ] Proveedores creados mediante API o SQL
- [ ] Verificación de proveedores creados (sin duplicados, RUC válidos)
- [ ] Prueba de creación de orden de compra exitosa
- [ ] Validación de regla L-04 funcionando

### Post-Migración
- [ ] Documentación actualizada
- [ ] Equipo de operaciones notificado
- [ ] Monitoreo de errores configurado
- [ ] Plan de rollback documentado

---

## 10. Soporte y Contacto

Para preguntas o problemas durante la migración:

- **Documentación**: Ver `docs/canonical/modules/COMPRAS_MODULE_CANONICAL.md`
- **API Documentation**: `http://localhost:8080/swagger-ui.html`
- **Issues**: Crear issue en el repositorio
- **Contacto**: Equipo de desarrollo BudgetPro

---

## 11. Referencias

- **Especificación de API**: `backend/src/main/resources/api-docs/orden-compra-api.yaml`
- **Documentación Canónica**: `docs/canonical/modules/COMPRAS_MODULE_CANONICAL.md`
- **Reglas de Negocio**: Ver sección "Invariants" en documentación canónica
- **Migración de Base de Datos**: `backend/src/main/resources/db/migration/V20__create_proveedor_and_orden_compra.sql`

---

**Última Actualización**: 2026-02-15  
**Versión**: 1.0.0  
**Autor**: BudgetPro Development Team
