# Análisis de Resolución de Bloqueantes - PR #38

## Resumen Ejecutivo

Este documento detalla el plan de acción para resolver los **5 bloqueantes críticos** identificados en la revisión de aceptación del PR #38 contra REQ-57.

**Estado actual**: 5 bloqueantes críticos que impiden el merge
**Objetivo**: Resolver todos los bloqueantes manteniendo la integridad arquitectónica y funcional

---

## Bloqueante #1: Inconsistencia de URL de API

### Problema
- **Documentado**: `POST /api/v1/compras/{compraId}/recepciones`
- **Implementado**: `POST /api/v1/ordenes-compra/{id}/recepciones`
- **Impacto**: Rompe el contrato de API documentado y puede afectar integraciones frontend

### Análisis del Contexto
- El proyecto tiene dos controladores relacionados:
  - `CompraController` → `/api/v1/compras` (para el agregado `Compra`)
  - `OrdenCompraController` → `/api/v1/ordenes-compra` (para el agregado `OrdenCompra`)
- El PR implementa recepción para el agregado `Compra`, no `OrdenCompra`
- La documentación del PR y REQ-57 especifican `/api/v1/compras`

### Solución Propuesta

**Opción A: Cambiar a `/api/v1/compras` (RECOMENDADA)**
- Alinea con la documentación y el agregado de dominio
- Consistente con `CompraController`
- Requiere actualizar:
  1. `@RequestMapping` en `RecepcionController`
  2. Todos los tests de integración (11 tests)
  3. URL en `Location` header

**Archivos a modificar:**
```java
// RecepcionController.java
@RequestMapping("/api/v1/compras")  // Cambiar de "/api/v1/ordenes-compra"
@PostMapping("/{compraId}/recepciones")  // Cambiar variable de {id} a {compraId}
```

**Cambios en tests:**
- `RecepcionControllerIntegrationTest.java`: Actualizar todas las URLs de `/api/v1/ordenes-compra/{id}/recepciones` a `/api/v1/compras/{compraId}/recepciones`
- Actualizar variable de `id` a `compraId` en todos los métodos de test

**Estimación**: 30 minutos

---

## Bloqueante #2: `movimientoAlmacenId` siempre `null` en respuesta

### Problema
- El campo `movimientoAlmacenId` en `RecepcionResponse.DetalleResponse` siempre es `null`
- Hay un `TODO` en el código indicando que debe obtenerse del `MovimientoAlmacen` creado
- Impacto: Los consumidores de la API no pueden referenciar el movimiento creado

### Análisis del Contexto
- El `RecibirOrdenCompraUseCase` crea `MovimientoAlmacen` en el Step 10
- Cada `RecepcionDetalle` tiene un `MovimientoAlmacen` asociado
- El `movimientoAlmacenId` se genera pero no se almacena en `RecepcionDetalle`
- El controlador intenta reconstruir la respuesta desde la entidad, pero no tiene acceso al ID

### Solución Propuesta

**Opción A: Almacenar `movimientoAlmacenId` en `RecepcionDetalle` (RECOMENDADA)**

1. **Agregar campo al dominio:**
   ```java
   // RecepcionDetalle.java
   private final MovimientoAlmacenId movimientoAlmacenId;  // Nuevo campo
   ```

2. **Actualizar factory method:**
   ```java
   public static RecepcionDetalle crear(
       RecepcionDetalleId id,
       UUID detalleOrdenId,
       UUID recursoId,
       AlmacenId almacenId,
       BigDecimal cantidadRecibida,
       BigDecimal precioUnitario,
       MovimientoAlmacenId movimientoAlmacenId  // Nuevo parámetro
   )
   ```

3. **Modificar el use case para pasar el ID:**
   ```java
   // RecibirOrdenCompraUseCase.java - Step 10
   MovimientoAlmacenId movimientoId = MovimientoAlmacenId.generate();
   MovimientoAlmacen movimiento = MovimientoAlmacen.crearEntrada(...);
   
   // Actualizar RecepcionDetalle con el movimientoId
   detalleRecepcion = RecepcionDetalle.crear(
       detalleRecepcion.getId(),
       detalleRecepcion.getDetalleOrdenId(),
       detalleRecepcion.getRecursoId(),
       detalleRecepcion.getAlmacenId(),
       detalleRecepcion.getCantidadRecibida(),
       detalleRecepcion.getPrecioUnitario(),
       movimientoId  // Pasar el ID
   );
   ```

4. **Agregar columna a la base de datos:**
   ```sql
   -- Nueva migración V23
   ALTER TABLE recepcion_detalle 
   ADD COLUMN movimiento_almacen_id UUID REFERENCES movimiento_almacen(id);
   ```

5. **Actualizar entidad JPA:**
   ```java
   // RecepcionDetalleEntity.java
   @Column(name = "movimiento_almacen_id")
   private UUID movimientoAlmacenId;
   ```

6. **Actualizar mapper:**
   ```java
   // RecepcionMapper.java
   // Incluir movimientoAlmacenId en toEntity() y toDomain()
   ```

7. **Actualizar controlador:**
   ```java
   // RecepcionController.toResponse()
   UUID movimientoAlmacenId = detalleEntity.getMovimientoAlmacenId();  // Ya no es null
   ```

**Archivos a modificar:**
- `RecepcionDetalle.java` (dominio)
- `RecepcionDetalleEntity.java` (entidad)
- `RecepcionMapper.java` (mapper)
- `RecibirOrdenCompraUseCase.java` (use case)
- `RecepcionController.java` (controlador)
- Nueva migración `V23__add_movimiento_almacen_id_to_recepcion_detalle.sql`

**Estimación**: 1.5 horas

---

## Bloqueante #3: Violación de Arquitectura Hexagonal

### Problema
- `RecepcionController` inyecta directamente `RecepcionJpaRepository` (capa de infraestructura)
- También inyecta `CompraRepository` (puerto de dominio, correcto)
- Violación: El controlador debe interactuar solo con puertos de aplicación, no con repositorios JPA

### Análisis del Contexto
- El controlador necesita cargar `RecepcionEntity` y `Compra` para construir la respuesta
- Actualmente hace:
  1. Ejecuta el use case (correcto)
  2. Carga `RecepcionEntity` desde `RecepcionJpaRepository` (incorrecto)
  3. Carga `Compra` desde `CompraRepository` (correcto)

### Solución Propuesta

**Opción A: Retornar `Recepcion` completo desde el use case (RECOMENDADA)**

1. **Modificar el puerto de entrada:**
   ```java
   // RecibirOrdenCompraInputPort.java
   Recepcion ejecutar(RecibirOrdenCompraCommand command);  // Retornar Recepcion en lugar de RecepcionId
   ```

2. **Actualizar el use case:**
   ```java
   // RecibirOrdenCompraUseCase.java
   @Override
   public Recepcion ejecutar(RecibirOrdenCompraCommand command) {
       // ... toda la lógica existente ...
       
       // Step 12: Retornar Recepcion completa
       return recepcion;  // Ya está cargada en memoria
   }
   ```

3. **Actualizar el controlador:**
   ```java
   // RecepcionController.java
   // Eliminar inyección de RecepcionJpaRepository
   private final RecibirOrdenCompraInputPort recibirOrdenCompraInputPort;
   private final CompraRepository compraRepository;  // Mantener solo este
   
   @PostMapping("/{compraId}/recepciones")
   public ResponseEntity<RecepcionResponse> recibirOrdenCompra(...) {
       // Ejecutar caso de uso
       Recepcion recepcion = recibirOrdenCompraInputPort.ejecutar(command);
       
       // Cargar compra para obtener estado
       Compra compra = compraRepository.findById(CompraId.from(compraId))
           .orElseThrow(...);
       
       // Construir respuesta desde dominio
       RecepcionResponse response = toResponse(recepcion, compra);
       
       URI location = URI.create("/api/v1/compras/" + compraId + "/recepciones/" + recepcion.getId().getValue());
       return ResponseEntity.created(location).body(response);
   }
   
   private RecepcionResponse toResponse(Recepcion recepcion, Compra compra) {
       // Mapear desde dominio, no desde entidad
       List<RecepcionResponse.DetalleResponse> detalles = recepcion.getDetalles().stream()
           .map(detalle -> {
               // Buscar detalle de compra para cantidadPendiente
               CompraDetalle compraDetalle = compra.getDetalles().stream()
                   .filter(d -> d.getId().equals(detalle.getDetalleOrdenId()))
                   .findFirst()
                   .orElseThrow(...);
               
               return new RecepcionResponse.DetalleResponse(
                   detalle.getRecursoId(),
                   detalle.getCantidadRecibida(),
                   compraDetalle.getCantidadPendiente(),
                   detalle.getAlmacenId().getValue(),
                   detalle.getMovimientoAlmacenId().getValue()  // Ya no es null
               );
           })
           .collect(Collectors.toList());
       
       return new RecepcionResponse(
           recepcion.getId().getValue(),
           recepcion.getCompraId().getValue(),
           compra.getEstado(),
           recepcion.getFechaRecepcion(),
           recepcion.getGuiaRemision(),
           detalles,
           recepcion.getCreadoPorUsuarioId(),
           recepcion.getFechaCreacion()
       );
   }
   ```

**Archivos a modificar:**
- `RecibirOrdenCompraInputPort.java` (cambiar retorno)
- `RecibirOrdenCompraUseCase.java` (retornar Recepcion)
- `RecepcionController.java` (eliminar RecepcionJpaRepository, mapear desde dominio)
- Tests del use case (actualizar aserciones)

**Estimación**: 1 hora

---

## Bloqueante #4: Inconsistencias de Estado HTTP en Tests

### Problema
- Los tests esperan `422 UNPROCESSABLE_ENTITY` con `error: "BUSINESS_RULE_VIOLATION"`
- El `GlobalExceptionHandler` mapea:
  - `ProjectNotActiveException` → `412 PRECONDITION_FAILED`
  - `DuplicateReceptionException` → `409 CONFLICT`
  - `InvalidStateException` → `400 BAD_REQUEST`
- Los tests fallarán porque esperan `422` pero reciben `409`/`412`/`400`

### Análisis del Contexto
- El use case lanza `BusinessRuleException` para proyecto no activo y guía duplicada
- Pero el handler tiene excepciones específicas que mapean a otros códigos
- Hay inconsistencia entre lo que lanza el use case y lo que esperan los tests

### Solución Propuesta

**Opción A: Actualizar tests para alinear con el handler (RECOMENDADA)**

1. **Actualizar test de proyecto no activo:**
   ```java
   // RecepcionControllerIntegrationTest.java - Test #5
   .andExpect(status().isPreconditionFailed())  // Cambiar de isUnprocessableEntity()
   .andExpect(jsonPath("$.error").value("PROJECT_NOT_ACTIVE"));  // Cambiar de "BUSINESS_RULE_VIOLATION"
   ```

2. **Actualizar test de guía duplicada:**
   ```java
   // RecepcionControllerIntegrationTest.java - Test #6
   .andExpect(status().isConflict())  // Cambiar de isUnprocessableEntity()
   .andExpect(jsonPath("$.error").value("DUPLICATE_RECEPTION"));  // Cambiar de "BUSINESS_RULE_VIOLATION"
   ```

3. **Actualizar test de estado inválido:**
   ```java
   // RecepcionControllerIntegrationTest.java - Test #8
   .andExpect(status().isBadRequest())  // Cambiar de isUnprocessableEntity()
   .andExpect(jsonPath("$.error").value("INVALID_STATE"));  // Cambiar de "BUSINESS_RULE_VIOLATION"
   ```

4. **Actualizar use case para lanzar excepciones específicas:**
   ```java
   // RecibirOrdenCompraUseCase.java
   // Step 2: Cambiar BusinessRuleException por ProjectNotActiveException
   if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
       throw new ProjectNotActiveException(
           String.format("El proyecto %s no está ACTIVO. Estado actual: %s", 
               proyecto.getNombre(), proyecto.getEstado())
       );
   }
   
   // Step 3: Cambiar BusinessRuleException por DuplicateReceptionException
   if (recepcionRepository.existsByCompraIdAndGuiaRemision(compraId, command.getGuiaRemision())) {
       throw new DuplicateReceptionException(
           String.format("Ya existe una recepción con la guía de remisión '%s' para la compra %s",
               command.getGuiaRemision(), compraId.getValue())
       );
   }
   
   // Step 5: Ya lanza IllegalStateException, cambiar a InvalidStateException
   if (compra.getEstado() != EstadoCompra.ENVIADA && compra.getEstado() != EstadoCompra.PARCIAL) {
       throw new InvalidStateException(
           String.format("La compra debe estar en estado ENVIADA o PARCIAL para recibir. Estado actual: %s",
               compra.getEstado())
       );
   }
   ```

**Archivos a modificar:**
- `RecibirOrdenCompraUseCase.java` (cambiar excepciones)
- `RecepcionControllerIntegrationTest.java` (actualizar 3 tests)

**Estimación**: 45 minutos

---

## Bloqueante #5: Fallback de Seguridad en `getCurrentUserId()`

### Problema
- `getCurrentUserId()` devuelve UUID nulo (`00000000-0000-0000-0000-000000000000`) para usuarios no autenticados
- Comentario dice "Para desarrollo", pero es un riesgo de seguridad si llega a producción
- Otros controladores (`OrdenCompraController`) tienen el mismo problema

### Análisis del Contexto
- El método está protegido con `@PreAuthorize("hasRole('RESIDENTE')")`
- Spring Security debería garantizar que solo usuarios autenticados lleguen al método
- Pero el fallback permite ejecución sin autenticación válida

### Solución Propuesta

**Opción A: Lanzar excepción de autenticación (RECOMENDADA)**

1. **Crear excepción de autenticación (si no existe):**
   ```java
   // application/compra/exception/AuthenticationRequiredException.java
   public class AuthenticationRequiredException extends RuntimeException {
       public AuthenticationRequiredException(String message) {
           super(message);
       }
   }
   ```

2. **Actualizar `getCurrentUserId()` en `RecepcionController`:**
   ```java
   private UUID getCurrentUserId(Authentication auth) {
       Authentication authentication = auth != null ? auth : SecurityContextHolder.getContext().getAuthentication();
       
       if (authentication == null || !authentication.isAuthenticated()
               || "anonymousUser".equals(authentication.getPrincipal())) {
           throw new AuthenticationRequiredException(
               "Se requiere autenticación para realizar esta operación"
           );
       }
       
       Object principal = authentication.getPrincipal();
       if (principal instanceof String) {
           try {
               return UUID.fromString((String) principal);
           } catch (IllegalArgumentException e) {
               throw new AuthenticationRequiredException(
                   "No se pudo extraer el ID de usuario del token de autenticación"
               );
           }
       }
       
       // Si el principal no es String, intentar extraer de UserDetails
       if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
           // Implementar según la estructura de UserDetails
           throw new AuthenticationRequiredException(
               "Formato de autenticación no soportado"
           );
       }
       
       throw new AuthenticationRequiredException(
           "No se pudo determinar el usuario autenticado"
       );
   }
   ```

3. **Agregar handler en `GlobalExceptionHandler`:**
   ```java
   @ExceptionHandler(AuthenticationRequiredException.class)
   public ResponseEntity<Map<String, Object>> handleAuthenticationRequired(
           AuthenticationRequiredException ex) {
       Map<String, Object> body = new HashMap<>();
       body.put("message", ex.getMessage());
       body.put("error", "AUTHENTICATION_REQUIRED");
       body.put("status", HttpStatus.UNAUTHORIZED.value());
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
   }
   ```

4. **Aplicar el mismo cambio a `OrdenCompraController`:**
   - Reutilizar el mismo método o crear un helper compartido
   - Considerar crear un `@Component` `SecurityHelper` para evitar duplicación

**Archivos a modificar:**
- `RecepcionController.java` (actualizar `getCurrentUserId()`)
- `OrdenCompraController.java` (actualizar `getCurrentUserId()`)
- `GlobalExceptionHandler.java` (agregar handler)
- Opcional: Crear `SecurityHelper` component para reutilización

**Estimación**: 1 hora

---

## Plan de Ejecución

### Fase 1: Preparación (15 min)
1. Crear branch desde el PR actual
2. Revisar y entender todos los cambios propuestos
3. Preparar ambiente de pruebas

### Fase 2: Bloqueantes Simples (1.5 horas)
1. ✅ **Bloqueante #1**: Cambiar URL de API (30 min)
2. ✅ **Bloqueante #4**: Corregir tests y excepciones (45 min)
3. ✅ **Bloqueante #5**: Corregir seguridad (1 hora)

### Fase 3: Bloqueantes Complejos (2.5 horas)
1. ✅ **Bloqueante #2**: Implementar `movimientoAlmacenId` (1.5 horas)
2. ✅ **Bloqueante #3**: Corregir arquitectura (1 hora)

### Fase 4: Validación (30 min)
1. Ejecutar todos los tests
2. Verificar que no hay regresiones
3. Revisar código con linter

### Fase 5: Commit y Push (15 min)
1. Commit con mensaje descriptivo
2. Push al branch
3. Actualizar PR con comentario

**Tiempo total estimado**: ~4.5 horas

---

## Consideraciones Adicionales

### Migración de Base de Datos
- El bloqueante #2 requiere una nueva migración (V23)
- Verificar que las migraciones anteriores (V21, V22) estén aplicadas
- Probar rollback de la migración

### Tests de Integración
- Todos los tests deben pasar después de los cambios
- Considerar agregar tests adicionales para:
  - Verificar que `movimientoAlmacenId` no es null
  - Verificar que la arquitectura no se viola
  - Verificar que la autenticación funciona correctamente

### Compatibilidad con Frontend
- Si el frontend ya está usando la API, coordinar el cambio de URL
- Considerar mantener ambas URLs temporalmente con deprecation warning

### Documentación
- Actualizar documentación de API si existe
- Actualizar README si menciona la URL
- Actualizar comentarios en código

---

## Checklist de Validación Final

Antes de considerar resueltos los bloqueantes, verificar:

- [ ] Todos los tests pasan (unitarios, integración, e2e)
- [ ] No hay violaciones de arquitectura (revisar con Semgrep)
- [ ] No hay warnings de AXIOM
- [ ] La URL de API coincide con la documentación
- [ ] `movimientoAlmacenId` está poblado en todas las respuestas
- [ ] El controlador no inyecta repositorios JPA directamente
- [ ] Los tests esperan los códigos HTTP correctos
- [ ] `getCurrentUserId()` lanza excepción en lugar de devolver UUID nulo
- [ ] La migración de base de datos se aplica correctamente
- [ ] No hay regresiones en funcionalidad existente

---

## Notas Finales

Este análisis proporciona un plan detallado para resolver todos los bloqueantes. Se recomienda:

1. **Ejecutar en orden**: Los bloqueantes están ordenados por complejidad
2. **Hacer commits incrementales**: Un commit por bloqueante facilita el review
3. **Validar después de cada bloqueante**: No esperar al final para ejecutar tests
4. **Documentar decisiones**: Si se toma un enfoque diferente, documentarlo

**Prioridad**: Todos los bloqueantes son críticos y deben resolverse antes del merge.
