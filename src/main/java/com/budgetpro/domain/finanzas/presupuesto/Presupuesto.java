package com.budgetpro.domain.finanzas.presupuesto;

import com.budgetpro.domain.finanzas.partida.CodigoPartida;
import com.budgetpro.domain.finanzas.partida.EstadoPartida;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.recurso.model.TipoRecurso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root del agregado PRESUPUESTO.
 * 
 * Representa un presupuesto de proyecto con su colección de partidas.
 * 
 * Invariantes Clave:
 * 1. Presupuesto contractual es inmutable (no se pueden agregar/modificar partidas).
 * 2. Cada Partida tiene exactamente un APU (si aplica a MVP, validado en creación de partida).
 * 3. Las partidas son entidades internas del agregado (se acceden solo a través del agregado raíz).
 * 
 * Contexto: Presupuestos & APUs
 */
public final class Presupuesto {

    private final PresupuestoId id;
    private final UUID proyectoId;
    private final boolean esContractual;
    private Long version;
    
    // Colección de Partida como entidad interna del agregado
    private final List<Partida> partidas;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Presupuesto(PresupuestoId id, UUID proyectoId, boolean esContractual, Long version, List<Partida> partidas) {
        this.id = Objects.requireNonNull(id, "El ID del presupuesto no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.esContractual = esContractual;
        this.version = version != null ? version : 0L;
        this.partidas = partidas != null ? new ArrayList<>(partidas) : new ArrayList<>();
    }

    /**
     * Factory method para crear un nuevo Presupuesto (no contractual).
     */
    public static Presupuesto crear(PresupuestoId id, UUID proyectoId) {
        return new Presupuesto(id, proyectoId, false, 0L, new ArrayList<>());
    }

    /**
     * Factory method para crear un Presupuesto contractual (inmutable).
     */
    public static Presupuesto crearContractual(PresupuestoId id, UUID proyectoId) {
        return new Presupuesto(id, proyectoId, true, 0L, new ArrayList<>());
    }

    /**
     * Factory method para reconstruir un Presupuesto desde persistencia.
     */
    public static Presupuesto reconstruir(PresupuestoId id, UUID proyectoId, boolean esContractual, Long version, List<Partida> partidas) {
        return new Presupuesto(id, proyectoId, esContractual, version, partidas);
    }

    /**
     * Agrega una nueva partida al presupuesto.
     * 
     * INVARIANTE: Si el presupuesto es contractual, no se pueden agregar partidas (inmutable).
     * 
     * @param codigo Código de la partida
     * @param nombre Nombre de la partida
     * @param tipo Tipo de recurso
     * @param montoPresupuestado Monto presupuestado
     * @return La partida creada y agregada
     * @throws IllegalStateException si el presupuesto es contractual (inmutable)
     * @throws IllegalArgumentException si ya existe una partida con el mismo código en este presupuesto
     */
    public Partida agregarPartida(CodigoPartida codigo, String nombre, TipoRecurso tipo, Monto montoPresupuestado) {
        // INVARIANTE: Presupuesto contractual es inmutable
        if (esContractual) {
            throw new IllegalStateException("No se pueden agregar partidas a un presupuesto contractual (inmutable)");
        }

        // Validar que no exista una partida con el mismo código
        boolean existeCodigo = partidas.stream()
                .anyMatch(p -> p.getCodigo().equals(codigo));
        if (existeCodigo) {
            throw new IllegalArgumentException(
                String.format("Ya existe una partida con código %s en este presupuesto", codigo.getValue())
            );
        }

        // Crear nueva partida y agregarla a la colección
        Partida nuevaPartida = Partida.crear(proyectoId, id.getValue(), codigo, nombre, tipo, montoPresupuestado);
        partidas.add(nuevaPartida);
        
        return nuevaPartida;
    }
    
    /**
     * Agrega una partida hija a una partida padre existente (WBS jerárquico).
     * 
     * Según Directiva Maestra v2.0: Implementar lógica para crear partidas hijas.
     * 
     * @param padreId El ID de la partida padre
     * @param codigo El código de la partida hija
     * @param nombre El nombre de la partida hija
     * @param tipo El tipo de recurso
     * @param montoPresupuestado El monto presupuestado de la partida hija
     * @return La partida hija creada y agregada
     * @throws IllegalStateException si el presupuesto es contractual (inmutable)
     * @throws IllegalArgumentException si no existe la partida padre o ya existe una partida con el mismo código
     */
    public Partida agregarPartidaHija(PartidaId padreId, CodigoPartida codigo, String nombre,
                                     TipoRecurso tipo, Monto montoPresupuestado) {
        // INVARIANTE: Presupuesto contractual es inmutable
        if (esContractual) {
            throw new IllegalStateException("No se pueden agregar partidas a un presupuesto contractual (inmutable)");
        }
        
        // Buscar la partida padre
        Partida padre = buscarPartidaPorId(padreId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("No existe una partida padre con ID %s", padreId)
                ));
        
        // Validar que no exista una partida con el mismo código
        boolean existeCodigo = partidas.stream()
                .anyMatch(p -> p.getCodigo().equals(codigo));
        if (existeCodigo) {
            throw new IllegalArgumentException(
                String.format("Ya existe una partida con código %s en este presupuesto", codigo.getValue())
            );
        }
        
        // Crear partida hija usando factory method
        Partida partidaHija = Partida.crearHija(padre, codigo, nombre, tipo, montoPresupuestado);
        partidas.add(partidaHija);
        
        return partidaHija;
    }

    /**
     * Busca una partida por su ID.
     * 
     * @param partidaId El ID de la partida
     * @return Optional con la partida si existe, vacío en caso contrario
     */
    public Optional<Partida> buscarPartidaPorId(PartidaId partidaId) {
        return partidas.stream()
                .filter(p -> p.getId().equals(partidaId))
                .findFirst();
    }

    /**
     * Busca una partida por su código.
     * 
     * @param codigo El código de la partida
     * @return Optional con la partida si existe, vacío en caso contrario
     */
    public Optional<Partida> buscarPartidaPorCodigo(CodigoPartida codigo) {
        return partidas.stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst();
    }

    /**
     * Obtiene todas las partidas del presupuesto (lista inmutable).
     * 
     * @return Lista inmutable de partidas
     */
    public List<Partida> getPartidas() {
        return Collections.unmodifiableList(partidas);
    }

    /**
     * Elimina una partida del presupuesto por su ID.
     * 
     * INVARIANTE: Si el presupuesto es contractual, no se pueden eliminar partidas (inmutable).
     * 
     * @param partidaId El ID de la partida a eliminar
     * @throws IllegalStateException si el presupuesto es contractual (inmutable)
     */
    public void eliminarPartida(PartidaId partidaId) {
        // INVARIANTE: Presupuesto contractual es inmutable
        if (esContractual) {
            throw new IllegalStateException("No se pueden eliminar partidas de un presupuesto contractual (inmutable)");
        }

        boolean eliminada = partidas.removeIf(p -> p.getId().equals(partidaId));
        if (!eliminada) {
            throw new IllegalArgumentException(
                String.format("No existe una partida con ID %s en este presupuesto", partidaId)
            );
        }
    }

    /**
     * Calcula el total presupuestado (suma de todas las partidas).
     * 
     * NOTA: En un modelo WBS jerárquico, esto suma todas las partidas (raíz e hijas).
     * Si se requiere rollup (solo partidas raíz que incluyen sus hijas), usar calcularTotalPresupuestadoRollup().
     * 
     * @return Monto total presupuestado
     */
    public Monto calcularTotalPresupuestado() {
        return partidas.stream()
                .map(Partida::getMontoPresupuestado)
                .reduce(Monto.cero(), Monto::sumar);
    }
    
    /**
     * Calcula el total presupuestado con rollup (solo partidas raíz, que incluyen sus hijas).
     * 
     * Según Directiva Maestra v2.0: Calcular costos ascendentes (rollup).
     * 
     * @return Monto total presupuestado (solo partidas raíz con rollup de hijas)
     */
    public Monto calcularTotalPresupuestadoRollup() {
        // Obtener solo partidas raíz (sin padre)
        List<Partida> partidasRaiz = partidas.stream()
                .filter(Partida::esRaiz)
                .toList();
        
        // Para cada partida raíz, calcular su total rollup (incluyendo hijas)
        return partidasRaiz.stream()
                .map(partidaRaiz -> {
                    // Calcular monto total de hijas de esta partida raíz
                    Monto montoHijas = partidas.stream()
                            .filter(p -> p.esHija() && p.getParentId().equals(partidaRaiz.getId()))
                            .map(Partida::getMontoPresupuestado)
                            .reduce(Monto.cero(), Monto::sumar);
                    
                    // Retornar total rollup de esta partida raíz
                    return partidaRaiz.calcularTotalRollup(montoHijas);
                })
                .reduce(Monto.cero(), Monto::sumar);
    }

    // Getters

    public PresupuestoId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public boolean isEsContractual() {
        return esContractual;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version != null ? version : 0L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Presupuesto that = (Presupuesto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Presupuesto{id=%s, proyectoId=%s, esContractual=%s, version=%d, partidas=%d}", 
                           id, proyectoId, esContractual, version, partidas.size());
    }
}
