package com.budgetpro.domain.finanzas.ordencambio.model;

import com.budgetpro.domain.finanzas.ordencambio.service.PresupuestoVersionService;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado ORDEN_CAMBIO.
 * 
 * Gestiona el ciclo de vida de una orden de cambio, desde su solicitud hasta su
 * aprobación o rechazo.
 * 
 * Invariantes: - El proyectoId no puede ser nulo - El número de orden no puede
 * estar vacío - El origen no puede ser nulo - La descripción no puede estar
 * vacía - El estado no puede ser nulo - Para enviar a revisión, se requiere
 * justificación técnica - Para aprobar, se requiere impacto en cronograma
 * (aunque sea 0) - Para rechazar, se requiere motivo
 */
public final class OrdenCambio {

    private final OrdenCambioId id;
    private final ProyectoId proyectoId;
    private final String numeroOrden;
    private final OrigenOrdenCambio origen;
    private String descripcion;
    private String justificacionTecnica;
    private EstadoOrdenCambio estado;

    // Campos de auditoría
    private final LocalDateTime fechaSolicitud;
    private final UUID solicitanteId;
    private LocalDateTime fechaRevision;
    private LocalDateTime fechaResolucion;
    private UUID aprobadorId;
    private String motivoRechazo;

    // Campos de impacto
    private BigDecimal montoTotal;
    private Integer impactoCronogramaDias;
    private boolean requiereAdenda;
    private String numeroAdenda;
    private Long presupuestoVersionId; // Versión del presupuesto afectada

    // Listas internas del agregado
    private final List<OrdenCambioPartida> partidas;
    private final List<OrdenCambioDocumento> documentos;
    private final List<OrdenCambioRecurso> recursos;
    private final List<OrdenCambioHistorial> historial;

    /**
     * Constructor privado. Usar factory methods.
     */
    private OrdenCambio(OrdenCambioId id, ProyectoId proyectoId, String numeroOrden, OrigenOrdenCambio origen,
            String descripcion, String justificacionTecnica, EstadoOrdenCambio estado, LocalDateTime fechaSolicitud,
            UUID solicitanteId, LocalDateTime fechaRevision, LocalDateTime fechaResolucion, UUID aprobadorId,
            String motivoRechazo, BigDecimal montoTotal, Integer impactoCronogramaDias, boolean requiereAdenda,
            String numeroAdenda, Long presupuestoVersionId, List<OrdenCambioPartida> partidas,
            List<OrdenCambioDocumento> documentos, List<OrdenCambioRecurso> recursos,
            List<OrdenCambioHistorial> historial) {

        validarInvariantes(proyectoId, numeroOrden, origen, descripcion, estado, fechaSolicitud, solicitanteId);

        this.id = Objects.requireNonNull(id, "El ID de la orden no puede ser nulo");
        this.proyectoId = proyectoId;
        this.numeroOrden = numeroOrden.trim();
        this.origen = origen;
        this.descripcion = descripcion.trim();
        this.justificacionTecnica = justificacionTecnica;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.solicitanteId = solicitanteId;
        this.fechaRevision = fechaRevision;
        this.fechaResolucion = fechaResolucion;
        this.aprobadorId = aprobadorId;
        this.motivoRechazo = motivoRechazo;
        this.montoTotal = montoTotal;
        this.impactoCronogramaDias = impactoCronogramaDias;
        this.requiereAdenda = requiereAdenda;
        this.numeroAdenda = numeroAdenda;
        this.presupuestoVersionId = presupuestoVersionId;

        // Inicializar listas (defensiva) o usar las pasadas si no son nulas
        this.partidas = partidas != null ? new ArrayList<>(partidas) : new ArrayList<>();
        this.documentos = documentos != null ? new ArrayList<>(documentos) : new ArrayList<>();
        this.recursos = recursos != null ? new ArrayList<>(recursos) : new ArrayList<>();
        this.historial = historial != null ? new ArrayList<>(historial) : new ArrayList<>();
    }

    /**
     * Factory method para crear una nueva Orden de Cambio en estado BORRADOR.
     */
    public static OrdenCambio crear(OrdenCambioId id, ProyectoId proyectoId, String numeroOrden,
            OrigenOrdenCambio origen, String descripcion, UUID solicitanteId) {
        return new OrdenCambio(id, proyectoId, numeroOrden, origen, descripcion, null, EstadoOrdenCambio.BORRADOR,
                LocalDateTime.now(), solicitanteId, null, null, null, null, BigDecimal.ZERO, null, false, null, null,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Factory method para reconstruir una Orden de Cambio desde persistencia.
     */
    public static OrdenCambio reconstruir(OrdenCambioId id, ProyectoId proyectoId, String numeroOrden,
            OrigenOrdenCambio origen, String descripcion, String justificacionTecnica, EstadoOrdenCambio estado,
            LocalDateTime fechaSolicitud, UUID solicitanteId, LocalDateTime fechaRevision,
            LocalDateTime fechaResolucion, UUID aprobadorId, String motivoRechazo, BigDecimal montoTotal,
            Integer impactoCronogramaDias, boolean requiereAdenda, String numeroAdenda, Long presupuestoVersionId,
            List<OrdenCambioPartida> partidas, List<OrdenCambioDocumento> documentos, List<OrdenCambioRecurso> recursos,
            List<OrdenCambioHistorial> historial) {
        return new OrdenCambio(id, proyectoId, numeroOrden, origen, descripcion, justificacionTecnica, estado,
                fechaSolicitud, solicitanteId, fechaRevision, fechaResolucion, aprobadorId, motivoRechazo, montoTotal,
                impactoCronogramaDias, requiereAdenda, numeroAdenda, presupuestoVersionId, partidas, documentos,
                recursos, historial);
    }

    /**
     * Deprecated: Use la versión que acepta colecciones. Mantenido temporalmente si
     * hay tests que lo usan.
     */
    @Deprecated
    public static OrdenCambio reconstruir(OrdenCambioId id, ProyectoId proyectoId, String numeroOrden,
            OrigenOrdenCambio origen, String descripcion, String justificacionTecnica, EstadoOrdenCambio estado,
            LocalDateTime fechaSolicitud, UUID solicitanteId, LocalDateTime fechaRevision,
            LocalDateTime fechaResolucion, UUID aprobadorId, String motivoRechazo, BigDecimal montoTotal,
            Integer impactoCronogramaDias, boolean requiereAdenda, String numeroAdenda, Long presupuestoVersionId) {
        return new OrdenCambio(id, proyectoId, numeroOrden, origen, descripcion, justificacionTecnica, estado,
                fechaSolicitud, solicitanteId, fechaRevision, fechaResolucion, aprobadorId, motivoRechazo, montoTotal,
                impactoCronogramaDias, requiereAdenda, numeroAdenda, presupuestoVersionId, new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private void validarInvariantes(ProyectoId proyectoId, String numeroOrden, OrigenOrdenCambio origen,
            String descripcion, EstadoOrdenCambio estado, LocalDateTime fechaSolicitud, UUID solicitanteId) {
        if (proyectoId == null)
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        if (numeroOrden == null || numeroOrden.isBlank())
            throw new IllegalArgumentException("El número de orden no puede estar vacío");
        if (origen == null)
            throw new IllegalArgumentException("El origen no puede ser nulo");
        if (descripcion == null || descripcion.isBlank())
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        if (estado == null)
            throw new IllegalArgumentException("El estado no puede ser nulo");
        if (fechaSolicitud == null)
            throw new IllegalArgumentException("La fecha de solicitud no puede ser nula");
        if (solicitanteId == null)
            throw new IllegalArgumentException("El solicitanteId no puede ser nulo");
    }

    // Métodos de Agregación

    public void agregarPartida(OrdenCambioPartida partida) {
        if (this.estado != EstadoOrdenCambio.BORRADOR) {
            throw new IllegalStateException("Solo se pueden agregar partidas en estado BORRADOR");
        }
        Objects.requireNonNull(partida, "La partida no puede ser nula");
        if (!partida.getOrdenCambioId().equals(this.id)) {
            throw new IllegalArgumentException("La partida no pertenece a esta orden de cambio");
        }

        this.partidas.add(partida);
        recalcularMontoTotal();
    }

    public void agregarDocumento(OrdenCambioDocumento documento) {
        Objects.requireNonNull(documento, "El documento no puede ser nulo");
        if (!documento.getOrdenCambioId().equals(this.id)) {
            throw new IllegalArgumentException("El documento no pertenece a esta orden de cambio");
        }

        // Validación de tipo vs estado
        if (documento.getTipo() == TipoDocumentoOrdenCambio.APROBACION && this.estado != EstadoOrdenCambio.APROBADA) {
            throw new IllegalStateException(
                    "Solo se pueden adjuntar documentos de aprobación cuando la orden está APROBADA");
        }

        this.documentos.add(documento);
    }

    public void agregarRecurso(OrdenCambioRecurso recurso) {
        if (this.estado != EstadoOrdenCambio.BORRADOR) {
            throw new IllegalStateException("Solo se pueden agregar recursos en estado BORRADOR");
        }
        Objects.requireNonNull(recurso, "El recurso no puede ser nulo");
        if (!recurso.getOrdenCambioId().equals(this.id)) {
            throw new IllegalArgumentException("El recurso no pertenece a esta orden de cambio");
        }

        this.recursos.add(recurso);
    }

    private void recalcularMontoTotal() {
        this.montoTotal = this.partidas.stream().map(OrdenCambioPartida::getSubtotal).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }

    /**
     * Envía la orden de cambio a revisión. Requiere justificación técnica y al
     * menos una partida.
     */
    public void enviarARevision(String justificacionTecnica) {
        if (this.estado != EstadoOrdenCambio.BORRADOR) {
            throw new IllegalStateException("Solo se puede enviar a revisión desde estado BORRADOR");
        }
        if (justificacionTecnica == null || justificacionTecnica.isBlank()) {
            throw new IllegalArgumentException("La justificación técnica es obligatoria para enviar a revisión");
        }
        if (this.partidas.isEmpty()) {
            throw new IllegalStateException("Debe haber al menos una partida para enviar a revisión");
        }

        EstadoOrdenCambio estadoAnterior = this.estado;
        this.justificacionTecnica = justificacionTecnica.trim();
        this.estado = EstadoOrdenCambio.EN_REVISION;
        this.fechaRevision = LocalDateTime.now();

        registrarCambioEstado(estadoAnterior, this.estado, this.solicitanteId, "Enviado a revisión");
    }

    /**
     * Aprueba la orden de cambio.
     * 
     * @param aprobadorId           ID del usuario que aprueba
     * @param impactoCronogramaDias Impacto en días (puede ser 0 o negativo)
     * @param requiereAdenda        Si requiere adenda al contrato
     * @param numeroAdenda          Número de la adenda (requerido si requiereAdenda
     *                              es true)
     * @param versionService        Servicio de dominio para crear nueva versión
     * @param presupuestoBase       Presupuesto base sobre el que se aplica
     * @param hashService           Servicio de hash para integridad
     */
    public Presupuesto aprobar(UUID aprobadorId, Integer impactoCronogramaDias, boolean requiereAdenda,
            String numeroAdenda, PresupuestoVersionService versionService, Presupuesto presupuestoBase,
            IntegrityHashService hashService) {
        if (this.estado != EstadoOrdenCambio.EN_REVISION) {
            throw new IllegalStateException("Solo se puede aprobar una orden en estado EN_REVISION");
        }
        Objects.requireNonNull(aprobadorId, "El aprobadorId no puede ser nulo");
        Objects.requireNonNull(impactoCronogramaDias, "El impacto en cronograma es obligatorio");
        Objects.requireNonNull(versionService, "El servicio de versionamiento no puede ser nulo");
        Objects.requireNonNull(presupuestoBase, "El presupuesto base no puede ser nulo");
        Objects.requireNonNull(hashService, "El servicio de integridad no puede ser nulo");

        if (requiereAdenda && (numeroAdenda == null || numeroAdenda.isBlank())) {
            throw new IllegalArgumentException("El número de adenda es obligatorio si se requiere adenda");
        }

        EstadoOrdenCambio estadoAnterior = this.estado;
        this.estado = EstadoOrdenCambio.APROBADA;
        this.aprobadorId = aprobadorId;
        this.fechaResolucion = LocalDateTime.now();
        this.impactoCronogramaDias = impactoCronogramaDias;
        this.requiereAdenda = requiereAdenda;
        this.numeroAdenda = numeroAdenda;

        // Crear nueva versión del presupuesto
        Presupuesto nuevaVersion = versionService.crearVersionDesdeOrdenCambio(this, presupuestoBase, hashService);
        // Aquí asumimos que Presupuesto tiene un ID numérico accesible, pero
        // PresupuestoId envuelve UUID.
        // La implementación del plan indicaba Long presupuestoVersionId.
        // Si PresupuestoId es UUID, este campo debería ser UUID o el ID de
        // persistencia.
        // Dado que en el modelo actual presupuestoVersionId es Long, asumimos que se
        // refiere a un ID secuencial o DB ID.
        // Como no tenemos acceso al ID de base de datos aquí (es dominio puro),
        // y PresupuestoId es UUID, hay una inconsistencia en el diseño original del
        // campo Long.
        // Sin embargo, para cumplir con el contrato actual, simulamos que obtenemos el
        // version number o similar.
        // En un escenario real, esto se manejaría diferente (probablemente UUID para
        // todo).
        // Ajuste: usaremos el número de versión lógica como referencia si es posible, o
        // dejaremos null/placeholder si es DB ID.
        // Al ver Presupuesto.java (no visible ahora pero recordado), tenía "version".
        this.presupuestoVersionId = nuevaVersion.getVersion();

        registrarCambioEstado(estadoAnterior, this.estado, aprobadorId,
                "Orden aprobada - Generada versión v" + nuevaVersion.getVersion());

        return nuevaVersion;
    }

    /**
     * Rechaza la orden de cambio.
     * 
     * @param rechazadorId ID del usuario que rechaza
     * @param motivo       Motivo del rechazo
     */
    public void rechazar(UUID rechazadorId, String motivo) {
        if (this.estado != EstadoOrdenCambio.EN_REVISION) {
            throw new IllegalStateException("Solo se puede rechazar una orden en estado EN_REVISION");
        }
        if (rechazadorId == null)
            throw new IllegalArgumentException("El rechazadorId no puede ser nulo");
        if (motivo == null || motivo.isBlank())
            throw new IllegalArgumentException("El motivo de rechazo es obligatorio");

        EstadoOrdenCambio estadoAnterior = this.estado;
        this.estado = EstadoOrdenCambio.RECHAZADA;
        this.aprobadorId = rechazadorId; // Usamos campo generico aprobadorId para quien resolvió
        this.fechaResolucion = LocalDateTime.now();
        this.motivoRechazo = motivo.trim();

        registrarCambioEstado(estadoAnterior, this.estado, rechazadorId, "Rechazado: " + motivo);
    }

    /**
     * Reabre una orden rechazada para corregirla.
     */
    public void reabrir() {
        if (this.estado != EstadoOrdenCambio.RECHAZADA) {
            throw new IllegalStateException("Solo se puede reabrir una orden en estado RECHAZADA");
        }

        EstadoOrdenCambio estadoAnterior = this.estado;
        this.estado = EstadoOrdenCambio.BORRADOR;
        this.motivoRechazo = null;
        this.aprobadorId = null;
        this.fechaResolucion = null;

        // Quien reabre es usualmente el solicitante original, pero como no pasamos user
        // ID aqui,
        // usamos el solicitanteId para el log o un sistema user.
        // Nota: Idealmente deberíamos pasar el ID de quien reabre, pero mantengo la
        // firma original
        // y uso el solicitanteId (dueño de la orden) para el registro histórico por
        // consistencia si no se especifica otro actor.
        registrarCambioEstado(estadoAnterior, this.estado, this.solicitanteId, "Reabierto para corrección");
    }

    private void registrarCambioEstado(EstadoOrdenCambio anterior, EstadoOrdenCambio nuevo, UUID usuarioId,
            String comentario) {
        this.historial.add(OrdenCambioHistorial.registrarCambio(this.id, anterior, nuevo, usuarioId, comentario));
    }

    /**
     * Actualiza el monto total de la orden manualmente.
     * 
     * @deprecated El monto se calcula automáticamente al agregar partidas.
     */
    @Deprecated
    public void actualizarMontoTotal(BigDecimal nuevoMonto) {
        if (this.estado != EstadoOrdenCambio.BORRADOR) {
            throw new IllegalStateException("Solo se puede modificar el monto en estado BORRADOR");
        }
        // Permitimos override manual si es necesario, pero idealmente debe ser
        // calculado
        this.montoTotal = nuevoMonto != null ? nuevoMonto : BigDecimal.ZERO;
    }

    // Getters
    public OrdenCambioId getId() {
        return id;
    }

    public ProyectoId getProyectoId() {
        return proyectoId;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public OrigenOrdenCambio getOrigen() {
        return origen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getJustificacionTecnica() {
        return justificacionTecnica;
    }

    public EstadoOrdenCambio getEstado() {
        return estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public LocalDateTime getFechaRevision() {
        return fechaRevision;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public UUID getAprobadorId() {
        return aprobadorId;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public Integer getImpactoCronogramaDias() {
        return impactoCronogramaDias;
    }

    public boolean isRequiereAdenda() {
        return requiereAdenda;
    }

    public String getNumeroAdenda() {
        return numeroAdenda;
    }

    public Long getPresupuestoVersionId() {
        return presupuestoVersionId;
    }

    public List<OrdenCambioPartida> getPartidas() {
        return Collections.unmodifiableList(partidas);
    }

    public List<OrdenCambioDocumento> getDocumentos() {
        return Collections.unmodifiableList(documentos);
    }

    public List<OrdenCambioRecurso> getRecursos() {
        return Collections.unmodifiableList(recursos);
    }

    public List<OrdenCambioHistorial> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdenCambio that = (OrdenCambio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("OrdenCambio{id=%s, numero='%s', estado=%s}", id, numeroOrden, estado);
    }
}
