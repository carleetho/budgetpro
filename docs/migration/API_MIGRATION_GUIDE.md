# API Migration Guide - REQ-4 Domain Validator

**Fecha**: 2026-01-23  
**PR**: #4  
**Requerimiento**: REQ-4

## Resumen

Este documento describe los cambios en los endpoints de API introducidos en el PR #4 (REQ-4: Domain Validator). Los cambios son principalmente aditivos, con una nueva estructura de endpoints para operaciones de APU.

---

## Cambios en Endpoints

### 1. Endpoint de Actualización de Rendimiento de APU

#### Antes (No existía)
No existía un endpoint dedicado para actualizar el rendimiento de un APU. Las actualizaciones requerían modificar el APU completo.

#### Después (Nuevo)
```
PUT /api/v1/apu/{apuSnapshotId}/rendimiento
```

**Descripción**: Actualiza el rendimiento vigente de un APU y recalcula automáticamente los costos afectados mediante recálculo en cascada.

**Request Body**:
```json
{
  "nuevoRendimiento": 30.00,
  "usuarioId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response**: `204 No Content`

**Validaciones**:
- `nuevoRendimiento`: Debe ser positivo (`@DecimalMin("0.0001")`)
- `usuarioId`: Requerido (`@NotNull`)

**Comportamiento**:
1. Actualiza el rendimiento del APUSnapshot
2. Recalcula automáticamente todos los costos afectados
3. Si el presupuesto está aprobado, actualiza el hash de ejecución
4. Si hay dependencias (ej: EQUIPO_HERRAMIENTA depende de MANO_OBRA), se recalculan en cascada

#### Endpoint de Creación (Sin cambios)
```
POST /api/v1/partidas/{partidaId}/apu
```
Este endpoint permanece sin cambios y sigue funcionando como antes.

---

## Guía de Migración

### Para Clientes Existentes

#### Escenario 1: Actualizar Rendimiento de APU

**Antes** (si existía lógica personalizada):
```java
// No había endpoint directo, requería modificar APU completo
PUT /api/v1/partidas/{partidaId}/apu/{apuId}
{
  "rendimiento": 30.00,
  // ... todos los demás campos
}
```

**Después**:
```java
// Usar nuevo endpoint dedicado
PUT /api/v1/apu/{apuSnapshotId}/rendimiento
{
  "nuevoRendimiento": 30.00,
  "usuarioId": "uuid"
}
```

**Ventajas**:
- Solo requiere enviar el rendimiento nuevo
- Recalcula automáticamente costos en cascada
- Más eficiente y seguro

#### Escenario 2: Crear Nuevo APU

**Sin cambios**: Continúa usando:
```
POST /api/v1/partidas/{partidaId}/apu
```

---

## Compatibilidad

### Endpoints que NO cambian
- ✅ `POST /api/v1/partidas/{partidaId}/apu` - Crear APU
- ✅ `GET /api/v1/presupuestos/{presupuestoId}` - Consultar presupuesto
- ✅ `GET /api/v1/presupuestos/{presupuestoId}/explosion-insumos` - Explosión de insumos
- ✅ Todos los demás endpoints existentes

### Endpoints nuevos
- 🆕 `PUT /api/v1/apu/{apuSnapshotId}/rendimiento` - Actualizar rendimiento

### Endpoints deprecados
- ❌ Ninguno

---

## Ejemplos de Migración

### Ejemplo 1: Actualizar Rendimiento desde Frontend

**Código Antes** (si existía):
```typescript
// Hypothetical - no existía antes
async function updateApuPerformance(apuId: string, newPerformance: number) {
  // Tendría que obtener APU completo, modificar y enviar todo
  const apu = await fetch(`/api/v1/partidas/${partidaId}/apu/${apuId}`);
  const updatedApu = { ...apu, rendimiento: newPerformance };
  await fetch(`/api/v1/partidas/${partidaId}/apu/${apuId}`, {
    method: 'PUT',
    body: JSON.stringify(updatedApu)
  });
}
```

**Código Después**:
```typescript
async function updateApuPerformance(apuSnapshotId: string, newPerformance: number, userId: string) {
  await fetch(`/api/v1/apu/${apuSnapshotId}/rendimiento`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      nuevoRendimiento: newPerformance,
      usuarioId: userId
    })
  });
}
```

### Ejemplo 2: Integración con Backend

**Java/Spring Boot**:
```java
@RestController
public class ApuClientController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void updateApuPerformance(UUID apuSnapshotId, BigDecimal nuevoRendimiento, UUID usuarioId) {
        ActualizarRendimientoRequest request = new ActualizarRendimientoRequest(
            nuevoRendimiento,
            usuarioId
        );
        
        restTemplate.put(
            "http://api.budgetpro.com/api/v1/apu/{apuSnapshotId}/rendimiento",
            request,
            apuSnapshotId
        );
    }
}
```

---

## Consideraciones Importantes

### 1. Identificadores
- **apuSnapshotId**: El nuevo endpoint usa `apuSnapshotId` (UUID del APUSnapshot)
- **partidaId**: El endpoint de creación sigue usando `partidaId`

### 2. Recalculación Automática
El nuevo endpoint realiza recalculación automática en cascada:
- MATERIAL → MANO_OBRA → EQUIPO_MAQUINA → EQUIPO_HERRAMIENTA
- No es necesario recalcular manualmente

### 3. Integridad de Presupuestos Aprobados
Si el presupuesto está aprobado (congelado):
- El hash de ejecución se actualiza automáticamente
- Se mantiene la integridad del presupuesto

### 4. Validaciones
El endpoint valida:
- Rendimiento positivo
- APUSnapshot existe
- Usuario existe (si se requiere validación de usuario)

---

## Testing

### Pruebas Recomendadas

1. **Actualizar rendimiento exitosamente**:
```bash
curl -X PUT http://localhost:8080/api/v1/apu/{apuSnapshotId}/rendimiento \
  -H "Content-Type: application/json" \
  -d '{
    "nuevoRendimiento": 30.00,
    "usuarioId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

2. **Validar error con rendimiento negativo**:
```bash
curl -X PUT http://localhost:8080/api/v1/apu/{apuSnapshotId}/rendimiento \
  -H "Content-Type: application/json" \
  -d '{
    "nuevoRendimiento": -10.00,
    "usuarioId": "550e8400-e29b-41d4-a716-446655440000"
  }'
# Esperado: 400 Bad Request
```

3. **Validar error con APU inexistente**:
```bash
curl -X PUT http://localhost:8080/api/v1/apu/00000000-0000-0000-0000-000000000000/rendimiento \
  -H "Content-Type: application/json" \
  -d '{
    "nuevoRendimiento": 30.00,
    "usuarioId": "550e8400-e29b-41d4-a716-446655440000"
  }'
# Esperado: 404 Not Found
```

---

## Rollback Plan

Si es necesario revertir estos cambios:

1. **Endpoints**: El nuevo endpoint puede ser deshabilitado sin afectar otros
2. **Funcionalidad**: La creación de APUs no se ve afectada
3. **Datos**: No hay cambios en el esquema de base de datos relacionados con este endpoint

---

## Soporte

Para preguntas o problemas con la migración:
- Revisar documentación: `docs/CALCULO_DINAMICO.md`
- Crear issue en el repositorio
- Contactar al equipo de desarrollo

---

**Última Actualización**: 2026-01-23  
**Versión**: 1.0.0
