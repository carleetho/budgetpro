package com.budgetpro.domain.logistica.compra.model;

import com.budgetpro.application.compra.exception.BusinessRuleException;
import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para el agregado Recepcion.
 */
class RecepcionTest {

    @Test
    @DisplayName("crear() debe crear una recepción correctamente")
    void crearDebeCrearRecepcionCorrectamente() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When
        Recepcion recepcion = Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, detalles, usuarioId);

        // Then
        assertThat(recepcion.getId()).isEqualTo(id);
        assertThat(recepcion.getCompraId()).isEqualTo(compraId);
        assertThat(recepcion.getFechaRecepcion()).isEqualTo(fechaRecepcion);
        assertThat(recepcion.getGuiaRemision()).isEqualTo(guiaRemision);
        assertThat(recepcion.getDetalles()).hasSize(2);
        assertThat(recepcion.getCreadoPorUsuarioId()).isEqualTo(usuarioId);
        assertThat(recepcion.getVersion()).isEqualTo(0L);
        assertThat(recepcion.getFechaCreacion()).isNotNull();
    }

    @Test
    @DisplayName("crear() debe normalizar guiaRemision (trim)")
    void crearDebeNormalizarGuiaRemision() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemisionConEspacios = "  GR-2026-001234  ";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When
        Recepcion recepcion = Recepcion.crear(id, compraId, fechaRecepcion, guiaRemisionConEspacios, detalles, usuarioId);

        // Then
        assertThat(recepcion.getGuiaRemision()).isEqualTo("GR-2026-001234");
    }

    @Test
    @DisplayName("crear() debe lanzar BusinessRuleException si guiaRemision es nula")
    void crearDebeLanzarExcepcionSiGuiaRemisionEsNula() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, null, detalles, usuarioId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La Guía de Remisión es obligatoria para recepción de bienes físicos");
    }

    @Test
    @DisplayName("crear() debe lanzar BusinessRuleException si guiaRemision está en blanco")
    void crearDebeLanzarExcepcionSiGuiaRemisionEstaEnBlanco() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, "  ", detalles, usuarioId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La Guía de Remisión es obligatoria para recepción de bienes físicos");

        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, "", detalles, usuarioId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("La Guía de Remisión es obligatoria para recepción de bienes físicos");
    }

    @Test
    @DisplayName("crear() debe lanzar IllegalArgumentException si detalles está vacía")
    void crearDebeLanzarExcepcionSiDetallesEstaVacia() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detallesVacios = new ArrayList<>();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, detallesVacios, usuarioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La recepción debe tener al menos un detalle");
    }

    @Test
    @DisplayName("crear() debe lanzar IllegalArgumentException si detalles es nula")
    void crearDebeLanzarExcepcionSiDetallesEsNula() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, null, usuarioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La recepción debe tener al menos un detalle");
    }

    @Test
    @DisplayName("crear() debe poblar campos de auditoría (creadoPorUsuarioId y fechaCreacion)")
    void crearDebePoblarCamposAuditoria() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When
        Recepcion recepcion = Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, detalles, usuarioId);

        // Then
        assertThat(recepcion.getCreadoPorUsuarioId()).isEqualTo(usuarioId);
        assertThat(recepcion.getFechaCreacion()).isNotNull();
        assertThat(recepcion.getFechaCreacion()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("getDetalles() debe retornar copia defensiva (inmutable)")
    void getDetallesDebeRetornarCopiaDefensiva() {
        // Given
        Recepcion recepcion = crearRecepcionBase();
        List<RecepcionDetalle> detalles1 = recepcion.getDetalles();
        List<RecepcionDetalle> detalles2 = recepcion.getDetalles();

        // Then
        assertThat(detalles1).isNotSameAs(detalles2);
        assertThat(detalles1).isEqualTo(detalles2);
        
        // Verificar que es inmutable intentando modificarlo
        assertThatThrownBy(() -> detalles1.add(crearDetalleAdicional()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("crear() debe lanzar NullPointerException si id es nulo")
    void crearDebeLanzarExcepcionSiIdEsNulo() {
        // Given
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(null, compraId, fechaRecepcion, guiaRemision, detalles, usuarioId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("El ID de la recepción no puede ser nulo");
    }

    @Test
    @DisplayName("crear() debe lanzar NullPointerException si compraId es nulo")
    void crearDebeLanzarExcepcionSiCompraIdEsNulo() {
        // Given
        RecepcionId id = RecepcionId.generate();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, null, fechaRecepcion, guiaRemision, detalles, usuarioId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("El compraId no puede ser nulo");
    }

    @Test
    @DisplayName("crear() debe lanzar NullPointerException si fechaRecepcion es nula")
    void crearDebeLanzarExcepcionSiFechaRecepcionEsNula() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, null, guiaRemision, detalles, usuarioId))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("La fecha de recepción no puede ser nula");
    }

    @Test
    @DisplayName("crear() debe lanzar NullPointerException si creadoPorUsuarioId es nulo")
    void crearDebeLanzarExcepcionSiCreadoPorUsuarioIdEsNulo() {
        // Given
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();

        // When/Then
        assertThatThrownBy(() -> Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, detalles, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("El creadoPorUsuarioId no puede ser nulo");
    }

    // Helper methods

    private Recepcion crearRecepcionBase() {
        RecepcionId id = RecepcionId.generate();
        CompraId compraId = CompraId.nuevo();
        LocalDate fechaRecepcion = LocalDate.now();
        String guiaRemision = "GR-2026-001234";
        List<RecepcionDetalle> detalles = crearDetallesBase();
        UUID usuarioId = UUID.randomUUID();

        return Recepcion.crear(id, compraId, fechaRecepcion, guiaRemision, detalles, usuarioId);
    }

    private List<RecepcionDetalle> crearDetallesBase() {
        RecepcionDetalleId id1 = RecepcionDetalleId.generate();
        RecepcionDetalleId id2 = RecepcionDetalleId.generate();
        UUID compraDetalleId1 = UUID.randomUUID();
        UUID compraDetalleId2 = UUID.randomUUID();
        UUID recursoId1 = UUID.randomUUID();
        UUID recursoId2 = UUID.randomUUID();
        AlmacenId almacenId = AlmacenId.generate();

        RecepcionDetalle detalle1 = RecepcionDetalle.crear(id1, compraDetalleId1, recursoId1, almacenId,
                new BigDecimal("50.00"), new BigDecimal("10.00"), MovimientoAlmacenId.generate());
        RecepcionDetalle detalle2 = RecepcionDetalle.crear(id2, compraDetalleId2, recursoId2, almacenId,
                new BigDecimal("30.00"), new BigDecimal("15.00"), MovimientoAlmacenId.generate());

        return List.of(detalle1, detalle2);
    }

    private RecepcionDetalle crearDetalleAdicional() {
        RecepcionDetalleId id = RecepcionDetalleId.generate();
        UUID compraDetalleId = UUID.randomUUID();
        UUID recursoId = UUID.randomUUID();
        AlmacenId almacenId = AlmacenId.generate();

        return RecepcionDetalle.crear(id, compraDetalleId, recursoId, almacenId,
                new BigDecimal("20.00"), new BigDecimal("12.00"), MovimientoAlmacenId.generate());
    }
}
