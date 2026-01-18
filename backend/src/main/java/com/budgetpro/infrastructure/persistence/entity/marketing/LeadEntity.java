package com.budgetpro.infrastructure.persistence.entity.marketing;

import com.budgetpro.infrastructure.persistence.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para registrar solicitudes de demo (leads).
 */
@Entity
@Table(name = "marketing_lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeadEntity extends AuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "nombre_contacto", nullable = false, length = 150)
    private String nombreContacto;

    @Email
    @Size(max = 200)
    @Column(name = "email", length = 200)
    private String email;

    @Size(max = 40)
    @Column(name = "telefono", length = 40)
    private String telefono;

    @Size(max = 200)
    @Column(name = "nombre_empresa", length = 200)
    private String nombreEmpresa;

    @Size(max = 120)
    @Column(name = "rol", length = 120)
    private String rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private LeadEstado estado;

    @CreationTimestamp
    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;
}
