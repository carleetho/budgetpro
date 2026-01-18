# BUDGETPRO — Vertical Slice Canónico

---

```markdown

## Caso de Uso: Registrar Compra Directa

> **🏷️ META-INFO**
> * **📅 Fecha:** 09/01/2026
> * **🧊 Estado:** CONGELADO (Código de Referencia Aprobado)
> * **👮 Auditoría:** Code Review Senior — APPROVED (Merge Authorized)
> * **🏗️ Arquitectura:** Clean / Hexagonal
> * **📎 Propósito:** Código “Ley” para el equipo

---

## 1️⃣ Resumen Ejecutivo

Este documento define el **Vertical Slice de referencia** para el caso de uso **“Registrar Compra Directa”** en BUDGETPRO.

Su objetivo es:

- Establecer un **estándar técnico obligatorio**
- Demostrar **separación estricta de capas**
- Mostrar **inversión de dependencias real**
- Servir como **plantilla para todos los casos de uso futuros**

⚠️ **Regla de oro:**
Si un desarrollador duda *“¿cómo se implementa X?”*, la respuesta debe encontrarse aquí.

---

## 2️⃣ Estructura de Carpetas (Congelada)

```text
com.invco.budgetpro
├── domain
│   ├── model
│   │   ├── compra
│   │   │   ├── Compra.java
│   │   │   ├── CompraDetalle.java
│   │   │   └── CompraId.java
│   │   ├── billetera
│   │   │   ├── Billetera.java
│   │   │   └── BilleteraId.java
│   │   └── inventario
│   │       └── InventarioItem.java
│   └── service
│       └── ProcesarCompraDirectaService.java
│
├── application
│   ├── port
│   │   ├── out
│   │   │   ├── CompraRepository.java
│   │   │   ├── BilleteraRepository.java
│   │   │   └── InventarioRepository.java
│   └── usecase
│       └── RegistrarCompraDirectaUseCase.java
│
├── infrastructure
│   ├── persistence
│   │   ├── entity
│   │   │   ├── CompraEntity.java
│   │   │   └── BilleteraEntity.java
│   │   ├── repository
│   │   │   └── CompraJpaRepository.java
│   │   └── adapter
│   │       └── CompraRepositoryAdapter.java
│   └── web
│       └── CompraController.java

```

---

## 3️⃣ Dominio — Agregado `Compra` (No Anémico)

```java
public class Compra {

    private final CompraId id;
    private final UUID presupuestoId;
    private final List<CompraDetalle> detalles;
    private BigDecimal total;

    private Compra(CompraId id, UUID presupuestoId, List<CompraDetalle> detalles) {
        if (presupuestoId == null) {
            throw new IllegalStateException("No se puede comprar sin presupuesto");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("La compra debe tener detalles");
        }
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.detalles = detalles;
        calcularTotal();
    }

    public static Compra crear(UUID presupuestoId, List<CompraDetalle> detalles) {
        return new Compra(CompraId.nuevo(), presupuestoId, detalles);
    }

    public static Compra reconstruir(
            CompraId id,
            UUID presupuestoId,
            List<CompraDetalle> detalles,
            BigDecimal total
    ) {
        Compra compra = new Compra(id, presupuestoId, detalles);
        compra.total = total;
        return compra;
    }

    private void calcularTotal() {
        this.total = detalles.stream()
                .map(CompraDetalle::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal total() {
        return total;
    }

    public CompraId id() {
        return id;
    }
}

```

🛑 **Invariante crítica:**

> No se puede registrar una compra sin presupuesto.
> 

Esta regla **vive en el Dominio**, no en el controlador ni en la BD.

---

## 4️⃣ Puertos de Salida (Application → Domain)

```java
public interface CompraRepository {
    void save(Compra compra);
}

```

```java
public interface BilleteraRepository {
    Optional<Billetera> findByProyectoId(UUID proyectoId);
}

```

📌 **Regla congelada:**

El Dominio **no conoce JPA, SQL ni anotaciones**.

---

## 5️⃣ Servicio de Dominio (Puro)

```java
public class ProcesarCompraDirectaService {

    public void procesar(Compra compra, Billetera billetera) {
        billetera.validarSaldoSuficiente(compra.total());
        billetera.debitar(compra.total());
    }
}

```

✔ No guarda

✔ No abre transacciones

✔ 100% testeable en unit tests

---

## 6️⃣ Caso de Uso (Orquestador)

```java
public class RegistrarCompraDirectaUseCase {

    private final CompraRepository compraRepository;
    private final BilleteraRepository billeteraRepository;
    private final ProcesarCompraDirectaService domainService;

    /**
     * NOTA ARQUITECTÓNICA:
     * @Transactional se acepta como deuda técnica consciente para el MVP.
     */
    @Transactional
    public RegistrarCompraResponse ejecutar(RegistrarCompraCommand command) {

        Compra compra = Compra.crear(
                command.presupuestoId(),
                command.detalles()
        );

        Billetera billetera = billeteraRepository
                .findByProyectoId(command.proyectoId())
                .orElseThrow(() -> new IllegalStateException("Billetera no encontrada"));

        domainService.procesar(compra, billetera);

        // El UseCase es dueño de la transacción
        compraRepository.save(compra);
        billeteraRepository.save(billetera);

        return new RegistrarCompraResponse(compra.id().value());
    }
}

```

🧠 **Decisión clave (auditada y aprobada):**

> El UseCase es el único responsable de persistir los agregados modificados.
> 

---

## 7️⃣ Adaptador de Persistencia (Infraestructura)

```java
public class CompraRepositoryAdapter implements CompraRepository {

    private final CompraJpaRepository jpaRepository;

    @Override
    public void save(Compra compra) {
        CompraEntity entity = mapToEntity(compra);
        jpaRepository.save(entity);
    }

    private CompraEntity mapToEntity(Compra compra) {
        return new CompraEntity(
                compra.id().value(),
                compra.total()
        );
    }

    private Compra mapToDomain(CompraEntity entity) {
        return Compra.reconstruir(
                new CompraId(entity.getId()),
                entity.getPresupuestoId(),
                /* detalles */,
                entity.getTotal()
        );
    }
}

```

⚠️ **Regla obligatoria:**

El mapeo es **manual**.

No MapStruct. No magia. No atajos.

---

## 8️⃣ Controller (Entrega mínima)

```java
@PostMapping("/compras/directa")
public RegistrarCompraResponse registrar(@RequestBody RegistrarCompraRequest request) {
    return useCase.ejecutar(request.toCommand());
}

```

✔ Sin lógica

✔ Sin reglas

✔ Sin transacciones

---

## 9️⃣ Principios Arquitectónicos Reafirmados

| Principio | Estado |
| --- | --- |
| Dominio rico | ✅ |
| Hexagonal real | ✅ |
| Inversión de dependencias | ✅ |
| UX defensiva soportada | ✅ |
| Testabilidad | ✅ |

---

## 🏁 Conclusión

Este documento define el **patrón obligatorio** para:

- Compra Directa
- Reversiones
- Préstamos
- Planillas
- Estimaciones
- Órdenes de cambio

📌 **Si un PR no respeta este patrón → NO SE APRUEBA.**

---

**[FIN DEL ARTEFACTO — VERTICAL SLICE CANÓNICO]**