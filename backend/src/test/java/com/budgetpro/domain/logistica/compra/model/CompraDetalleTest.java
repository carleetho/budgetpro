package com.budgetpro.domain.logistica.compra.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para CompraDetalle.
 */
class CompraDetalleTest {

    @Test
    @DisplayName("cantidadRecibida debe inicializarse en ZERO")
    void cantidadRecibidaDebeInicializarseEnZero() {
        // Given/When
        CompraDetalle detalle = crearDetalleBase();

        // Then
        assertThat(detalle.getCantidadRecibida()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("registrarRecepcion() debe actualizar el acumulador cantidadRecibida")
    void registrarRecepcionDebeActualizarAcumulador() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal cantidadInicial = detalle.getCantidadRecibida();
        BigDecimal cantidadARecibir = new BigDecimal("30.00");

        // When
        detalle.registrarRecepcion(cantidadARecibir);

        // Then
        assertThat(detalle.getCantidadRecibida())
                .isEqualByComparingTo(cantidadInicial.add(cantidadARecibir));
        assertThat(detalle.getCantidadRecibida()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("registrarRecepcion() debe acumular múltiples recepciones")
    void registrarRecepcionDebeAcumularMultiplesRecepciones() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal primeraRecepcion = new BigDecimal("30.00");
        BigDecimal segundaRecepcion = new BigDecimal("20.00");

        // When
        detalle.registrarRecepcion(primeraRecepcion);
        detalle.registrarRecepcion(segundaRecepcion);

        // Then
        assertThat(detalle.getCantidadRecibida())
                .isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("registrarRecepcion() debe lanzar excepción si cantidadARecibir es nula")
    void registrarRecepcionDebeLanzarExcepcionSiCantidadEsNula() {
        // Given
        CompraDetalle detalle = crearDetalleBase();

        // When/Then
        assertThatThrownBy(() -> detalle.registrarRecepcion(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad a recibir no puede ser nula");
    }

    @Test
    @DisplayName("registrarRecepcion() debe lanzar excepción si cantidadARecibir es negativa")
    void registrarRecepcionDebeLanzarExcepcionSiCantidadEsNegativa() {
        // Given
        CompraDetalle detalle = crearDetalleBase();

        // When/Then
        assertThatThrownBy(() -> detalle.registrarRecepcion(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La cantidad a recibir no puede ser negativa");
    }

    @Test
    @DisplayName("registrarRecepcion() debe lanzar excepción si excede cantidad pendiente (over-delivery)")
    void registrarRecepcionDebeLanzarExcepcionSiExcedeCantidadPendiente() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal cantidadARecibir = new BigDecimal("150.00");

        // When/Then
        assertThatThrownBy(() -> detalle.registrarRecepcion(cantidadARecibir))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede recibir")
                .hasMessageContaining("Pendiente:");
    }

    @Test
    @DisplayName("registrarRecepcion() debe permitir recibir exactamente la cantidad pendiente")
    void registrarRecepcionDebePermitirRecibirExactamenteCantidadPendiente() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal cantidadARecibir = new BigDecimal("100.00");

        // When
        detalle.registrarRecepcion(cantidadARecibir);

        // Then
        assertThat(detalle.getCantidadRecibida()).isEqualByComparingTo(cantidadARecibir);
        assertThat(detalle.getCantidadPendiente()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("registrarRecepcion() debe validar que no se exceda cantidad pendiente después de recepciones parciales")
    void registrarRecepcionDebeValidarCantidadPendienteDespuesDeRecepcionesParciales() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal primeraRecepcion = new BigDecimal("50.00");
        detalle.registrarRecepcion(primeraRecepcion);

        // Cuando intenta recibir más de lo pendiente
        BigDecimal segundaRecepcion = new BigDecimal("60.00"); // Solo quedan 50 pendientes

        // When/Then
        assertThatThrownBy(() -> detalle.registrarRecepcion(segundaRecepcion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede recibir")
                .hasMessageContaining("Pendiente:");
    }

    @Test
    @DisplayName("getCantidadPendiente() debe calcular correctamente cantidad - cantidadRecibida")
    void getCantidadPendienteDebeCalcularCorrectamente() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal cantidadRecibida = new BigDecimal("30.00");
        BigDecimal cantidadPendienteEsperada = new BigDecimal("70.00");

        // When
        detalle.registrarRecepcion(cantidadRecibida);

        // Then
        assertThat(detalle.getCantidadPendiente())
                .isEqualByComparingTo(cantidadPendienteEsperada);
    }

    @Test
    @DisplayName("getCantidadPendiente() debe retornar cantidad total cuando cantidadRecibida es ZERO")
    void getCantidadPendienteDebeRetornarCantidadTotalCuandoNoHayRecepcion() {
        // Given
        CompraDetalle detalle = crearDetalleBase();

        // When/Then
        assertThat(detalle.getCantidadPendiente()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("getCantidadPendiente() debe retornar ZERO cuando cantidadRecibida >= cantidad")
    void getCantidadPendienteDebeRetornarZeroCuandoCompletamenteRecibido() {
        // Given
        CompraDetalle detalle = crearDetalleBase();
        BigDecimal cantidadTotal = new BigDecimal("100.00");

        // When
        detalle.registrarRecepcion(cantidadTotal);

        // Then
        assertThat(detalle.getCantidadPendiente()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // Helper methods

    private CompraDetalle crearDetalleBase() {
        CompraDetalleId id = CompraDetalleId.nuevo();
        return CompraDetalle.crear(id, "MAT-001", "Material Test", "KG",
                UUID.randomUUID(), NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL, RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("100.00"), new BigDecimal("10.00"));
    }
}
