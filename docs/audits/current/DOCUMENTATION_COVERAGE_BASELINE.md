# Documentation Coverage Audit Report

## Executive Summary

- **Audit Date**: 2026-02-08
- **Total Existing Rules**: 161
- **Documented Rules (Canonical)**: 43
- **Undocumented Rules (Code-Only)**: 118
- **Documentation Coverage**: 26.7%

## Coverage Gap Analysis

The following rules exist in the codebase (as enforced by validators or logic) but are NOT explicitly referenced in the canonical documentation (`docs/modules/*.md`).

| Rule ID   | Module                                 | Description                                                                                              |
| --------- | -------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| REGLA-001 | Producción (RPC)                       | Un reporte aprobado o rechazado es inmutable; no se puede editar ni eliminar.                            |
| REGLA-002 | Producción (RPC)                       | La fecha del reporte es obligatoria y no puede ser futura.                                               |
| REGLA-003 | Producción (RPC), Proyectos            | Solo se puede reportar avance en proyectos en estado EJECUCION.                                          |
| REGLA-004 | Producción (RPC), Presupuesto/Partidas | La cantidad reportada es obligatoria y el reporte no puede exceder el metrado vigente de la partida.     |
| REGLA-005 | Producción (RPC)                       | Un reporte de producción debe contener al menos un detalle.                                              |
| REGLA-006 | Producción (RPC)                       | Solo se puede aprobar un reporte en estado PENDIENTE.                                                    |
| REGLA-007 | Producción (RPC)                       | Solo se puede rechazar un reporte en estado PENDIENTE y el motivo es obligatorio.                        |
| REGLA-008 | Producción (RPC)                       | Si el estado del reporte es nulo, se asigna PENDIENTE.                                                   |
| REGLA-009 | Producción (RPC)                       | El reporte de producción no puede ser nulo.                                                              |
| REGLA-010 | Estimaciones                           | La estimación solo puede pasar de BORRADOR a APROBADA y de APROBADA a PAGADA.                            |
| REGLA-011 | Estimaciones                           | El monto neto a pagar de una estimación es: montoBruto - amortizacionAnticipo - retencionFondoGarant...  |
| REGLA-012 | Estimaciones                           | El número de estimación debe ser positivo cuando se define.                                              |
| REGLA-013 | Estimaciones                           | El periodo de fin no puede ser menor al periodo de inicio.                                               |
| REGLA-014 | Estimaciones                           | La amortización de anticipo y la retención de fondo de garantía no pueden ser negativas.                 |
| REGLA-015 | Estimaciones                           | La cantidad de avance y el precio unitario en un detalle de estimación no pueden ser negativos.          |
| REGLA-016 | Estimaciones, Partidas                 | El volumen estimado no puede exceder el volumen contratado.                                              |
| REGLA-017 | Estimaciones                           | La amortización de anticipo calculada no puede exceder el saldo pendiente.                               |
| REGLA-018 | Cronograma                             | La fecha de fin de una actividad no puede ser menor que la fecha de inicio.                              |
| REGLA-019 | Cronograma                             | Una actividad no puede ser predecesora de sí misma.                                                      |
| REGLA-020 | Cronograma                             | En una dependencia Fin-Inicio, la fecha de inicio de B debe ser >= la fecha de fin de A.                 |
| REGLA-021 | Cronograma                             | La fecha de fin estimada del programa no puede ser menor a la fecha de inicio.                           |
| REGLA-022 | Cronograma                             | No se puede actualizar la fecha de fin desde actividades si no hay fecha de inicio.                      |
| REGLA-023 | Sobrecostos                            | Los porcentajes de indirectos, financiamiento, utilidad, fianzas e impuestos reflejables no pueden s...  |
| REGLA-024 | RRHH / Sobrecostos                     | Los días de aguinaldo, vacaciones y no trabajados no pueden ser negativos; los días laborables al añ...  |
| REGLA-025 | RRHH / Sobrecostos                     | El salario base debe ser positivo para calcular salario real.                                            |
| REGLA-026 | Alertas Paramétricas                   | Si un recurso EQUIPO tiene costo_horario = 0, se genera ALERTA CRÍTICA.                                  |
| REGLA-027 | Alertas Paramétricas                   | El ratio Kg Acero / m3 Concreto debe estar entre 80 y 150; fuera de rango genera WARNING.                |
| REGLA-028 | Alertas Paramétricas                   | El tamaño del agregado no debe exceder 1/5 del ancho del elemento; si excede, genera ALERTA TÉCNICA.     |
| REGLA-029 | Inteligencia Maquinaria                | Si un insumo tiene precio unitario 0, se genera alerta de descapitalización de maquinaria.               |
| REGLA-030 | Inteligencia Maquinaria                | Valor inicial, vida útil y horas anuales de uso deben ser positivos para calcular costo horario.         |
| REGLA-031 | Compras                                | El proyectoId, la fecha, el proveedor y los detalles son obligatorios en una compra.                     |
| REGLA-032 | Compras                                | El subtotal de un detalle de compra es cantidad \* precio unitario; cantidad y precio unitario no pue... |
| REGLA-033 | Compras, Billetera, Inventario         | Para procesar una compra, las partidas deben existir; se descuenta de billetera, se aprueba la compr...  |
| REGLA-034 | Consumos                               | El consumo de partida requiere partidaId, fecha y tipo; el monto no puede ser negativo.                  |
| REGLA-035 | APU                                    | En APU, el partidaId es obligatorio y la lista de insumos no puede ser nula.                             |
| REGLA-036 | APU                                    | En APU, el subtotal de insumo es cantidad \* precio unitario; cantidad y precio unitario no pueden se... |
| REGLA-037 | Partidas                               | En Partida: presupuestoId obligatorio, item no vacío, descripción no vacía, metrado no negativo y ni...  |
| REGLA-038 | Partidas                               | Si una partida tiene padreId, debe pertenecer al mismo presupuestoId (validado a nivel de aplicación...  |
| REGLA-039 | Recursos                               | En Recurso: nombre no vacío, tipo no nulo y unidadBase no vacía.                                         |
| REGLA-040 | Recursos                               | El nombre del recurso se normaliza con trim, uppercase y espacios simples.                               |
| REGLA-041 | Recursos                               | Un recurso provisional se crea con estado EN_REVISION.                                                   |
| REGLA-042 | Billetera                              | En MovimientoCaja: monto > 0, referencia no vacía, billeteraId y tipo no nulos.                          |
| REGLA-043 | Proyectos                              | El nombre del proyecto no puede estar vacío; el estado del proyecto no puede ser nulo.                   |
| REGLA-044 | Presupuesto                            | El nombre del presupuesto no puede estar vacío; el proyectoId y el estado son obligatorios.              |
| REGLA-045 | Presupuesto                            | Al aprobar presupuesto, el estado cambia a APROBADO y esContractual se marca true.                       |
| REGLA-046 | Presupuesto                            | El presupuesto APROBADO es de solo lectura.                                                              |
| REGLA-047 | Partidas, Presupuesto                  | El metradoOriginal de partida es inmutable si el presupuesto está APROBADO.                              |
| REGLA-048 | Partidas                               | Si metradoVigente es nulo al persistir una partida, se iguala a metradoOriginal.                         |
| REGLA-049 | Inventarios                            | El movimiento de almacén de tipo SALIDA requiere partidaId.                                              |
| REGLA-050 | Inventarios                            | Si tipo es nulo en movimiento de almacén, se asigna tipoMovimiento.                                      |
| REGLA-051 | Seguridad                              | JWT_SECRET es obligatorio y debe tener al menos 32 caracteres.                                           |
| REGLA-052 | Seguridad                              | Las rutas `/api/public/**` y `/api/v1/auth/**` son públicas; el resto requiere autenticación.            |
| REGLA-053 | Seguridad                              | CORS permite origen `http://localhost:3000` y métodos GET, POST, PUT, DELETE, PATCH, OPTIONS.            |
| REGLA-054 | Configuración                          | Las variables RESEND_API_KEY, ADMIN_EMAIL y JWT_SECRET se requieren por configuración.                   |
| REGLA-055 | Seguridad                              | La entidad Usuario exige email único.                                                                    |
| REGLA-056 | Seguridad                              | El rol de usuario debe estar en {ADMIN, RESIDENTE, GERENTE, AUDITOR}.                                    |
| REGLA-057 | Marketing                              | El estado de Lead debe estar en {NUEVO, CONTACTADO, CONVERTIDO}.                                         |
| REGLA-058 | Producción (RPC)                       | El estado de reporte de producción debe estar en {PENDIENTE, APROBADO, RECHAZADO}.                       |
| REGLA-059 | Producción (RPC)                       | En detalle RPC, cantidad_reportada debe ser >= 0.                                                        |
| REGLA-060 | Proyectos                              | En proyecto, el estado está restringido por CHECK en migraciones.                                        |
| REGLA-061 | Presupuesto                            | En presupuesto, el estado está restringido por CHECK en migraciones.                                     |
| REGLA-062 | Partidas                               | En partida, metrado_original, metrado_vigente y precio_unitario deben ser >= 0.                          |
| REGLA-063 | Recursos                               | En recurso, el tipo debe estar en {MATERIAL, MANO_OBRA, EQUIPO, SUBCONTRATO}; el estado en {ACTIVO, ...  |
| REGLA-064 | Inventarios                            | En movimiento_almacen: cantidad > 0, precio_unitario >= 0, importe_total >= 0.                           |
| REGLA-065 | Inventarios                            | En stock_movimiento: cantidad_entrada, cantidad_salida, saldo_cantidad, saldo_valor y costo_promedio...  |
| REGLA-066 | Estimaciones                           | En estimación: periodo_fin >= periodo_inicio; montos y acumulados no negativos; estado en {BORRADOR,...  |
| REGLA-067 | Cronograma                             | En cronograma: fecha_fin_estimada >= fecha_inicio; duracion_total_dias > 0 si existe; actividad con ...  |
| REGLA-068 | Avance / Valuación                     | En avance_fisico: metrado_ejecutado >= 0; valuación con (proyecto_id, codigo) único.                     |
| REGLA-069 | RRHH / Sobrecostos                     | En configuracion_laboral: días no negativos; porcentaje_seguridad_social entre 0 y 100; dias_laborab...  |
| REGLA-070 | Sobrecostos                            | En analisis_sobrecosto: porcentajes entre 0 y 100.                                                       |
| REGLA-071 | Proyectos                              | El proyecto tiene moneda obligatoria de longitud 3 y presupuesto_total no nulo.                          |
| REGLA-072 | Cambios                                | La orden de cambio requiere proyecto, código y tipo/estado; impacto_plazo no puede ser negativo.         |
| REGLA-073 | Auditoría                              | El created_by es obligatorio en entidades auditables.                                                    |
| REGLA-074 | Marketing                              | En Lead: nombre_contacto obligatorio; email debe ser válido; fecha_solicitud no nula.                    |
| REGLA-075 | Seguridad                              | En usuario: nombre, email y password no pueden estar vacíos; email debe ser válido; rol y activo no ...  |
| REGLA-076 | Producción (RPC)                       | En ReporteProduccion: fecha_reporte, responsable_id y estado son obligatorios.                           |
| REGLA-077 | Producción (RPC)                       | En DetalleRPC: reporte_id, partida_id y cantidad_reportada son obligatorios.                             |
| REGLA-078 | Seguridad                              | Para login: email debe ser válido y no vacío; password es obligatoria.                                   |
| REGLA-079 | Seguridad                              | Para registro: nombreCompleto obligatorio; email válido y obligatorio; password obligatoria con tama...  |
| REGLA-080 | Marketing                              | Para crear lead público: nombreContacto obligatorio; email válido si se provee; límites de tamaño.       |
| REGLA-081 | Producción (RPC)                       | En RPC request: fechaReporte y responsableId obligatorios; debe incluir al menos un detalle.             |
| REGLA-082 | Producción (RPC)                       | En detalle RPC request: partidaId y cantidad reportada obligatorias; cantidad positiva.                  |
| REGLA-083 | Producción (RPC)                       | En aprobación RPC request: aprobadorId es obligatorio.                                                   |
| REGLA-084 | Producción (RPC)                       | En rechazo RPC request: aprobadorId y motivo son obligatorios.                                           |
| REGLA-085 | Inventarios                            | Para registrar movimiento de almacén: almacenId, recursoId, tipoMovimiento, fechaMovimiento, cantida...  |
| REGLA-086 | Reajuste                               | Para calcular reajuste: proyectoId, presupuestoId, fechaCorte, códigos y fechas de índice base/actua...  |
| REGLA-087 | Estimaciones                           | Para generar estimación: fechas de corte/inicio/fin y detalles son obligatorios; porcentajes no nega...  |
| REGLA-088 | Estimaciones                           | En detalle de estimación request: partidaId, cantidadAvance y precioUnitario obligatorios; no negati...  |
| REGLA-089 | Cronograma                             | Para programar actividad: partidaId, fechaInicio y fechaFin son obligatorios.                            |
| REGLA-090 | RRHH / Sobrecostos                     | En configuración laboral request: días no negativos; porcentaje seguridad social entre 0 y 100; días...  |
| REGLA-091 | Avance                                 | Para registrar avance físico: fecha y metradoEjecutado obligatorios; metradoEjecutado positivo.          |
| REGLA-092 | Compras                                | Para registrar compra: proyectoId, fecha, proveedor y detalles obligatorios.                             |
| REGLA-093 | Compras                                | En detalle de compra request: recursoId, partidaId, cantidad y precioUnitario obligatorios; cantidad...  |
| REGLA-094 | APU                                    | Para crear APU: lista de insumos obligatoria.                                                            |
| REGLA-095 | APU                                    | En insumo APU request: recursoId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario ...  |
| REGLA-096 | Partidas                               | Para crear partida: presupuestoId, item, descripcion y nivel obligatorios; metrado no negativo.          |
| REGLA-097 | Proyectos                              | Para crear proyecto: nombre obligatorio.                                                                 |
| REGLA-098 | Presupuesto                            | Para crear presupuesto: proyectoId y nombre obligatorios.                                                |
| REGLA-099 | Recursos                               | Para crear recurso: nombre, tipo y unidadBase obligatorios.                                              |
| REGLA-126 | Avance                                 | El avance físico: partidaId obligatorio, fecha obligatoria, metradoEjecutado no negativo.                |
| REGLA-127 | Valuación                              | La valuación requiere proyectoId, fechaCorte, código no vacío y estado no nulo.                          |
| REGLA-128 | Recursos                               | No se puede crear un recurso con el mismo nombre normalizado si ya existe.                               |
| REGLA-129 | Recursos                               | El tipo de recurso debe ser un valor válido del enum TipoRecurso.                                        |
| REGLA-130 | Estimaciones                           | La estimación es única por proyecto y número de estimación.                                              |
| REGLA-131 | Estimaciones                           | El detalle de estimación es único por (estimacion_id, partida_id).                                       |
| REGLA-132 | Cronograma                             | Un programa de obra es único por proyecto (proyecto_id UNIQUE).                                          |
| REGLA-133 | Cronograma                             | Una actividad programada es única por partida (partida_id UNIQUE).                                       |
| REGLA-134 | Inventarios                            | En movimiento_almacen, el tipo y tipo_movimiento deben estar en {ENTRADA, SALIDA, DEVOLUCION}.           |
| REGLA-135 | Cambios                                | En orden_cambio, tipo debe estar en {ADICIONAL, DEDUCTIVO, PRECIO, PLAZO} y estado en {SOLICITADO, A...  |
| REGLA-136 | Inventarios                            | En almacén, el código es único por proyecto.                                                             |
| REGLA-137 | Inventarios                            | En stock_actual, la combinación (almacen_id, recurso_id) es única.                                       |
| REGLA-138 | Seguridad                              | JWT expira en 24 horas por defecto (jwt.expiration-hours: 24).                                           |
| REGLA-139 | Producción (RPC)                       | En RPC legacy request: fechaReporte obligatoria y al menos un item.                                      |
| REGLA-140 | Producción (RPC)                       | En item RPC legacy: partidaId y cantidad obligatorias; cantidad positiva.                                |
| REGLA-141 | Producción (RPC)                       | En rechazo RPC legacy: motivo obligatorio.                                                               |
| REGLA-142 | Avance                                 | El acumulado no debería superar el metrado total de la partida (alertar, no bloquear).                   |
| REGLA-143 | Presupuesto                            | El presupuesto de línea base es único cuando es_linea_base = true.                                       |
| REGLA-144 | Recursos                               | En DTO de recurso, atributos se inicializan como mapa vacío si es nulo.                                  |

## Coverage by Module

| Module                  | Total Rules | Documented | Coverage % |
| ----------------------- | ----------- | ---------- | ---------- |
| APU                     | 5           | 1          | 20.0%      |
| Alertas Paramétricas    | 3           | 0          | 0.0%       |
| Arquitectura            | 1           | 1          | 100.0%     |
| Auditoría               | 3           | 2          | 66.7%      |
| Avance                  | 4           | 0          | 0.0%       |
| Billetera               | 2           | 0          | 0.0%       |
| Cambios                 | 14          | 12         | 85.7%      |
| Compras                 | 11          | 6          | 54.5%      |
| Configuración           | 1           | 0          | 0.0%       |
| Consumos                | 1           | 0          | 0.0%       |
| Cronograma              | 13          | 4          | 30.8%      |
| Estimaciones            | 13          | 0          | 0.0%       |
| Inteligencia Maquinaria | 2           | 0          | 0.0%       |
| Inventario              | 1           | 0          | 0.0%       |
| Inventarios             | 16          | 8          | 50.0%      |
| Marketing               | 3           | 0          | 0.0%       |
| Partidas                | 7           | 0          | 0.0%       |
| Presupuesto             | 22          | 14         | 63.6%      |
| Producción              | 2           | 2          | 100.0%     |
| Producción (RPC)        | 20          | 0          | 0.0%       |
| Proyectos               | 17          | 12         | 70.6%      |
| RRHH                    | 10          | 6          | 60.0%      |
| Reajuste                | 1           | 0          | 0.0%       |
| Recursos                | 8           | 0          | 0.0%       |
| Seguridad               | 9           | 0          | 0.0%       |
| Sobrecostos             | 2           | 0          | 0.0%       |
| Valuación               | 1           | 0          | 0.0%       |

## Recommendations

1.  **Backfill Canonical Specs**: The 118 missing rules should be explicitly added to their respective `_SPECS.md` files.
2.  **Enforce Traceability**: Use the `REGLA-XXX` tags in the documentation to allow automated auditing in the future.
3.  **Sync Code and Docs**: Ensure that any change in validators is reflected in the canonical documents.
