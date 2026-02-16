package com.budgetpro.infrastructure.adapter.inventario;

import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.compra.port.out.InventarioService;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Adaptador de infraestructura para InventarioService.
 * 
 * Integra con el módulo Inventario para actualizar stock cuando se recibe una orden de compra.
 * Solo actualiza inventario para partidas que tienen insumos de tipo MATERIAL.
 */
@Component
public class InventarioServiceAdapter implements InventarioService {

    private final ApuSnapshotRepository apuSnapshotRepository;
    private final InventarioRepository inventarioRepository;
    private final DefaultBodegaPort defaultBodegaPort;

    public InventarioServiceAdapter(ApuSnapshotRepository apuSnapshotRepository,
                                   InventarioRepository inventarioRepository,
                                   DefaultBodegaPort defaultBodegaPort) {
        this.apuSnapshotRepository = apuSnapshotRepository;
        this.inventarioRepository = inventarioRepository;
        this.defaultBodegaPort = defaultBodegaPort;
    }

    @Override
    public void increaseStock(UUID proyectoId, UUID partidaId, BigDecimal cantidad, String referencia) {
        // 1. Obtener APUSnapshot de la partida
        APUSnapshot apuSnapshot = apuSnapshotRepository.findByPartidaId(partidaId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("La partida %s no tiene APU asociado. No se puede actualizar inventario.", partidaId)
                ));

        // 2. Filtrar insumos de tipo MATERIAL
        List<APUInsumoSnapshot> insumosMaterial = apuSnapshot.getInsumos().stream()
                .filter(insumo -> insumo.getTipoRecurso() == TipoRecurso.MATERIAL)
                .toList();

        if (insumosMaterial.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("La partida %s no tiene insumos de tipo MATERIAL. Solo partidas con insumos MATERIAL afectan el inventario.", partidaId)
            );
        }

        // 3. Obtener bodega por defecto del proyecto
        BodegaId bodegaId = defaultBodegaPort.getDefaultForProject(proyectoId)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("No hay bodega por defecto para el proyecto %s", proyectoId)
                ));

        // 4. Para cada insumo MATERIAL, actualizar el inventario
        for (APUInsumoSnapshot insumo : insumosMaterial) {
            // Calcular cantidad a agregar: cantidad del detalle * aporte unitario del insumo
            BigDecimal cantidadInsumo = cantidad.multiply(insumo.getAporteUnitario());

            // Obtener o crear InventarioItem
            InventarioItem inventarioItem = inventarioRepository
                    .findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                            proyectoId,
                            insumo.getRecursoExternalId(),
                            insumo.getUnidadBase(),
                            bodegaId
                    )
                    .orElseGet(() -> {
                        // Crear nuevo InventarioItem si no existe
                        InventarioId nuevoId = InventarioId.generate();
                        return InventarioItem.crearConSnapshot(
                                nuevoId,
                                proyectoId,
                                insumo.getRecursoExternalId(),
                                bodegaId,
                                insumo.getRecursoNombre(),
                                insumo.getTipoRecurso().name(),
                                insumo.getUnidadBase()
                        );
                    });

            // Registrar entrada en inventario
            // Usar precio unitario del insumo como costo unitario
            BigDecimal costoUnitario = insumo.getPrecioUnitario();
            String referenciaCompleta = String.format("%s - %s", referencia, insumo.getRecursoNombre());

            var tx = inventarioItem.ingresar(
                    cantidadInsumo,
                    costoUnitario,
                    null, // compraDetalleId es null para OrdenCompra (no tenemos CompraDetalle)
                    referenciaCompleta
            );

            inventarioItem = tx.inventario();
            inventarioRepository.save(inventarioItem);
        }
    }
}
