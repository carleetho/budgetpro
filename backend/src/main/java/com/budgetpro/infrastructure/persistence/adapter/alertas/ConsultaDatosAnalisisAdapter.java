package com.budgetpro.infrastructure.persistence.adapter.alertas;

import com.budgetpro.application.alertas.usecase.AnalizarPresupuestoUseCaseImpl;
import com.budgetpro.domain.finanzas.alertas.service.AnalizadorParametricoService;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuInsumoJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de ConsultaDatosAnalisis que consulta datos necesarios para el
 * análisis paramétrico.
 */
@Component
public class ConsultaDatosAnalisisAdapter implements AnalizarPresupuestoUseCaseImpl.ConsultaDatosAnalisis {

    private final PartidaJpaRepository partidaJpaRepository;
    private final ApuJpaRepository apuJpaRepository;
    private final ApuInsumoJpaRepository apuInsumoJpaRepository;
    private final RecursoJpaRepository recursoJpaRepository;

    public ConsultaDatosAnalisisAdapter(PartidaJpaRepository partidaJpaRepository, ApuJpaRepository apuJpaRepository,
            ApuInsumoJpaRepository apuInsumoJpaRepository, RecursoJpaRepository recursoJpaRepository) {
        this.partidaJpaRepository = partidaJpaRepository;
        this.apuJpaRepository = apuJpaRepository;
        this.apuInsumoJpaRepository = apuInsumoJpaRepository;
        this.recursoJpaRepository = recursoJpaRepository;
    }

    @Override
    public AnalizadorParametricoService.DatosAnalisis consultarDatos(UUID presupuestoId) {
        // Buscar todas las partidas del presupuesto
        List<PartidaEntity> partidasEntity = partidaJpaRepository.findByPresupuestoId(presupuestoId);

        // Buscar todos los recursos únicos del presupuesto (a través de APUs)
        List<RecursoEntity> recursosEntity = obtenerRecursosDelPresupuesto(presupuestoId);

        // Mapear recursos
        List<AnalizadorParametricoService.DatosRecurso> recursos = recursosEntity.stream().map(this::mapearRecurso)
                .collect(Collectors.toList());

        // Mapear partidas con sus insumos
        List<AnalizadorParametricoService.DatosPartida> partidas = partidasEntity.stream().map(this::mapearPartida)
                .collect(Collectors.toList());

        return new AnalizadorParametricoService.DatosAnalisis(recursos, partidas);
    }

    private List<RecursoEntity> obtenerRecursosDelPresupuesto(UUID presupuestoId) {
        // Obtener todos los APUs del presupuesto
        List<PartidaEntity> partidas = partidaJpaRepository.findByPresupuestoId(presupuestoId);

        List<UUID> recursoIds = new ArrayList<>();

        // Para cada partida, buscar su APU y sus insumos
        for (PartidaEntity partida : partidas) {
            Optional<ApuEntity> apuOpt = apuJpaRepository.findByPartidaId(partida.getId());
            if (apuOpt.isPresent()) {
                ApuEntity apu = apuOpt.get();
                List<ApuInsumoEntity> insumos = apuInsumoJpaRepository.findByApuId(apu.getId());
                for (ApuInsumoEntity insumo : insumos) {
                    UUID recursoId = insumo.getRecurso().getId();
                    if (!recursoIds.contains(recursoId)) {
                        recursoIds.add(recursoId);
                    }
                }
            }
        }

        if (recursoIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Buscar recursos
        return recursoJpaRepository.findAllById(recursoIds);
    }

    private AnalizadorParametricoService.DatosRecurso mapearRecurso(RecursoEntity entity) {
        Map<String, Object> atributos = entity.getAtributos() != null ? new HashMap<>(entity.getAtributos())
                : new HashMap<>();

        return new AnalizadorParametricoService.DatosRecurso(entity.getId(), entity.getNombre(), entity.getTipo(),
                atributos);
    }

    private AnalizadorParametricoService.DatosPartida mapearPartida(PartidaEntity entity) {
        // Buscar APU de la partida
        Optional<ApuEntity> apuOpt = apuJpaRepository.findByPartidaId(entity.getId());

        List<AnalizadorParametricoService.DatosApuInsumo> insumos = new ArrayList<>();

        if (apuOpt.isPresent()) {
            ApuEntity apu = apuOpt.get();
            List<ApuInsumoEntity> insumosEntity = apuInsumoJpaRepository.findByApuId(apu.getId());

            insumos = insumosEntity.stream().map(insumo -> {
                RecursoEntity recurso = insumo.getRecurso();
                Map<String, Object> atributos = recurso.getAtributos() != null ? new HashMap<>(recurso.getAtributos())
                        : new HashMap<>();

                return new AnalizadorParametricoService.DatosApuInsumo(recurso.getId(), recurso.getNombre(),
                        recurso.getTipo(), insumo.getCantidad(), atributos);
            }).collect(Collectors.toList());
        }

        return new AnalizadorParametricoService.DatosPartida(entity.getId(), entity.getDescripcion(), insumos);
    }
}
