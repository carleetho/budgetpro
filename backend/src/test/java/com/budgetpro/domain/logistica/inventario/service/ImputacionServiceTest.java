package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.inventario.exception.ImputacionObligatoriaException;
import com.budgetpro.domain.logistica.inventario.port.out.ConsumoPartidaRepository;
import com.budgetpro.domain.logistica.inventario.port.out.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImputacionServiceTest {

    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private ConsumoPartidaRepository consumoPartidaRepository;

    private ImputacionService service;

    @BeforeEach
    void setUp() {
        service = new ImputacionService(partidaRepository, consumoPartidaRepository);
    }

    @Test
    void validarYRegistrarAC_directoSinPartida_throwsException() {
        assertThatThrownBy(() -> service.validarYRegistrarAC(null, NaturalezaGasto.DIRECTO_PARTIDA, BigDecimal.TEN,
                BigDecimal.ONE, "Ref")).isInstanceOf(ImputacionObligatoriaException.class)
                        .hasMessageContaining("requiere una Partida Presupuestal");
    }

    @Test
    void validarYRegistrarAC_partidaNoExiste_throwsException() {
        UUID partidaId = UUID.randomUUID();
        when(partidaRepository.existsById(partidaId)).thenReturn(false);

        assertThatThrownBy(() -> service.validarYRegistrarAC(partidaId, NaturalezaGasto.DIRECTO_PARTIDA, BigDecimal.TEN,
                BigDecimal.ONE, "Ref")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no existe");
    }

    @Test
    void validarYRegistrarAC_presupuestoNoCongelado_throwsException() {
        UUID partidaId = UUID.randomUUID();
        when(partidaRepository.existsById(partidaId)).thenReturn(true);
        when(partidaRepository.isPresupuestoCongelado(partidaId)).thenReturn(false);

        assertThatThrownBy(() -> service.validarYRegistrarAC(partidaId, NaturalezaGasto.DIRECTO_PARTIDA, BigDecimal.TEN,
                BigDecimal.ONE, "Ref")).isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("NO CONGELADO");
    }

    @Test
    void validarYRegistrarAC_valido_registraConsumo() {
        UUID partidaId = UUID.randomUUID();
        when(partidaRepository.existsById(partidaId)).thenReturn(true);
        when(partidaRepository.isPresupuestoCongelado(partidaId)).thenReturn(true);

        BigDecimal cantidad = new BigDecimal("10"); // 10 units
        BigDecimal costoPromedio = new BigDecimal("5000"); // $5000 each
        BigDecimal esperadoAC = new BigDecimal("50000"); // Total $50,000

        service.validarYRegistrarAC(partidaId, NaturalezaGasto.DIRECTO_PARTIDA, cantidad, costoPromedio, "Ref");

        verify(consumoPartidaRepository).registrarConsumo(eq(partidaId), eq(esperadoAC), any(), eq("Ref"));
    }
}
