package com.budgetpro.infrastructure.rest.compra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(
        @NotBlank @Size(max = 300) String razonSocial,
        @NotBlank @Size(max = 30) String ruc,
        @Size(max = 200) String contacto,
        @Size(max = 500) String direccion,
        String estado
) {
}

