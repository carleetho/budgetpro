package com.budgetpro.application.explosion.usecase;

import com.budgetpro.application.explosion.dto.ExplosionInsumosResponse;
import com.budgetpro.application.explosion.dto.RecursoAgregadoDTO;
import com.budgetpro.application.explosion.port.in.ExplotarInsumosPresupuestoUseCase;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.port.ApuSnapshotRepository;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso para explotar insumos de un presupuesto.
 * 
 * Agrega las cantidades totales de recursos necesarios para ejecutar el presupuesto completo,
 * normalizando unidades antes de sumar para evitar el "Error Fatal de Unidades".
 */
@Service
public class ExplotarInsumosPresupuestoUseCaseImpl implements ExplotarInsumosPresupuestoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExplotarInsumosPresupuestoUseCaseImpl.class);
    private static final int PRECISION_CALCULO = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final PresupuestoRepository presupuestoRepository;
    private final PartidaRepository partidaRepository;
    private final ApuSnapshotRepository apuSnapshotRepository;

    public ExplotarInsumosPresupuestoUseCaseImpl(
            PresupuestoRepository presupuestoRepository,
            PartidaRepository partidaRepository,
            ApuSnapshotRepository apuSnapshotRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.partidaRepository = partidaRepository;
        this.apuSnapshotRepository = apuSnapshotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ExplosionInsumosResponse ejecutar(UUID presupuestoId) {
        // 1. Validar que el presupuesto existe
        presupuestoRepository.findById(PresupuestoId.from(presupuestoId))
                .orElseThrow(() -> new PresupuestoNoEncontradoException(presupuestoId));

        // 2. Obtener todas las partidas del presupuesto
        List<Partida> partidas = partidaRepository.findByPresupuestoId(presupuestoId);

        if (partidas.isEmpty()) {
            log.warn("Presupuesto {} no tiene partidas", presupuestoId);
            return new ExplosionInsumosResponse(Collections.emptyMap());
        }

        // 3. Identificar partidas hoja (sin hijos en WBS)
        Map<UUID, List<Partida>> hijosPorPadre = partidas.stream()
                .filter(p -> p.getPadreId() != null)
                .collect(Collectors.groupingBy(Partida::getPadreId));

        List<Partida> partidasHoja = partidas.stream()
                .filter(partida -> {
                    List<Partida> hijos = hijosPorPadre.get(partida.getId().getValue());
                    return hijos == null || hijos.isEmpty();
                })
                .collect(Collectors.toList());

        log.debug("Encontradas {} partidas hoja de {} partidas totales", partidasHoja.size(), partidas.size());

        // 4. Agregar cantidades de insumos normalizadas
        Map<String, RecursoAgregado> recursosAgregados = new HashMap<>();

        for (Partida partidaHoja : partidasHoja) {
            UUID partidaId = partidaHoja.getId().getValue();
            
            // Obtener APU de la partida hoja
            Optional<APUSnapshot> apuSnapshotOpt = apuSnapshotRepository.findByPartidaId(partidaId);
            
            if (apuSnapshotOpt.isEmpty()) {
                log.warn("Partida hoja {} no tiene APU asociado, se omite de la explosión", partidaId);
                continue;
            }

            APUSnapshot apuSnapshot = apuSnapshotOpt.get();
            BigDecimal metrado = partidaHoja.getMetrado();

            // Procesar cada insumo del APU
            for (APUInsumoSnapshot insumo : apuSnapshot.getInsumos()) {
                procesarInsumo(insumo, metrado, recursosAgregados);
            }
        }

        // 5. Agrupar por tipo de recurso y construir respuesta
        Map<String, List<RecursoAgregadoDTO>> recursosPorTipo = agruparPorTipo(recursosAgregados);

        return new ExplosionInsumosResponse(recursosPorTipo);
    }

    /**
     * Procesa un insumo, normalizando su cantidad a unidad base y agregándola al mapa de recursos.
     */
    private void procesarInsumo(APUInsumoSnapshot insumo, BigDecimal metrado, Map<String, RecursoAgregado> recursosAgregados) {
        String recursoExternalId = insumo.getRecursoExternalId();
        BigDecimal aporteUnitario = insumo.getAporteUnitario() != null 
                ? insumo.getAporteUnitario() 
                : insumo.getCantidad();
        
        if (aporteUnitario == null || aporteUnitario.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("Insumo {} tiene aporte unitario cero o nulo, se omite", recursoExternalId);
            return;
        }

        // Calcular cantidad en unidad de aporte: metrado × aporteUnitario
        BigDecimal cantidadEnUnidadAporte = metrado.multiply(aporteUnitario)
                .setScale(PRECISION_CALCULO, ROUNDING_MODE);

        // Normalizar a unidad base: cantidad × factorConversionUnidadBase
        BigDecimal factorConversion = insumo.getFactorConversionUnidadBase() != null
                ? insumo.getFactorConversionUnidadBase()
                : BigDecimal.ONE;

        BigDecimal cantidadEnUnidadBase = cantidadEnUnidadAporte.multiply(factorConversion)
                .setScale(PRECISION_CALCULO, ROUNDING_MODE);

        // Obtener o crear recurso agregado
        RecursoAgregado recursoAgregado = recursosAgregados.computeIfAbsent(
                recursoExternalId,
                k -> new RecursoAgregado(
                        insumo.getRecursoExternalId(),
                        insumo.getRecursoNombre(),
                        insumo.getUnidadBase(),
                        insumo.getUnidadCompra(),
                        insumo.getFactorConversionUnidadBase(),
                        insumo.getTipoRecurso()
                )
        );

        // Validar que las unidades base sean compatibles
        if (!recursoAgregado.unidadBase.equals(insumo.getUnidadBase())) {
            throw new IllegalArgumentException(
                    String.format("Unidades incompatibles para recurso %s: %s vs %s",
                            recursoExternalId, recursoAgregado.unidadBase, insumo.getUnidadBase()));
        }

        // Agregar cantidad normalizada
        recursoAgregado.cantidadTotalBase = recursoAgregado.cantidadTotalBase.add(cantidadEnUnidadBase);
    }

    /**
     * Agrupa los recursos agregados por tipo y convierte a DTOs.
     */
    private Map<String, List<RecursoAgregadoDTO>> agruparPorTipo(Map<String, RecursoAgregado> recursosAgregados) {
        Map<String, List<RecursoAgregadoDTO>> recursosPorTipo = new TreeMap<>();

        for (RecursoAgregado recurso : recursosAgregados.values()) {
            String tipoRecurso = recurso.tipoRecurso != null 
                    ? recurso.tipoRecurso.name() 
                    : "OTROS";

            // Convertir de unidad base a unidad de compra
            BigDecimal factorConversion = recurso.factorConversion != null 
                    ? recurso.factorConversion 
                    : BigDecimal.ONE;

            BigDecimal cantidadEnUnidadCompra = recurso.cantidadTotalBase.divide(
                    factorConversion, PRECISION_CALCULO, ROUNDING_MODE);

            // Redondear hacia arriba (no puedes comprar 0.3 bolsas)
            BigDecimal cantidadCompraRedondeada = cantidadEnUnidadCompra.setScale(0, RoundingMode.UP)
                    .max(BigDecimal.ONE); // Mínimo 1 unidad

            String unidadCompra = recurso.unidadCompra != null && !recurso.unidadCompra.isBlank()
                    ? recurso.unidadCompra
                    : recurso.unidadBase;

            RecursoAgregadoDTO dto = new RecursoAgregadoDTO(
                    recurso.recursoExternalId,
                    recurso.recursoNombre,
                    cantidadCompraRedondeada,
                    unidadCompra,
                    recurso.cantidadTotalBase,
                    factorConversion
            );

            recursosPorTipo.computeIfAbsent(tipoRecurso, k -> new ArrayList<>()).add(dto);
        }

        // Ordenar recursos dentro de cada tipo alfabéticamente
        recursosPorTipo.values().forEach(lista -> 
                lista.sort(Comparator.comparing(RecursoAgregadoDTO::recursoNombre)));

        return recursosPorTipo;
    }

    /**
     * Clase interna para acumular cantidades de un recurso durante la explosión.
     */
    private static class RecursoAgregado {
        final String recursoExternalId;
        final String recursoNombre;
        final String unidadBase;
        final String unidadCompra;
        final BigDecimal factorConversion;
        final TipoRecurso tipoRecurso;
        BigDecimal cantidadTotalBase;

        RecursoAgregado(String recursoExternalId, String recursoNombre, String unidadBase,
                       String unidadCompra, BigDecimal factorConversion, TipoRecurso tipoRecurso) {
            this.recursoExternalId = recursoExternalId;
            this.recursoNombre = recursoNombre;
            this.unidadBase = unidadBase != null ? unidadBase : "UN";
            this.unidadCompra = unidadCompra;
            this.factorConversion = factorConversion != null ? factorConversion : BigDecimal.ONE;
            this.tipoRecurso = tipoRecurso;
            this.cantidadTotalBase = BigDecimal.ZERO;
        }
    }
}
