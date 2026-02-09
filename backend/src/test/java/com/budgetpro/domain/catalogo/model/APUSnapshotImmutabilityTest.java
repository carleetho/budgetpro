package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class APUSnapshotImmutabilityTest {

    @Test
    void actualizarRendimiento_shouldReturnNewInstance() {
        // Given
        UUID partidaId = UUID.randomUUID();
        APUSnapshot original = APUSnapshot.crear(APUSnapshotId.generate(), partidaId, "APU-EXT", "CAPECO",
                BigDecimal.ONE, "UND", LocalDateTime.now());

        // When
        BigDecimal nuevoRendimiento = new BigDecimal("2.0");
        UUID usuarioId = UUID.randomUUID();
        APUSnapshot modified = original.actualizarRendimiento(nuevoRendimiento, usuarioId);

        // Then
        assertThat(modified).isNotSameAs(original);
        assertThat(original.getRendimientoVigente()).isEqualTo(BigDecimal.ONE);
        assertThat(modified.getRendimientoVigente()).isEqualTo(nuevoRendimiento);
        assertThat(modified.isRendimientoModificado()).isTrue();
        assertThat(modified.getRendimientoModificadoPor()).isEqualTo(usuarioId);

        // Audit fields in original should be empty/false
        assertThat(original.isRendimientoModificado()).isFalse();
        assertThat(original.getRendimientoModificadoPor()).isNull();
    }

    @Test
    void agregarInsumo_shouldReturnNewInstance() {
        // Given
        UUID partidaId = UUID.randomUUID();
        APUSnapshot original = APUSnapshot.crear(APUSnapshotId.generate(), partidaId, "APU-EXT", "CAPECO",
                BigDecimal.ONE, "UND", LocalDateTime.now());
        APUInsumoSnapshot insumo = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(), "MAT-001", "Cemento",
                BigDecimal.TEN, BigDecimal.ONE);

        // When
        APUSnapshot modified = original.agregarInsumo(insumo);

        // Then
        assertThat(modified).isNotSameAs(original);
        assertThat(original.getInsumos()).isEmpty();
        assertThat(modified.getInsumos()).hasSize(1);
        assertThat(modified.getInsumos().get(0)).isEqualTo(insumo);
    }

    @Test
    void recursoProxy_marcarObsoleto_shouldReturnNewInstance() {
        // Given
        RecursoProxy original = RecursoProxy.crear(RecursoProxyId.generate(), "MAT-001", "CAPECO", "Material Test",
                TipoRecurso.MATERIAL, "UND", BigDecimal.TEN, LocalDateTime.now());

        // When
        RecursoProxy modified = original.marcarObsoleto();

        // Then
        assertThat(modified).isNotSameAs(original);
        assertThat(original.getEstado()).isEqualTo(EstadoProxy.ACTIVO);
        assertThat(modified.getEstado()).isEqualTo(EstadoProxy.OBSOLETO);
    }

    @Test
    void recursoProxy_actualizarCostoReal_shouldReturnNewInstance() {
        // Given
        RecursoProxy original = RecursoProxy.crear(RecursoProxyId.generate(), "MAT-001", "CAPECO", "Material Test",
                TipoRecurso.MATERIAL, "UND", BigDecimal.TEN, LocalDateTime.now());

        // When
        BigDecimal nuevoCosto = new BigDecimal("15.00");
        RecursoProxy modified = original.actualizarCostoReal(nuevoCosto);

        // Then
        assertThat(modified).isNotSameAs(original);
        assertThat(original.getCostoReal()).isNull();
        assertThat(modified.getCostoReal()).isEqualTo(nuevoCosto);
    }
}
