package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.rrhh.model.Nomina;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NominaResponse {
    private final UUID id;
    private final UUID proyectoId;
    private final LocalDate periodoInicio;
    private final LocalDate periodoFin;
    private final String descripcion;
    private final String estado;
    private final BigDecimal totalBruto;
    private final BigDecimal totalNeto;
    private final Integer cantidadEmpleados;
    private final List<DetalleNominaResponse> detalles;

    private NominaResponse(UUID id, UUID proyectoId, LocalDate periodoInicio, LocalDate periodoFin, String descripcion,
            String estado, BigDecimal totalBruto, BigDecimal totalNeto, Integer cantidadEmpleados,
            List<DetalleNominaResponse> detalles) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.periodoInicio = periodoInicio;
        this.periodoFin = periodoFin;
        this.descripcion = descripcion;
        this.estado = estado;
        this.totalBruto = totalBruto;
        this.totalNeto = totalNeto;
        this.cantidadEmpleados = cantidadEmpleados;
        this.detalles = detalles;
    }

    public static NominaResponse fromDomain(Nomina nomina) {
        return new NominaResponse(nomina.getId().getValue(), nomina.getProyectoId().getValue(),
                nomina.getPeriodoInicio(), nomina.getPeriodoFin(), nomina.getDescripcion(), nomina.getEstado(),
                nomina.getTotalBruto(), nomina.getTotalNeto(), nomina.getCantidadEmpleados(),
                nomina.getDetalles().stream().map(DetalleNominaResponse::fromDomain).collect(Collectors.toList()));
    }

    public UUID getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFin() {
        return periodoFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public BigDecimal getTotalNeto() {
        return totalNeto;
    }

    public Integer getCantidadEmpleados() {
        return cantidadEmpleados;
    }

    public List<DetalleNominaResponse> getDetalles() {
        return detalles;
    }
}
