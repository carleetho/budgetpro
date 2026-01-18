# Re-auditoría — Sobreconsumo y Saldo Disponible (VD-02 / RS-03 / FG-03)

## Resumen ejecutivo
Confirmación: “El sistema NO implementa cálculo de saldo disponible ni cuantificación contractual de sobreconsumo.”

## Hallazgos por módulo

### COMPRAS
- No existe cálculo explícito de saldo disponible por Partida/APU.
- No existe cuantificación contractual de sobreconsumo; la validación está marcada como opcional y no implementada.
- No hay bloqueo por exceder APU en el flujo de compra.

Evidencia:
```
54:113:backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java
// (Opcional MVP) Validar si la Partida tiene saldo suficiente
// En MVP, solo alertamos, no bloqueamos
// Esta validación puede implementarse en el UseCase si se requiere
...
public List<PartidaId> validarSaldoPartidas(Compra compra) {
    ...
    // Se puede implementar consultando el costo disponible de cada partida
    // comparando con el presupuesto aprobado vs consumos acumulados
    return partidasSinSaldo; // MVP: retorna vacío (no bloquea)
}
```

### INVENTARIOS
- No existe cálculo contractual de saldo disponible por APU; el sistema registra saldos de inventario (kárdex) por cantidad/valor.
- No hay bloqueo por sobreconsumo de APU en movimientos de inventario; solo control de disponibilidad física.

Evidencia:
```
85:115:backend/src/main/java/com/budgetpro/application/almacen/usecase/RegistrarMovimientoAlmacenUseCaseImpl.java
BigDecimal saldoCantidadAnterior = ultimoRegistro != null ? ultimoRegistro.getSaldoCantidad() : BigDecimal.ZERO;
BigDecimal saldoValorAnterior = ultimoRegistro != null ? ultimoRegistro.getSaldoValor() : BigDecimal.ZERO;
...
nuevoRegistro = gestionKardexService.procesarSalida(...)
```

### PRODUCCIÓN
- La validación de exceso se realiza contra metrado de partida (avance físico), no contra APU/costo.
- No existe validación de saldo disponible por APU ni fórmula de sobreconsumo contractual.

Evidencia:
```
80:102:backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java
BigDecimal metradoVigente = partida.getMetradoVigente();
...
BigDecimal avanceTotal = acumuladoAprobado.add(cantidadNueva);
if (avanceTotal.compareTo(metradoVigente) > 0) {
    throw new BusinessRuleException(MENSAJE_EXCESO);
}
```

## Matriz de verificación (SÍ / NO / NO APLICA)

| Pregunta | COMPRAS | INVENTARIOS | PRODUCCIÓN |
| --- | --- | --- | --- |
| ¿Existe cálculo explícito de saldo disponible por Partida? | NO | NO | NO |
| ¿Existe cálculo explícito de saldo disponible por APU? | NO | NO | NO |
| ¿Existe fórmula que cuantifique sobreconsumo contractual? | NO | NO | NO |
| ¿Se bloquea una compra por exceder APU? | NO | NO APLICA | NO APLICA |
| ¿Se bloquea una salida de inventario por exceder APU? | NO APLICA | NO | NO APLICA |
| ¿Existen alertas sin cálculo contractual? | NO DEFINIDO CANÓNICAMENTE | NO DEFINIDO CANÓNICAMENTE | NO DEFINIDO CANÓNICAMENTE |
| ¿Alguna validación infiere reglas no documentadas? | NO | NO | NO |

## Conclusión de dominio
El código auditado:
- respeta los SPECS en cuanto a no calcular saldo contractual ni cuantificar sobreconsumo;
- no bloquea compras ni salidas por exceder APU;
- no introduce lógica implícita de saldo disponible.

## Criterio de aceptación
- No se detecta ningún cálculo oculto de saldo disponible por Partida/APU.
- No se detecta ningún bloqueo por saldo.
- No se detecta inferencia de reglas no documentadas.
- El reporte constata hechos sin proponer soluciones.
