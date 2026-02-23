package com.budgetpro.domain.logistica.compra.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para el agregado Compra.
 */
class CompraTest {

    @Test
    @DisplayName("enviar() debe transicionar APROBADA → ENVIADA")
    void enviarDebeTransicionarAprobadaAEnviada() {
        // Given
        Compra compra = crearCompraEnEstado(EstadoCompra.APROBADA);

        // When
        compra.enviar();

        // Then
        assertThat(compra.getEstado()).isEqualTo(EstadoCompra.ENVIADA);
    }

    @Test
    @DisplayName("enviar() debe lanzar excepción si no está en estado APROBADA")
    void enviarDebeLanzarExcepcionSiNoEstaAprobada() {
        // Given
        Compra compraBorrador = crearCompraEnEstado(EstadoCompra.BORRADOR);
        Compra compraEnviada = crearCompraEnEstado(EstadoCompra.ENVIADA);
        Compra compraParcial = crearCompraEnEstado(EstadoCompra.PARCIAL);
        Compra compraRecibida = crearCompraEnEstado(EstadoCompra.RECIBIDA);

        // When/Then
        assertThatThrownBy(() -> compraBorrador.enviar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede enviar una compra aprobada");

        assertThatThrownBy(() -> compraEnviada.enviar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede enviar una compra aprobada");

        assertThatThrownBy(() -> compraParcial.enviar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede enviar una compra aprobada");

        assertThatThrownBy(() -> compraRecibida.enviar())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede enviar una compra aprobada");
    }

    @Test
    @DisplayName("marcarComoParcialmenteRecibida() debe transicionar desde ENVIADA")
    void marcarComoParcialmenteRecibidaDebeTransicionarDesdeEnviada() {
        // Given
        Compra compra = crearCompraEnEstado(EstadoCompra.ENVIADA);

        // When
        compra.marcarComoParcialmenteRecibida();

        // Then
        assertThat(compra.getEstado()).isEqualTo(EstadoCompra.PARCIAL);
    }

    @Test
    @DisplayName("marcarComoParcialmenteRecibida() debe permitir transición desde PARCIAL")
    void marcarComoParcialmenteRecibidaDebePermitirTransicionDesdeParcial() {
        // Given
        Compra compra = crearCompraEnEstado(EstadoCompra.PARCIAL);

        // When
        compra.marcarComoParcialmenteRecibida();

        // Then
        assertThat(compra.getEstado()).isEqualTo(EstadoCompra.PARCIAL);
    }

    @Test
    @DisplayName("marcarComoParcialmenteRecibida() debe lanzar excepción si no está en ENVIADA o PARCIAL")
    void marcarComoParcialmenteRecibidaDebeLanzarExcepcionSiEstadoInvalido() {
        // Given
        Compra compraBorrador = crearCompraEnEstado(EstadoCompra.BORRADOR);
        Compra compraAprobada = crearCompraEnEstado(EstadoCompra.APROBADA);
        Compra compraRecibida = crearCompraEnEstado(EstadoCompra.RECIBIDA);

        // When/Then
        assertThatThrownBy(() -> compraBorrador.marcarComoParcialmenteRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción parcial");

        assertThatThrownBy(() -> compraAprobada.marcarComoParcialmenteRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción parcial");

        assertThatThrownBy(() -> compraRecibida.marcarComoParcialmenteRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción parcial");
    }

    @Test
    @DisplayName("marcarComoRecibida() debe transicionar desde ENVIADA")
    void marcarComoRecibidaDebeTransicionarDesdeEnviada() {
        // Given
        Compra compra = crearCompraEnEstado(EstadoCompra.ENVIADA);

        // When
        compra.marcarComoRecibida();

        // Then
        assertThat(compra.getEstado()).isEqualTo(EstadoCompra.RECIBIDA);
    }

    @Test
    @DisplayName("marcarComoRecibida() debe transicionar desde PARCIAL")
    void marcarComoRecibidaDebeTransicionarDesdeParcial() {
        // Given
        Compra compra = crearCompraEnEstado(EstadoCompra.PARCIAL);

        // When
        compra.marcarComoRecibida();

        // Then
        assertThat(compra.getEstado()).isEqualTo(EstadoCompra.RECIBIDA);
    }

    @Test
    @DisplayName("marcarComoRecibida() debe lanzar excepción si no está en ENVIADA o PARCIAL")
    void marcarComoRecibidaDebeLanzarExcepcionSiEstadoInvalido() {
        // Given
        Compra compraBorrador = crearCompraEnEstado(EstadoCompra.BORRADOR);
        Compra compraAprobada = crearCompraEnEstado(EstadoCompra.APROBADA);
        Compra compraRecibida = crearCompraEnEstado(EstadoCompra.RECIBIDA);

        // When/Then
        assertThatThrownBy(() -> compraBorrador.marcarComoRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción completa");

        assertThatThrownBy(() -> compraAprobada.marcarComoRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción completa");

        assertThatThrownBy(() -> compraRecibida.marcarComoRecibida())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inválido para recepción completa");
    }

    @Test
    @DisplayName("estaCompletamenteRecibida() debe retornar true cuando todos los detalles tienen cantidadRecibida >= cantidad")
    void estaCompletamenteRecibidaDebeRetornarTrueCuandoTodosLosDetallesEstanCompletos() {
        // Given
        CompraDetalle detalle1 = crearDetalleConRecepcion(new BigDecimal("100"), new BigDecimal("100"));
        CompraDetalle detalle2 = crearDetalleConRecepcion(new BigDecimal("50"), new BigDecimal("50"));
        Compra compra = crearCompraConDetalles(List.of(detalle1, detalle2));

        // When
        boolean resultado = compra.estaCompletamenteRecibida();

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("estaCompletamenteRecibida() debe retornar true cuando cantidadRecibida > cantidad")
    void estaCompletamenteRecibidaDebeRetornarTrueCuandoCantidadRecibidaEsMayor() {
        // Given
        CompraDetalle detalle = crearDetalleConRecepcion(new BigDecimal("100"), new BigDecimal("150"));
        Compra compra = crearCompraConDetalles(List.of(detalle));

        // When
        boolean resultado = compra.estaCompletamenteRecibida();

        // Then
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("estaCompletamenteRecibida() debe retornar false cuando algún detalle tiene cantidadRecibida < cantidad")
    void estaCompletamenteRecibidaDebeRetornarFalseCuandoHayDetallesPendientes() {
        // Given
        CompraDetalle detalle1 = crearDetalleConRecepcion(new BigDecimal("100"), new BigDecimal("100"));
        CompraDetalle detalle2 = crearDetalleConRecepcion(new BigDecimal("50"), new BigDecimal("30"));
        Compra compra = crearCompraConDetalles(List.of(detalle1, detalle2));

        // When
        boolean resultado = compra.estaCompletamenteRecibida();

        // Then
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("estaCompletamenteRecibida() debe retornar false cuando ningún detalle ha sido recibido")
    void estaCompletamenteRecibidaDebeRetornarFalseCuandoNingunDetalleRecibido() {
        // Given
        CompraDetalle detalle1 = crearDetalleSinRecepcion(new BigDecimal("100"));
        CompraDetalle detalle2 = crearDetalleSinRecepcion(new BigDecimal("50"));
        Compra compra = crearCompraConDetalles(List.of(detalle1, detalle2));

        // When
        boolean resultado = compra.estaCompletamenteRecibida();

        // Then
        assertThat(resultado).isFalse();
    }

    // Helper methods

    private Compra crearCompraEnEstado(EstadoCompra estado) {
        CompraId id = CompraId.nuevo();
        UUID proyectoId = UUID.randomUUID();
        LocalDate fecha = LocalDate.now();
        String proveedor = "Proveedor Test";
        CompraDetalle detalle = crearDetalleSinRecepcion(new BigDecimal("100"));
        List<CompraDetalle> detalles = List.of(detalle);

        return Compra.reconstruir(id, proyectoId, fecha, proveedor, estado, 
                new BigDecimal("1000.00"), 1L, detalles);
    }

    private Compra crearCompraConDetalles(List<CompraDetalle> detalles) {
        CompraId id = CompraId.nuevo();
        UUID proyectoId = UUID.randomUUID();
        LocalDate fecha = LocalDate.now();
        String proveedor = "Proveedor Test";

        return Compra.crear(id, proyectoId, fecha, proveedor, detalles);
    }

    private CompraDetalle crearDetalleSinRecepcion(BigDecimal cantidad) {
        CompraDetalleId id = CompraDetalleId.nuevo();
        return CompraDetalle.crear(id, "MAT-001", "Material Test", "KG",
                UUID.randomUUID(), NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL, RubroInsumo.MATERIAL_CONSTRUCCION,
                cantidad, new BigDecimal("10.00"));
    }

    private CompraDetalle crearDetalleConRecepcion(BigDecimal cantidad, BigDecimal cantidadRecibida) {
        CompraDetalleId id = CompraDetalleId.nuevo();
        UUID partidaId = UUID.randomUUID();
        
        // Simular recepción usando el método reconstruir con cantidadRecibida
        return CompraDetalle.reconstruir(id, "MAT-001", "Material Test", "KG",
                partidaId, NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL, RubroInsumo.MATERIAL_CONSTRUCCION,
                cantidad, new BigDecimal("10.00"),
                cantidad.multiply(new BigDecimal("10.00")), cantidadRecibida);
    }
}
