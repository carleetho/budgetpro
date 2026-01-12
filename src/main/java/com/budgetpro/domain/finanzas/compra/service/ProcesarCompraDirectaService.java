package com.budgetpro.domain.finanzas.compra.service;

import com.budgetpro.domain.finanzas.compra.Compra;
import com.budgetpro.domain.finanzas.compra.DetalleCompra;
import com.budgetpro.domain.finanzas.compra.EstadoCompra;
import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.logistica.inventario.InventarioItem;
import com.budgetpro.domain.recurso.model.RecursoId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Domain Service que orquesta la lógica de negocio para procesar una compra directa.
 * 
 * Este servicio coordina la interacción entre los agregados Compra, Billetera e Inventario
 * para ejecutar una compra directa, garantizando que todas las invariantes de negocio
 * se cumplan en el orden correcto.
 * 
 * REGLAS DE NEGOCIO:
 * 1. El saldo nunca puede quedar negativo (validado antes de egresar)
 * 2. La Compra solo se confirma si todos los pasos se ejecutan correctamente
 * 3. El servicio coordina, los agregados ejecutan
 * 
 * Contexto: Logística & Costos
 * 
 * Este servicio es PURO (sin infraestructura, sin transacciones).
 * La persistencia y transacciones son responsabilidad del Use Case que lo invoca.
 * 
 * Nota: Se marca como @Service para permitir inyección de dependencias en Use Cases,
 * aunque el servicio mismo no tiene dependencias de infraestructura.
 */
@Service
public final class ProcesarCompraDirectaService {

    /**
     * Procesa una compra directa, orquestando los siguientes pasos:
     * 1. Validar que la billetera tenga saldo suficiente
     * 2. Registrar el egreso en la billetera
     * 3. Registrar los consumos en el inventario (uno por cada detalle de compra)
     * 4. Confirmar el estado de la compra
     * 
     * @param compra La compra a procesar (no puede ser nula)
     * @param billetera La billetera del proyecto (no puede ser nula)
     * @param inventariosPorRecurso Mapa de inventarios por recurso (no puede ser nulo)
     *                              Clave: RecursoId, Valor: InventarioItem
     * @throws IllegalArgumentException si algún parámetro es nulo o inválido
     * @throws SaldoInsuficienteException si la billetera no tiene saldo suficiente
     * @throws IllegalStateException si algún inventario no tiene stock suficiente o la compra no está en estado REGISTRADA
     */
    public void procesar(
            Compra compra,
            Billetera billetera,
            Map<com.budgetpro.domain.recurso.model.RecursoId, InventarioItem> inventariosPorRecurso) {
        
        // Validación de parámetros
        Objects.requireNonNull(compra, "La compra no puede ser nula");
        Objects.requireNonNull(billetera, "La billetera no puede ser nula");
        Objects.requireNonNull(inventariosPorRecurso, "El mapa de inventarios no puede ser nulo");

        // Validar que la compra esté en estado PENDIENTE
        if (compra.getEstado() != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                String.format("La compra %s no está en estado PENDIENTE. Estado actual: %s", 
                    compra.getId(), compra.getEstado())
            );
        }

        // Validar que la billetera corresponda al proyecto de la compra
        if (!billetera.getProyectoId().equals(compra.getProyectoId())) {
            throw new IllegalArgumentException(
                String.format("La billetera del proyecto %s no corresponde a la compra del proyecto %s",
                    billetera.getProyectoId(), compra.getProyectoId())
            );
        }

        // PASO 1: Validar que la billetera tenga saldo suficiente
        BigDecimal totalCompra = compra.getTotalAsMonto().getValue();
        if (!billetera.tieneSaldoSuficiente(totalCompra)) {
            throw new SaldoInsuficienteException(
                compra.getProyectoId(),
                billetera.getSaldoActual(),
                totalCompra
            );
        }

        // PASO 2: Registrar el egreso en la billetera
        String referenciaCompra = String.format("Compra %s", compra.getId());
        billetera.egresar(totalCompra, referenciaCompra, null);

        // PASO 3: Registrar los consumos en el inventario
        for (DetalleCompra detalle : compra.getDetalles()) {
            // Obtener el inventario correspondiente al recurso del detalle
            InventarioItem inventario = inventariosPorRecurso.get(detalle.getRecursoId());
            if (inventario == null) {
                throw new IllegalArgumentException(
                    String.format("No existe inventario para el recurso %s", detalle.getRecursoId())
                );
            }

            // Registrar el INGRESO en el inventario (cuando se compra, el stock aumenta)
            BigDecimal cantidad = detalle.getCantidad().getValue();
            inventario.registrarIngreso(cantidad);
        }

        // PASO 4: Confirmar el estado de la compra
        // Nota: La compra es inmutable, por lo que no podemos cambiar su estado directamente.
        // En un modelo más completo, la compra tendría un método procesar() que cambia su estado.
        // El cambio de estado se manejará en el Use Case después de persistir exitosamente.
    }
}
