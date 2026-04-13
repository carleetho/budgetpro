package com.budgetpro.application.partida.usecase;

import com.budgetpro.application.partida.dto.WbsNodeResponse;
import com.budgetpro.application.partida.port.in.ObtenerWbsUseCase;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ObtenerWbsUseCaseImpl implements ObtenerWbsUseCase {

    private final PartidaRepository partidaRepository;

    public ObtenerWbsUseCaseImpl(PartidaRepository partidaRepository) {
        this.partidaRepository = partidaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WbsNodeResponse> obtenerWbsPorPresupuesto(UUID presupuestoId) {
        if (presupuestoId == null) {
            throw new IllegalArgumentException("presupuestoId es obligatorio");
        }

        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);
        partidas.sort(Comparator.comparing(Partida::getItem, Comparator.nullsLast(String::compareTo)));

        Map<UUID, List<Partida>> childrenByParent = new HashMap<>();
        List<Partida> roots = new ArrayList<>();

        for (Partida p : partidas) {
            if (p.getPadreId() == null) {
                roots.add(p);
            } else {
                childrenByParent.computeIfAbsent(p.getPadreId(), k -> new ArrayList<>()).add(p);
            }
        }

        return roots.stream()
                .map(root -> buildNode(root, childrenByParent))
                .toList();
    }

    private WbsNodeResponse buildNode(Partida partida, Map<UUID, List<Partida>> childrenByParent) {
        List<Partida> children = childrenByParent.getOrDefault(partida.getId().getValue(), List.of());
        children.sort(Comparator.comparing(Partida::getItem, Comparator.nullsLast(String::compareTo)));
        List<WbsNodeResponse> mapped = children.stream().map(c -> buildNode(c, childrenByParent)).toList();
        return new WbsNodeResponse(ObtenerPartidaUseCaseImpl.toResponse(partida), mapped);
    }
}

