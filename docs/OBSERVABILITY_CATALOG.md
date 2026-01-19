# Observabilidad de Integración de Catálogos

## Resumen

Este documento describe las métricas y logs estructurados implementados para monitorear la integración con catálogos externos (CAPECO, MOCK, etc.) y las operaciones de snapshots.

## Métricas

Todas las métricas están disponibles en el endpoint `/actuator/prometheus` y pueden ser consumidas por Prometheus/Grafana.

### Métricas de API de Catálogo

#### `catalog.api.requests.total`
**Tipo:** Counter  
**Descripción:** Contador total de requests a APIs de catálogo  
**Tags:**
- `source`: Fuente del catálogo (ej: "CAPECO", "MOCK")
- `operation`: Operación realizada ("fetchRecurso", "fetchAPU", "searchRecursos", "isRecursoActive")
- `status`: Estado de la operación ("success", "error")
- `error_type`: (solo en errores) Tipo de excepción

**Ejemplo de consulta Prometheus:**
```promql
# Tasa de requests exitosos por segundo
rate(catalog_api_requests_total{status="success"}[5m])

# Tasa de errores por segundo
rate(catalog_api_requests_total{status="error"}[5m])

# Error rate por catálogo
sum(rate(catalog_api_requests_total{status="error"}[5m])) by (source) 
/ 
sum(rate(catalog_api_requests_total[5m])) by (source)
```

#### `catalog.api.latency`
**Tipo:** Timer (histograma)  
**Descripción:** Tiempo de respuesta de las APIs de catálogo  
**Tags:**
- `source`: Fuente del catálogo
- `operation`: Operación realizada

**Percentiles disponibles:** p50, p95, p99

**Ejemplo de consulta Prometheus:**
```promql
# Latencia p95 por operación
histogram_quantile(0.95, rate(catalog_api_latency_bucket[5m]))

# Latencia promedio
rate(catalog_api_latency_sum[5m]) / rate(catalog_api_latency_count[5m])
```

### Métricas de Cache

#### `catalog.cache.requests`
**Tipo:** Counter  
**Descripción:** Contador de accesos al cache (hits y misses)  
**Tags:**
- `source`: Fuente del catálogo
- `result`: Resultado del acceso ("hit", "miss")

**Ejemplo de consulta Prometheus:**
```promql
# Cache hit rate
sum(rate(catalog_cache_requests_total{result="hit"}[5m])) by (source)
/
sum(rate(catalog_cache_requests_total[5m])) by (source)
```

### Métricas de Snapshots

#### `snapshot.creation.total`
**Tipo:** Counter  
**Descripción:** Contador de snapshots creados  
**Tags:**
- `source`: Fuente del catálogo
- `insumos_count`: Cantidad de insumos en el snapshot

**Ejemplo de consulta Prometheus:**
```promql
# Snapshots creados por minuto
rate(snapshot_creation_total[1m])
```

#### `snapshot.creation.duration`
**Tipo:** Timer (histograma)  
**Descripción:** Duración de creación de snapshots  
**Tags:**
- `source`: Fuente del catálogo

**Percentiles disponibles:** p50, p95, p99

**Ejemplo de consulta Prometheus:**
```promql
# Tiempo promedio de creación
rate(snapshot_creation_duration_sum[5m]) / rate(snapshot_creation_duration_count[5m])
```

#### `snapshot.rendimiento.overrides`
**Tipo:** Counter  
**Descripción:** Contador de modificaciones de rendimiento  
**Tags:**
- `source`: Fuente del catálogo

**Ejemplo de consulta Prometheus:**
```promql
# Modificaciones de rendimiento por hora
rate(snapshot_rendimiento_overrides_total[1h])
```

## Logs Estructurados

Todos los logs se emiten en formato estructurado (JSON cuando se configura) e incluyen correlation IDs para rastreo de requests.

### Formato de Logs

Los logs incluyen los siguientes campos comunes:
- `component`: Componente que emite el log ("catalog-adapter", "snapshot-service", "catalog-cache")
- `action`: Acción realizada
- `catalog_source`: Fuente del catálogo
- `correlation_id`: ID único para rastreo de requests
- `duration_ms`: Duración de la operación en milisegundos
- `status`: Estado ("success", "error")

### Tipos de Logs

#### 1. Llamadas a API de Catálogo

**Nivel:** INFO (éxito) / ERROR (fallo)  
**Campos adicionales:**
- `external_id`: ID externo del recurso/APU
- `error_type`: Tipo de error (solo en fallos)
- `error_message`: Mensaje de error (solo en fallos)

**Ejemplo:**
```json
{
  "component": "catalog-adapter",
  "action": "fetchRecurso",
  "catalog_source": "CAPECO",
  "external_id": "MAT-001",
  "duration_ms": 245,
  "status": "success",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 2. Accesos al Cache

**Nivel:** DEBUG  
**Campos adicionales:**
- `external_id`: ID externo
- `result`: Resultado ("hit", "miss")

**Ejemplo:**
```json
{
  "component": "catalog-cache",
  "action": "cache_access",
  "catalog_source": "CAPECO",
  "external_id": "MAT-001",
  "result": "hit",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 3. Creación de Snapshots

**Nivel:** INFO  
**Campos adicionales:**
- `apu_snapshot_id`: ID del snapshot creado
- `external_apu_id`: ID externo del APU
- `insumos_count`: Cantidad de insumos

**Ejemplo:**
```json
{
  "component": "snapshot-service",
  "action": "create_snapshot",
  "apu_snapshot_id": "660e8400-e29b-41d4-a716-446655440001",
  "external_apu_id": "APU-001",
  "catalog_source": "CAPECO",
  "duration_ms": 1250,
  "insumos_count": 5,
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 4. Modificaciones de Rendimiento

**Nivel:** INFO  
**Campos adicionales:**
- `apu_snapshot_id`: ID del snapshot
- `rendimiento_original`: Rendimiento original del catálogo
- `rendimiento_anterior`: Rendimiento antes de la modificación
- `rendimiento_nuevo`: Nuevo rendimiento
- `modificado_por`: ID del usuario que realizó la modificación
- `desviacion_original`: Diferencia entre nuevo y original
- `cambio_absoluto`: Diferencia entre nuevo y anterior

**Ejemplo:**
```json
{
  "component": "snapshot-service",
  "action": "update_rendimiento",
  "apu_snapshot_id": "660e8400-e29b-41d4-a716-446655440001",
  "rendimiento_original": "10.0",
  "rendimiento_anterior": "10.0",
  "rendimiento_nuevo": "12.5",
  "modificado_por": "770e8400-e29b-41d4-a716-446655440002",
  "desviacion_original": "2.5",
  "cambio_absoluto": "2.5",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 5. Errores de Catálogo

**Nivel:** ERROR  
**Campos adicionales:**
- `external_id`: ID externo
- `error_type`: Tipo de excepción
- `error_message`: Mensaje de error

**Ejemplo:**
```json
{
  "component": "catalog-adapter",
  "action": "fetchRecurso",
  "catalog_source": "CAPECO",
  "external_id": "MAT-999",
  "error_type": "CatalogNotFoundException",
  "error_message": "Recurso MAT-999 not found in catalog CAPECO",
  "correlation_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Correlation IDs

Cada request genera un correlation ID único (UUID) que se incluye en:
- Todos los logs relacionados con el request
- MDC (Mapped Diagnostic Context) para logging automático
- Patrón de logging configurado en `application.yml`

El correlation ID permite rastrear todas las operaciones relacionadas con un request específico a través de múltiples componentes.

## Configuración

### Endpoint de Métricas

El endpoint de métricas Prometheus está disponible en:
```
GET /actuator/prometheus
```

### Configuración en application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        catalog.api.latency: 0.5, 0.95, 0.99
        snapshot.creation.duration: 0.5, 0.95, 0.99
```

### Logging con Correlation ID

El patrón de logging ya está configurado en `application.yml` para incluir el correlation ID del MDC:

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n"
```

## Dashboards Recomendados

### Dashboard de API de Catálogo

1. **Request Rate**: Requests por segundo por catálogo
2. **Error Rate**: Porcentaje de errores por catálogo
3. **Latency**: p50, p95, p99 por operación
4. **Cache Hit Rate**: Efectividad del cache

### Dashboard de Snapshots

1. **Snapshot Creation Rate**: Snapshots creados por minuto
2. **Creation Duration**: Tiempo de creación (p50, p95, p99)
3. **Rendimiento Overrides**: Modificaciones de rendimiento por hora
4. **Insumos Distribution**: Distribución de cantidad de insumos

## Alertas Recomendadas

Aunque las alertas no están implementadas en esta tarea, se recomiendan las siguientes:

1. **Error Rate > 5%**: Más del 5% de requests fallan
2. **Latency p95 > 2s**: El p95 de latencia excede 2 segundos
3. **Cache Hit Rate < 50%**: El cache hit rate es menor al 50%
4. **Snapshot Creation Failures**: Fallos en creación de snapshots

## Troubleshooting

### Verificar Métricas

```bash
# Ver métricas Prometheus
curl http://localhost:8080/actuator/prometheus | grep catalog

# Ver métricas de snapshots
curl http://localhost:8080/actuator/prometheus | grep snapshot
```

### Buscar Logs por Correlation ID

```bash
# Buscar todos los logs de un request específico
grep "550e8400-e29b-41d4-a716-446655440000" application.log
```

### Verificar Cache Hit Rate

```bash
# Consultar métrica de cache
curl http://localhost:8080/actuator/prometheus | grep catalog_cache_requests
```

## Referencias

- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus Querying](https://prometheus.io/docs/prometheus/latest/querying/basics/)
