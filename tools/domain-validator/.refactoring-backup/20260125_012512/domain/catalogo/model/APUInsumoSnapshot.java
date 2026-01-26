package com.budgetpro.domain.catalogo.model;

import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad interna del agregado APUSnapshot.
 *
 * Representa un insumo snapshot con referencia externa al recurso.
 * Extendido con campos para cálculo dinámico de ingeniería civil.
 *
 * Invariantes:
 * - recursoExternalId no puede estar vacío
 * - recursoNombre no puede estar vacío
 * - cantidad no puede ser negativa
 * - precioUnitario no puede ser negativo
 * - subtotal = cantidad * precioUnitario
 * - tipoRecurso no puede ser nulo
 * - desperdicio debe estar entre 0 y 1 (si es MATERIAL)
 * - porcentajeManoObra debe estar entre 0 y 1 (si es EQUIPO_HERRAMIENTA)
 */
public final class APUInsumoSnapshot {

    private final APUInsumoSnapshotId id;
    private final String recursoExternalId;
    private final String recursoNombre;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
    
    // Campos de clasificación
    private final TipoRecurso tipoRecurso;
    private final Integer ordenCalculo;
    
    // Campos de unidades
    private final BigDecimal aporteUnitario;
    private final String unidadAporte;
    private final String unidadBase;
    private final BigDecimal factorConversionUnidadBase;
    private final String unidadCompra;
    
    // Campos de precio/moneda
    private final String moneda;
    private final BigDecimal tipoCambioSnapshot;
    private final BigDecimal precioMercado;
    private final BigDecimal flete;
    private final BigDecimal precioPuestoEnObra;
    
    // Campos específicos MATERIAL
    private final BigDecimal desperdicio;
    
    // Campos específicos MANO_OBRA
    private final List<ComposicionCuadrillaSnapshot> composicionCuadrilla;
    private final BigDecimal costoDiaCuadrillaCalculado;
    private final Integer jornadaHoras;
    
    // Campos específicos EQUIPO_MAQUINA
    private final BigDecimal costoHoraMaquina;
    private final BigDecimal horasUso;
    
    // Campos específicos EQUIPO_HERRAMIENTA
    private final BigDecimal porcentajeManoObra;
    private final String dependeDe;

    private APUInsumoSnapshot(APUInsumoSnapshotId id,
                              String recursoExternalId,
                              String recursoNombre,
                              BigDecimal cantidad,
                              BigDecimal precioUnitario,
                              BigDecimal subtotal,
                              TipoRecurso tipoRecurso,
                              Integer ordenCalculo,
                              BigDecimal aporteUnitario,
                              String unidadAporte,
                              String unidadBase,
                              BigDecimal factorConversionUnidadBase,
                              String unidadCompra,
                              String moneda,
                              BigDecimal tipoCambioSnapshot,
                              BigDecimal precioMercado,
                              BigDecimal flete,
                              BigDecimal precioPuestoEnObra,
                              BigDecimal desperdicio,
                              List<ComposicionCuadrillaSnapshot> composicionCuadrilla,
                              BigDecimal costoDiaCuadrillaCalculado,
                              Integer jornadaHoras,
                              BigDecimal costoHoraMaquina,
                              BigDecimal horasUso,
                              BigDecimal porcentajeManoObra,
                              String dependeDe) {
        validarInvariantes(recursoExternalId, recursoNombre, cantidad, precioUnitario, tipoRecurso, desperdicio, porcentajeManoObra);

        this.id = Objects.requireNonNull(id, "El ID del insumo snapshot no puede ser nulo");
        this.recursoExternalId = recursoExternalId.trim();
        this.recursoNombre = recursoNombre.trim();
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.subtotal = subtotal != null ? subtotal : calcularSubtotal(this.cantidad, this.precioUnitario);
        
        // Campos de clasificación
        this.tipoRecurso = tipoRecurso;
        this.ordenCalculo = ordenCalculo;
        
        // Campos de unidades
        this.aporteUnitario = aporteUnitario;
        this.unidadAporte = unidadAporte != null ? unidadAporte.trim() : null;
        this.unidadBase = unidadBase != null ? unidadBase.trim() : null;
        this.factorConversionUnidadBase = factorConversionUnidadBase;
        this.unidadCompra = unidadCompra != null ? unidadCompra.trim() : null;
        
        // Campos de precio/moneda
        this.moneda = moneda != null ? moneda.trim() : null;
        this.tipoCambioSnapshot = tipoCambioSnapshot;
        this.precioMercado = precioMercado;
        this.flete = flete;
        this.precioPuestoEnObra = precioPuestoEnObra;
        
        // Campos específicos MATERIAL
        this.desperdicio = desperdicio;
        
        // Campos específicos MANO_OBRA
        this.composicionCuadrilla = composicionCuadrilla != null ? new ArrayList<>(composicionCuadrilla) : new ArrayList<>();
        this.costoDiaCuadrillaCalculado = costoDiaCuadrillaCalculado;
        this.jornadaHoras = jornadaHoras;
        
        // Campos específicos EQUIPO_MAQUINA
        this.costoHoraMaquina = costoHoraMaquina;
        this.horasUso = horasUso;
        
        // Campos específicos EQUIPO_HERRAMIENTA
        this.porcentajeManoObra = porcentajeManoObra;
        this.dependeDe = dependeDe != null ? dependeDe.trim() : null;
    }

    // Factory method simplificado para backward compatibility (legacy)
    public static APUInsumoSnapshot crear(APUInsumoSnapshotId id,
                                          String recursoExternalId,
                                          String recursoNombre,
                                          BigDecimal cantidad,
                                          BigDecimal precioUnitario) {
        return crear(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    // Factory method completo para nuevos insumos con cálculo dinámico
    public static APUInsumoSnapshot crear(APUInsumoSnapshotId id,
                                          String recursoExternalId,
                                          String recursoNombre,
                                          BigDecimal cantidad,
                                          BigDecimal precioUnitario,
                                          TipoRecurso tipoRecurso,
                                          Integer ordenCalculo,
                                          BigDecimal aporteUnitario,
                                          String unidadAporte,
                                          String unidadBase,
                                          BigDecimal factorConversionUnidadBase,
                                          String unidadCompra,
                                          String moneda,
                                          BigDecimal tipoCambioSnapshot,
                                          BigDecimal precioMercado,
                                          BigDecimal flete,
                                          BigDecimal precioPuestoEnObra,
                                          BigDecimal desperdicio,
                                          List<ComposicionCuadrillaSnapshot> composicionCuadrilla,
                                          BigDecimal costoDiaCuadrillaCalculado,
                                          Integer jornadaHoras,
                                          BigDecimal costoHoraMaquina,
                                          BigDecimal horasUso,
                                          BigDecimal porcentajeManoObra,
                                          String dependeDe) {
        return new APUInsumoSnapshot(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, null,
                tipoRecurso, ordenCalculo, aporteUnitario, unidadAporte, unidadBase, factorConversionUnidadBase, unidadCompra,
                moneda, tipoCambioSnapshot, precioMercado, flete, precioPuestoEnObra,
                desperdicio, composicionCuadrilla, costoDiaCuadrillaCalculado, jornadaHoras,
                costoHoraMaquina, horasUso, porcentajeManoObra, dependeDe);
    }

    // Factory method simplificado para backward compatibility (legacy)
    public static APUInsumoSnapshot reconstruir(APUInsumoSnapshotId id,
                                               String recursoExternalId,
                                               String recursoNombre,
                                               BigDecimal cantidad,
                                               BigDecimal precioUnitario,
                                               BigDecimal subtotal) {
        return reconstruir(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, subtotal,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    // Factory method completo para reconstruir desde persistencia
    public static APUInsumoSnapshot reconstruir(APUInsumoSnapshotId id,
                                               String recursoExternalId,
                                               String recursoNombre,
                                               BigDecimal cantidad,
                                               BigDecimal precioUnitario,
                                               BigDecimal subtotal,
                                               TipoRecurso tipoRecurso,
                                               Integer ordenCalculo,
                                               BigDecimal aporteUnitario,
                                               String unidadAporte,
                                               String unidadBase,
                                               BigDecimal factorConversionUnidadBase,
                                               String unidadCompra,
                                               String moneda,
                                               BigDecimal tipoCambioSnapshot,
                                               BigDecimal precioMercado,
                                               BigDecimal flete,
                                               BigDecimal precioPuestoEnObra,
                                               BigDecimal desperdicio,
                                               List<ComposicionCuadrillaSnapshot> composicionCuadrilla,
                                               BigDecimal costoDiaCuadrillaCalculado,
                                               Integer jornadaHoras,
                                               BigDecimal costoHoraMaquina,
                                               BigDecimal horasUso,
                                               BigDecimal porcentajeManoObra,
                                               String dependeDe) {
        return new APUInsumoSnapshot(id, recursoExternalId, recursoNombre, cantidad, precioUnitario, subtotal,
                tipoRecurso, ordenCalculo, aporteUnitario, unidadAporte, unidadBase, factorConversionUnidadBase, unidadCompra,
                moneda, tipoCambioSnapshot, precioMercado, flete, precioPuestoEnObra,
                desperdicio, composicionCuadrilla, costoDiaCuadrillaCalculado, jornadaHoras,
                costoHoraMaquina, horasUso, porcentajeManoObra, dependeDe);
    }

    private void validarInvariantes(String recursoExternalId,
                                    String recursoNombre,
                                    BigDecimal cantidad,
                                    BigDecimal precioUnitario,
                                    TipoRecurso tipoRecurso,
                                    BigDecimal desperdicio,
                                    BigDecimal porcentajeManoObra) {
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede estar vacío");
        }
        if (recursoNombre == null || recursoNombre.isBlank()) {
            throw new IllegalArgumentException("El recursoNombre no puede estar vacío");
        }
        if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precioUnitario no puede ser negativo");
        }
        
        // Validaciones específicas por tipo de recurso
        if (tipoRecurso != null) {
            if (tipoRecurso == TipoRecurso.MATERIAL && desperdicio != null) {
                if (desperdicio.compareTo(BigDecimal.ZERO) < 0 || desperdicio.compareTo(BigDecimal.ONE) > 0) {
                    throw new IllegalArgumentException("El desperdicio debe estar entre 0 y 1 (0% a 100%)");
                }
            }
            if (tipoRecurso == TipoRecurso.EQUIPO_HERRAMIENTA && porcentajeManoObra != null) {
                if (porcentajeManoObra.compareTo(BigDecimal.ZERO) < 0 || porcentajeManoObra.compareTo(BigDecimal.ONE) > 0) {
                    throw new IllegalArgumentException("El porcentajeManoObra debe estar entre 0 y 1 (0% a 100%)");
                }
            }
        }
    }

    private BigDecimal calcularSubtotal(BigDecimal cantidad, BigDecimal precioUnitario) {
        return cantidad.multiply(precioUnitario);
    }

    public APUInsumoSnapshotId getId() {
        return id;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public String getRecursoNombre() {
        return recursoNombre;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    // Getters para campos de clasificación
    public TipoRecurso getTipoRecurso() {
        return tipoRecurso;
    }

    public Integer getOrdenCalculo() {
        return ordenCalculo;
    }

    // Getters para campos de unidades
    public BigDecimal getAporteUnitario() {
        return aporteUnitario;
    }

    public String getUnidadAporte() {
        return unidadAporte;
    }

    public String getUnidadBase() {
        return unidadBase;
    }

    public BigDecimal getFactorConversionUnidadBase() {
        return factorConversionUnidadBase;
    }

    public String getUnidadCompra() {
        return unidadCompra;
    }

    // Getters para campos de precio/moneda
    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getTipoCambioSnapshot() {
        return tipoCambioSnapshot;
    }

    public BigDecimal getPrecioMercado() {
        return precioMercado;
    }

    public BigDecimal getFlete() {
        return flete;
    }

    public BigDecimal getPrecioPuestoEnObra() {
        return precioPuestoEnObra;
    }

    // Getters para campos específicos MATERIAL
    public BigDecimal getDesperdicio() {
        return desperdicio;
    }

    // Getters para campos específicos MANO_OBRA
    public List<ComposicionCuadrillaSnapshot> getComposicionCuadrilla() {
        return List.copyOf(composicionCuadrilla);
    }

    public BigDecimal getCostoDiaCuadrillaCalculado() {
        return costoDiaCuadrillaCalculado;
    }

    public Integer getJornadaHoras() {
        return jornadaHoras;
    }

    // Getters para campos específicos EQUIPO_MAQUINA
    public BigDecimal getCostoHoraMaquina() {
        return costoHoraMaquina;
    }

    public BigDecimal getHorasUso() {
        return horasUso;
    }

    // Getters para campos específicos EQUIPO_HERRAMIENTA
    public BigDecimal getPorcentajeManoObra() {
        return porcentajeManoObra;
    }

    public String getDependeDe() {
        return dependeDe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        APUInsumoSnapshot that = (APUInsumoSnapshot) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("APUInsumoSnapshot{id=%s, recursoExternalId='%s', cantidad=%s, precioUnitario=%s, subtotal=%s}",
                id, recursoExternalId, cantidad, precioUnitario, subtotal);
    }
}
