package com.budgetpro.infrastructure.persistence.adapter;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.CodigoPartida;
import com.budgetpro.domain.finanzas.partida.EstadoPartida;
import com.budgetpro.domain.finanzas.partida.Partida;
import com.budgetpro.domain.finanzas.partida.PartidaId;
import com.budgetpro.domain.finanzas.port.out.PartidaRepository;
import com.budgetpro.infrastructure.persistence.exception.PartidaDuplicadaException;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integración para PartidaRepositoryAdapter.
 * 
 * Prueba el flujo completo de persistencia del agregado Partida usando una base de datos PostgreSQL real
 * proporcionada por Testcontainers.
 * 
 * Escenarios cubiertos:
 * 1. Crear y guardar una nueva Partida
 * 2. Recuperar una Partida por ID
 * 3. Verificar que los montos persisten con escala correcta (4 decimales, NUMERIC(19,4))
 * 4. Verificar constraint UNIQUE (presupuesto_id, codigo) - duplicados fallan
 */
class PartidaRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PartidaJpaRepository partidaJpaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID proyectoId;
    private UUID presupuestoId;
    private PresupuestoEntity presupuestoEntity;

    @BeforeEach
    void setUp() {
        // Limpiar tablas antes de cada test
        // NOTA: Usamos jpaRepository directamente porque PartidaRepository (dominio) no tiene deleteAll()
        // JpaRepository extiende CrudRepository que proporciona deleteAll()
        partidaJpaRepository.deleteAll();
        presupuestoJpaRepository.deleteAll();
        
        // Limpiar tabla proyecto usando SQL directo (no hay entidad JPA para proyecto aún)
        jdbcTemplate.update("DELETE FROM proyecto WHERE id IS NOT NULL");

        // Crear proyecto y presupuesto dummy para los tests
        proyectoId = UUID.randomUUID();
        presupuestoId = UUID.randomUUID();

        // Insertar proyecto dummy usando SQL directo (no hay entidad JPA para proyecto aún)
        jdbcTemplate.update(
            "INSERT INTO proyecto (id, nombre, estado, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
            proyectoId, "Proyecto Test", "ACTIVO"
        );

        // Crear y guardar presupuesto dummy
        presupuestoEntity = new PresupuestoEntity();
        presupuestoEntity.setId(presupuestoId);
        presupuestoEntity.setProyectoId(proyectoId);
        presupuestoEntity.setNombre("Presupuesto Test");
        presupuestoEntity.setTotalAsignado(new BigDecimal("100000.0000"));
        presupuestoJpaRepository.save(presupuestoEntity);
    }

    @Test
    void testSave_DeberiaCrearYGuardarNuevaPartida() {
        // Given
        CodigoPartida codigo = CodigoPartida.of("MAT-01");
        Monto montoPresupuestado = Monto.of("5000.1234");

        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Materiales - Cemento",
            TipoRecurso.MATERIAL,
            montoPresupuestado
        );

        // When
        Partida saved = partidaRepository.save(partida);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCodigo().getValue()).isEqualTo("MAT-01");
        assertThat(saved.getNombre()).isEqualTo("Materiales - Cemento");
        assertThat(saved.getTipo()).isEqualTo(TipoRecurso.MATERIAL);
        assertThat(saved.getEstado()).isEqualTo(EstadoPartida.BORRADOR);
        assertThat(saved.getVersion()).isNotNull(); // Version se establece tras persistir
        assertThat(saved.getMontoPresupuestado().toBigDecimal()).isEqualByComparingTo(new BigDecimal("5000.1234"));
        assertThat(saved.getMontoReservado().toBigDecimal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getMontoEjecutado().toBigDecimal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testFindById_DeberiaRecuperarPartidaExistente() {
        // Given
        CodigoPartida codigo = CodigoPartida.of("MO-02");
        Monto montoPresupuestado = Monto.of("10000.5678");

        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Mano de Obra - Albañiles",
            TipoRecurso.MANO_OBRA,
            montoPresupuestado
        );

        Partida saved = partidaRepository.save(partida);
        PartidaId savedId = saved.getId();

        // When
        Optional<Partida> foundOpt = partidaRepository.findById(savedId);

        // Then
        assertThat(foundOpt).isPresent();
        Partida found = foundOpt.get();
        assertThat(found.getId()).isEqualTo(savedId);
        assertThat(found.getCodigo().getValue()).isEqualTo("MO-02");
        assertThat(found.getNombre()).isEqualTo("Mano de Obra - Albañiles");
        assertThat(found.getTipo()).isEqualTo(TipoRecurso.MANO_OBRA);
        assertThat(found.getMontoPresupuestado().toBigDecimal()).isEqualByComparingTo(new BigDecimal("10000.5678"));
    }

    @Test
    void testSave_DeberiaPersistirMontosConEscala4Decimales() {
        // Given: Monto con 6 decimales para probar redondeo a 4
        CodigoPartida codigo = CodigoPartida.of("TEST-ESCALA");
        Monto montoPresupuestado = Monto.of("1234.123456"); // Debe redondearse a 1234.1235 (HALF_EVEN)

        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Test Escala",
            TipoRecurso.MATERIAL,
            montoPresupuestado
        );

        // When
        Partida saved = partidaRepository.save(partida);

        // Then: Verificar que se guardó con escala 4 decimales
        Optional<PartidaEntity> entityOpt = partidaJpaRepository.findById(saved.getId().getValue());
        assertThat(entityOpt).isPresent();

        PartidaEntity entity = entityOpt.get();
        
        // Verificar escala: debe tener exactamente 4 decimales
        assertThat(entity.getMontoPresupuestado().scale()).isEqualTo(4);
        assertThat(entity.getMontoPresupuestado()).isEqualByComparingTo(new BigDecimal("1234.1235"));
        
        // Verificar que el dominio también tiene la escala correcta
        assertThat(saved.getMontoPresupuestado().toBigDecimal().scale()).isEqualTo(4);
    }

    @Test
    void testReservarYVerificarPersistencia() {
        // Given
        CodigoPartida codigo = CodigoPartida.of("RES-01");
        Monto montoPresupuestado = Monto.of("10000.0000");

        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Reserva Test",
            TipoRecurso.MATERIAL,
            montoPresupuestado
        );

        Partida saved = partidaRepository.save(partida);

        // When: Reservar un monto
        Monto montoAReservar = Monto.of("3000.5678");
        saved.reservar(montoAReservar);

        // Guardar después de reservar
        Partida updated = partidaRepository.save(saved);

        // Then: Verificar que el monto reservado se persistió correctamente
        assertThat(updated.getMontoReservado().toBigDecimal()).isEqualByComparingTo(new BigDecimal("3000.5678"));
        assertThat(updated.getSaldoDisponible().toBigDecimal()).isEqualByComparingTo(new BigDecimal("6999.4322"));

        // Verificar en BD directamente
        Optional<PartidaEntity> entityOpt = partidaJpaRepository.findById(updated.getId().getValue());
        assertThat(entityOpt).isPresent();
        PartidaEntity entity = entityOpt.get();
        assertThat(entity.getMontoReservado().scale()).isEqualTo(4);
        assertThat(entity.getMontoReservado()).isEqualByComparingTo(new BigDecimal("3000.5678"));
        assertThat(entity.getMontoPresupuestado()).isEqualByComparingTo(new BigDecimal("10000.0000"));
    }

    @Test
    void testSave_DuplicadoDeberiaLanzarPartidaDuplicadaException() {
        // Given: Crear primera partida
        CodigoPartida codigo = CodigoPartida.of("DUP-01");
        Monto montoPresupuestado = Monto.of("5000.0000");

        Partida partida1 = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Primera Partida",
            TipoRecurso.MATERIAL,
            montoPresupuestado
        );

        partidaRepository.save(partida1);

        // When: Intentar crear otra partida con el mismo código en el mismo presupuesto
        Partida partida2 = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo, // Mismo código
            "Segunda Partida",
            TipoRecurso.MATERIAL,
            Monto.of("3000.0000")
        );

        // Then: Debe lanzar PartidaDuplicadaException
        assertThatThrownBy(() -> partidaRepository.save(partida2))
            .isInstanceOf(PartidaDuplicadaException.class)
            .hasMessageContaining("Ya existe una partida con código 'DUP-01' en el presupuesto");
    }

    @Test
    void testSave_CodigoDuplicadoConVariacionMayusculasDeberiaFallar() {
        // Given: Crear primera partida con código "TEST-01"
        CodigoPartida codigo1 = CodigoPartida.of("TEST-01");
        Partida partida1 = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo1,
            "Primera",
            TipoRecurso.MATERIAL,
            Monto.of("1000.0000")
        );
        partidaRepository.save(partida1);

        // When: Intentar crear otra con código normalizado igual (CodigoPartida normaliza a mayúsculas)
        CodigoPartida codigo2 = CodigoPartida.of("test-01"); // Será normalizado a "TEST-01"
        Partida partida2 = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo2,
            "Segunda",
            TipoRecurso.MATERIAL,
            Monto.of("2000.0000")
        );

        // Then: Debe fallar porque el código normalizado ya existe
        assertThatThrownBy(() -> partidaRepository.save(partida2))
            .isInstanceOf(PartidaDuplicadaException.class);
    }

    @Test
    void testFindByProyectoId_DeberiaRetornarTodasLasPartidasDelProyecto() {
        // Given: Crear múltiples partidas para el mismo proyecto
        Partida partida1 = Partida.crear(
            proyectoId,
            presupuestoId,
            CodigoPartida.of("MAT-01"),
            "Material 1",
            TipoRecurso.MATERIAL,
            Monto.of("1000.0000")
        );
        partidaRepository.save(partida1);

        Partida partida2 = Partida.crear(
            proyectoId,
            presupuestoId,
            CodigoPartida.of("MAT-02"),
            "Material 2",
            TipoRecurso.MATERIAL,
            Monto.of("2000.0000")
        );
        partidaRepository.save(partida2);

        // Crear partida para otro proyecto (no debería aparecer)
        UUID otroProyectoId = UUID.randomUUID();
        UUID otroPresupuestoId = UUID.randomUUID();
        
        jdbcTemplate.update(
            "INSERT INTO proyecto (id, nombre, estado, created_at, updated_at) VALUES (?, ?, ?, now(), now())",
            otroProyectoId, "Otro Proyecto", "ACTIVO"
        );

        PresupuestoEntity otroPresupuesto = new PresupuestoEntity();
        otroPresupuesto.setId(otroPresupuestoId);
        otroPresupuesto.setProyectoId(otroProyectoId);
        otroPresupuesto.setNombre("Otro Presupuesto");
        otroPresupuesto.setTotalAsignado(BigDecimal.ZERO);
        presupuestoJpaRepository.save(otroPresupuesto);

        Partida partida3 = Partida.crear(
            otroProyectoId,
            otroPresupuestoId,
            CodigoPartida.of("MAT-03"),
            "Material 3",
            TipoRecurso.MATERIAL,
            Monto.of("3000.0000")
        );
        partidaRepository.save(partida3);

        // When
        var partidas = partidaRepository.findByProyectoId(proyectoId);

        // Then: Solo debe retornar las partidas del proyecto original
        assertThat(partidas).hasSize(2);
        assertThat(partidas).extracting(p -> p.getCodigo().getValue())
            .containsExactlyInAnyOrder("MAT-01", "MAT-02");
    }

    @Test
    void testFindByPresupuestoId_DeberiaRetornarTodasLasPartidasDelPresupuesto() {
        // Given: Crear múltiples partidas para el mismo presupuesto
        Partida partida1 = Partida.crear(
            proyectoId,
            presupuestoId,
            CodigoPartida.of("A-01"),
            "Partida A",
            TipoRecurso.MATERIAL,
            Monto.of("1000.0000")
        );
        partidaRepository.save(partida1);

        Partida partida2 = Partida.crear(
            proyectoId,
            presupuestoId,
            CodigoPartida.of("B-01"),
            "Partida B",
            TipoRecurso.MANO_OBRA,
            Monto.of("2000.0000")
        );
        partidaRepository.save(partida2);

        // When
        var partidas = partidaRepository.findByPresupuestoId(presupuestoId);

        // Then
        assertThat(partidas).hasSize(2);
        assertThat(partidas).extracting(p -> p.getCodigo().getValue())
            .containsExactlyInAnyOrder("A-01", "B-01");
    }

    @Test
    void testExistsByPresupuestoIdAndCodigo_DeberiaRetornarTrueSiExiste() {
        // Given: Crear una partida
        CodigoPartida codigo = CodigoPartida.of("EXISTS-01");
        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            codigo,
            "Test Exists",
            TipoRecurso.MATERIAL,
            Monto.of("1000.0000")
        );
        partidaRepository.save(partida);

        // When
        boolean exists = partidaRepository.existsByPresupuestoIdAndCodigo(presupuestoId, "EXISTS-01");
        boolean existsNormalized = partidaRepository.existsByPresupuestoIdAndCodigo(presupuestoId, "exists-01"); // Debe normalizar
        boolean notExists = partidaRepository.existsByPresupuestoIdAndCodigo(presupuestoId, "NO-EXISTS");

        // Then
        assertThat(exists).isTrue();
        assertThat(existsNormalized).isTrue(); // Debe normalizar a mayúsculas
        assertThat(notExists).isFalse();
    }

    @Test
    void testUpdate_DeberiaActualizarPartidaExistente() {
        // Given: Crear y guardar una partida
        Partida partida = Partida.crear(
            proyectoId,
            presupuestoId,
            CodigoPartida.of("UPDATE-01"),
            "Nombre Original",
            TipoRecurso.MATERIAL,
            Monto.of("5000.0000")
        );
        Partida saved = partidaRepository.save(partida);
        PartidaId savedId = saved.getId();
        Long versionOriginal = saved.getVersion();

        // When: Modificar la partida (reservar y actualizar nombre)
        saved.reservar(Monto.of("1000.0000"));
        saved.setNombre("Nombre Actualizado");
        
        Partida updated = partidaRepository.save(saved);

        // Then: Verificar que se actualizó correctamente
        assertThat(updated.getId()).isEqualTo(savedId);
        assertThat(updated.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(updated.getMontoReservado().toBigDecimal()).isEqualByComparingTo(new BigDecimal("1000.0000"));
        assertThat(updated.getVersion()).isGreaterThan(versionOriginal); // Version debe incrementarse
    }
}
