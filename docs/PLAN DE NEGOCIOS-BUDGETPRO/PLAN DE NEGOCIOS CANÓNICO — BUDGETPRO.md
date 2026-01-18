# PLAN DE NEGOCIOS CANÓNICO — BUDGETPRO

Version: 2026
Estado: ARTEFACTO CANONICO / GOLDEN SOURCE

---

## 0. Declaracion de posicion (statement of intent)
BudgetPro no es una aplicacion ni un ERP generico. Es un sistema disciplinario de ejecucion
de obra, orientado a proteger la utilidad mediante reglas de negocio ejecutables, trazabilidad
forense y control preventivo del presupuesto.

---

## 1. Tesis de negocio (resumen ejecutivo)
Problema: la construccion opera con herramientas desconectadas (Excel, libreta, mensajeria),
lo que impide conocer el margen real durante la obra y elimina la trazabilidad del gasto.

Solucion: convertir el presupuesto aprobado en un contrato digital inmutable que gobierna
compras, inventarios y registro de avances. El sistema bloquea desviaciones sin evidencia
y obliga a justificar excepciones.

ICP:
- Primario: PyMES constructoras con 3-5 proyectos simultaneos.
- Secundario: desarrolladores que necesitan auditar contratistas.

---

## 2. Doctrina operativa (leyes del sistema)
Estos principios no son configurables. Definen el ADN de BudgetPro.

1) El presupuesto es ley.
2) No existe costo sin origen tecnico (partida, motivo, responsable).
3) La verdad no es retroactiva: no se reescribe historia.
4) Excepciones son eventos formales, no atajos.
5) El sistema protege la utilidad, no la comodidad.

Decision canonica:
- Online-only. Se elimina sincronizacion offline para preservar integridad contractual.

---

## 3. Definicion funcional del producto
Nucleo:
- Presupuesto con maquina de estados (Borrador -> Congelado).
- Snapshot inmutable en JSONB para linea base.

Ejecucion:
- APU dicta topes de compra (Purchasing Caps).
- Inventario auditor: salidas siempre imputadas a partida.
- Avance fisico medido en unidades, no porcentajes.

Canal de excepcion:
- Gasto fuera de presupuesto permitido solo con clasificacion y autorizacion formal.
- Se construye historial reputacional del ejecutor.

EVM operativo:
- SPI/CPI como semaforo.
- Si TCPI > 1.20, se declara proyecto matematicamente critico.

---

## 4. Arquitectura tecnica (inmutabilidad y rigor)
- Backend: Java Spring Boot 3.2.
- Patron: Arquitectura Hexagonal + DDD.
- Base de datos: PostgreSQL con JSONB para snapshots inmutables.
- Cliente: PWA o nativa online-only para validacion sincrona.

---

## 5. Modelo de negocio y pricing (canonico)
Estructura por capacidad. Se descarta venta modular.

| Nivel | Precio | Capacidad | Perfil |
| --- | --- | --- | --- |
| STARTER | 29/mes | 1 proyecto | Validacion / freelancers |
| GROWTH | 99/mes | 5 proyectos | PyMES (sweet spot) |
| SCALE | 249/mes | Ilimitado + API | Empresas grandes |

Todos los planes incluyen el ciclo completo (presupuesto, compras, inventarios, EVM).
Retencion basada en data historica real del cliente.

---

## 6. Go-to-market (disciplinario)
Mensaje: no se vende "control". Se vende proteccion de utilidad y evidencia operativa.

Proceso:
- Growth/Scale: venta consultiva obligatoria.
- Starter: puerta de entrada, no motor principal de ingresos.
- Eliminacion de offline filtra clientes de alto costo de soporte.

---

## 7. Operaciones y Customer Success (eslabon critico)
Customer Success es ingenieria de adopcion, no soporte.

Riesgo principal:
- Si no se logra congelar el primer presupuesto y registrar las primeras compras,
  el churn es inmediato.

La adopcion es el punto de fractura del modelo. El plan asume CS disciplinario y activo.

---

## 8. Roadmap canonico
1) Cimientos: presupuesto, estados, snapshots inmutables.
2) Control: compras con topes, inventario auditor.
3) Doctrina: canal de excepcion y taxonomia del error.
4) Verdad: EVM operativo, alertas de TCPI.

---

## 9. Sentencia final
BudgetPro no compite contra software. Compite contra el caos operativo.

El camino correcto es unico:
- presupuesto como contrato digital,
- online-only para integridad contractual,
- pricing por capacidad,
- doctrina operativa por encima de features.

Documento congelado. Cualquier cambio requiere nueva auditoria formal.
