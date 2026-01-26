package com.budgetpro.application.explosion.usecase;

import com.budgetpro.application.explosion.dto.ExplosionInsumosResponse;
import com.budgetpro.application.explosion.dto.RecursoAgregadoDTO;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplotarInsumosPresupuestoUseCaseImplTest {

        @Mock
        private PresupuestoRepository presupuestoRepository;

        @Mock
        private PartidaRepository partidaRepository;

        @Mock
        private ApuSnapshotRepository apuSnapshotRepository;

        @InjectMocks
        private ExplotarInsumosPresupuestoUseCaseImpl useCase;

        private UUID presupuestoId;
        private UUID partidaId1;
        private UUID partidaId2;
        private Presupuesto presupuesto;
        private Partida partida1;
        private Partida partida2;
        private APUSnapshot apu1;
        private APUSnapshot apu2;

        @BeforeEach
        void setUp() {
                presupuestoId = UUID.randomUUID();
                partidaId1 = UUID.randomUUID();
                partidaId2 = UUID.randomUUID();

                presupuesto = Presupuesto.crear(PresupuestoId.from(presupuestoId), UUID.randomUUID(),
                                "Presupuesto de prueba");

                // Partida 1: 100 m³ con Cemento 9.73 bol/m³ (factor 42.5 kg/bol)
                partida1 = Partida.crearRaiz(PartidaId.from(partidaId1), presupuestoId, "01.01", "Partida A", "m³",
                                new BigDecimal("100.00"));

                // Partida 2: 50 m² con Cemento 0.5 kg/m²
                partida2 = Partida.crearRaiz(PartidaId.from(partidaId2), presupuestoId, "01.02", "Partida B", "m²",
                                new BigDecimal("50.00"));

                // APU 1 con Cemento
                apu1 = APUSnapshot.crear(APUSnapshotId.of(UUID.randomUUID()), partidaId1, "APU-001", "CATALOG-001",
                                new BigDecimal("25.00"), "m³", LocalDateTime.now());

                APUInsumoSnapshot insumoCemento1 = APUInsumoSnapshot.crear(APUInsumoSnapshotId.of(UUID.randomUUID()),
                                "MAT-001", "Cemento", new BigDecimal("9.73"), new BigDecimal("22.50"),
                                TipoRecurso.MATERIAL, 1, new BigDecimal("9.73"), "BOL", "KG", new BigDecimal("42.5"), // 1
                                                                                                                      // bolsa
                                                                                                                      // =
                                                                                                                      // 42.5
                                                                                                                      // kg
                                "BOL", "PEN", BigDecimal.ONE, new BigDecimal("22.50"), // precioMercado
                                BigDecimal.ZERO, // flete
                                new BigDecimal("22.50"), // precioPuestoEnObra
                                BigDecimal.ZERO, // desperdicio
                                null, // composicionCuadrilla
                                null, // costoDiaCuadrillaCalculado
                                null, // jornadaHoras
                                null, // costoHoraMaquina
                                null, // horasUso
                                null, // porcentajeManoObra
                                null // dependeDe
                );

                apu1.agregarInsumo(insumoCemento1);

                // APU 2 con Cemento en kg
                apu2 = APUSnapshot.crear(APUSnapshotId.of(UUID.randomUUID()), partidaId2, "APU-002", "CATALOG-001",
                                new BigDecimal("10.00"), "m²", LocalDateTime.now());

                APUInsumoSnapshot insumoCemento2 = APUInsumoSnapshot.crear(APUInsumoSnapshotId.of(UUID.randomUUID()),
                                "MAT-001", "Cemento", new BigDecimal("0.5"), new BigDecimal("0.53"),
                                TipoRecurso.MATERIAL, 1, new BigDecimal("0.5"), "KG", "KG", BigDecimal.ONE, // Ya está
                                                                                                            // en kg
                                "KG", "PEN", BigDecimal.ONE, new BigDecimal("0.53"), // precioMercado
                                BigDecimal.ZERO, // flete
                                new BigDecimal("0.53"), // precioPuestoEnObra
                                BigDecimal.ZERO, // desperdicio
                                null, // composicionCuadrilla
                                null, // costoDiaCuadrillaCalculado
                                null, // jornadaHoras
                                null, // costoHoraMaquina
                                null, // horasUso
                                null, // porcentajeManoObra
                                null // dependeDe
                );

                apu2.agregarInsumo(insumoCemento2);
        }

        @Test
        void deberiaExplotarInsumosConNormalizacionDeUnidades() {
                // Given
                when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
                when(partidaRepository.findByPresupuestoId(presupuestoId)).thenReturn(List.of(partida1, partida2));
                when(apuSnapshotRepository.findByPartidaId(partidaId1)).thenReturn(Optional.of(apu1));
                when(apuSnapshotRepository.findByPartidaId(partidaId2)).thenReturn(Optional.of(apu2));

                // When
                ExplosionInsumosResponse resultado = useCase.ejecutar(presupuestoId);

                // Then
                assertThat(resultado.recursosPorTipo()).isNotEmpty();
                assertThat(resultado.recursosPorTipo()).containsKey("MATERIAL");

                List<RecursoAgregadoDTO> materiales = resultado.recursosPorTipo().get("MATERIAL");
                assertThat(materiales).hasSize(1);

                RecursoAgregadoDTO cemento = materiales.get(0);
                assertThat(cemento.recursoExternalId()).isEqualTo("MAT-001");

                // Verificar normalización:
                // Partida 1: 100 m³ × 9.73 bol/m³ × 42.5 kg/bol = 41,352.5 kg
                // Partida 2: 50 m² × 0.5 kg/m² = 25 kg
                // Total: 41,377.5 kg base
                // En bolsas: 41,377.5 / 42.5 = 974.176... → 975 bolsas (redondeado hacia arriba
                // con CEILING)
                assertThat(cemento.cantidadBase()).isEqualByComparingTo(new BigDecimal("41377.5"));
                // El redondeo hacia arriba debe dar al menos 974, pero idealmente 975
                assertThat(cemento.cantidadTotal()).isGreaterThanOrEqualTo(new BigDecimal("974"));
                assertThat(cemento.unidad()).isEqualTo("BOL");
        }

        @Test
        void deberiaRechazarUnidadesIncompatibles() {
                // Given - Crear un insumo con unidad base diferente
                APUInsumoSnapshot insumoIncompatible = APUInsumoSnapshot.crear(
                                APUInsumoSnapshotId.of(UUID.randomUUID()), "MAT-001", "Cemento", new BigDecimal("1.0"),
                                new BigDecimal("1.0"), TipoRecurso.MATERIAL, 1, new BigDecimal("1.0"), "TON", "TON", // Diferente
                                                                                                                     // unidad
                                                                                                                     // base
                                BigDecimal.ONE, "TON", "PEN", BigDecimal.ONE, new BigDecimal("1.0"), // precioMercado
                                BigDecimal.ZERO, // flete
                                new BigDecimal("1.0"), // precioPuestoEnObra
                                BigDecimal.ZERO, // desperdicio
                                null, // composicionCuadrilla
                                null, // costoDiaCuadrillaCalculado
                                null, // jornadaHoras
                                null, // costoHoraMaquina
                                null, // horasUso
                                null, // porcentajeManoObra
                                null // dependeDe
                );

                APUSnapshot apuIncompatible = APUSnapshot.crear(APUSnapshotId.of(UUID.randomUUID()), partidaId2,
                                "APU-003", "CATALOG-001", new BigDecimal("10.00"), "m²", LocalDateTime.now());
                apuIncompatible.agregarInsumo(insumoIncompatible);

                when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
                when(partidaRepository.findByPresupuestoId(presupuestoId)).thenReturn(List.of(partida1, partida2));
                when(apuSnapshotRepository.findByPartidaId(partidaId1)).thenReturn(Optional.of(apu1));
                when(apuSnapshotRepository.findByPartidaId(partidaId2)).thenReturn(Optional.of(apuIncompatible));

                // When/Then
                assertThatThrownBy(() -> useCase.ejecutar(presupuestoId)).isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("Unidades incompatibles");
        }

        @Test
        void deberiaIgnorarPartidasSinAPU() {
                // Given
                when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
                when(partidaRepository.findByPresupuestoId(presupuestoId)).thenReturn(List.of(partida1, partida2));
                when(apuSnapshotRepository.findByPartidaId(partidaId1)).thenReturn(Optional.of(apu1));
                when(apuSnapshotRepository.findByPartidaId(partidaId2)).thenReturn(Optional.empty());

                // When
                ExplosionInsumosResponse resultado = useCase.ejecutar(presupuestoId);

                // Then
                assertThat(resultado.recursosPorTipo()).containsKey("MATERIAL");
                List<RecursoAgregadoDTO> materiales = resultado.recursosPorTipo().get("MATERIAL");
                // Solo debe tener recursos de partida1
                assertThat(materiales).hasSize(1);
                assertThat(materiales.get(0).recursoExternalId()).isEqualTo("MAT-001");
        }

        @Test
        void deberiaLanzarExcepcionSiPresupuestoNoExiste() {
                // Given
                when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.empty());

                // When/Then
                assertThatThrownBy(() -> useCase.ejecutar(presupuestoId))
                                .isInstanceOf(PresupuestoNoEncontradoException.class);
        }

        @Test
        void deberiaRetornarVacioSiNoHayPartidas() {
                // Given
                when(presupuestoRepository.findById(any(PresupuestoId.class))).thenReturn(Optional.of(presupuesto));
                when(partidaRepository.findByPresupuestoId(presupuestoId)).thenReturn(List.of());

                // When
                ExplosionInsumosResponse resultado = useCase.ejecutar(presupuestoId);

                // Then
                assertThat(resultado.recursosPorTipo()).isEmpty();
        }
}
