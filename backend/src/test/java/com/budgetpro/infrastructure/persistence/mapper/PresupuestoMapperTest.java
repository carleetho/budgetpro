package com.budgetpro.infrastructure.persistence.mapper;

import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PresupuestoMapper.
 * 
 * Verifica:
 * - Mapeo bidireccional entre dominio y entidad
 * - Mapeo de campos de integrity hash
 * - Manejo de valores null para presupuestos no aprobados
 * - Round-trip (toEntity -> toDomain -> toEntity)
 */
class PresupuestoMapperTest {

    private PresupuestoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PresupuestoMapper();
    }

    @Test
    void toEntity_conIntegrityHash_debeMappearTodosLosCampos() {
        // Given: Presupuesto con integrity hash (aprobado)
        UUID proyectoId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        PresupuestoId presupuestoId = PresupuestoId.from(UUID.randomUUID());
        String approvalHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String executionHash = "b2c3d4e5f6789012345678901234567890123456789012345678901234567890abcd";
        LocalDateTime generatedAt = LocalDateTime.now();

        Presupuesto presupuesto = Presupuesto.reconstruir(
                presupuestoId,
                proyectoId,
                "Presupuesto Test",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                approvalHash,
                executionHash,
                generatedAt,
                approvedBy,
                "SHA-256-v1"
        );

        // When
        PresupuestoEntity entity = mapper.toEntity(presupuesto);

        // Then
        assertNotNull(entity);
        assertEquals(presupuesto.getId().getValue(), entity.getId());
        assertEquals(presupuesto.getProyectoId(), entity.getProyectoId());
        assertEquals(presupuesto.getNombre(), entity.getNombre());
        assertEquals(presupuesto.getEstado(), entity.getEstado());
        assertEquals(presupuesto.getEsContractual(), entity.getEsContractual());
        
        // Verificar campos de integrity hash
        assertEquals(approvalHash, entity.getIntegrityHashApproval());
        assertEquals(executionHash, entity.getIntegrityHashExecution());
        assertEquals(generatedAt, entity.getIntegrityHashGeneratedAt());
        assertEquals(approvedBy, entity.getIntegrityHashGeneratedBy());
        assertEquals("SHA-256-v1", entity.getIntegrityHashAlgorithm());
    }

    @Test
    void toEntity_sinIntegrityHash_debeMapparConNulls() {
        // Given: Presupuesto sin integrity hash (no aprobado)
        UUID proyectoId = UUID.randomUUID();
        PresupuestoId presupuestoId = PresupuestoId.from(UUID.randomUUID());

        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Borrador");

        // When
        PresupuestoEntity entity = mapper.toEntity(presupuesto);

        // Then
        assertNotNull(entity);
        assertEquals(presupuesto.getId().getValue(), entity.getId());
        assertEquals(presupuesto.getProyectoId(), entity.getProyectoId());
        assertEquals(presupuesto.getNombre(), entity.getNombre());
        
        // Campos de integrity hash deben ser null
        assertNull(entity.getIntegrityHashApproval());
        assertNull(entity.getIntegrityHashExecution());
        assertNull(entity.getIntegrityHashGeneratedAt());
        assertNull(entity.getIntegrityHashGeneratedBy());
        assertNull(entity.getIntegrityHashAlgorithm());
    }

    @Test
    void toDomain_conIntegrityHash_debeReconstruirCorrectamente() {
        // Given: Entity con integrity hash
        UUID proyectoId = UUID.randomUUID();
        UUID presupuestoId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        String approvalHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String executionHash = "b2c3d4e5f6789012345678901234567890123456789012345678901234567890abcd";
        LocalDateTime generatedAt = LocalDateTime.now();

        PresupuestoEntity entity = new PresupuestoEntity();
        entity.setId(presupuestoId);
        entity.setProyectoId(proyectoId);
        entity.setNombre("Presupuesto Test");
        entity.setEstado(EstadoPresupuesto.CONGELADO);
        entity.setEsContractual(true);
        entity.setVersion(1);
        entity.setIntegrityHashApproval(approvalHash);
        entity.setIntegrityHashExecution(executionHash);
        entity.setIntegrityHashGeneratedAt(generatedAt);
        entity.setIntegrityHashGeneratedBy(approvedBy);
        entity.setIntegrityHashAlgorithm("SHA-256-v1");

        // When
        Presupuesto domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals(PresupuestoId.from(presupuestoId), domain.getId());
        assertEquals(proyectoId, domain.getProyectoId());
        assertEquals("Presupuesto Test", domain.getNombre());
        assertEquals(EstadoPresupuesto.CONGELADO, domain.getEstado());
        assertTrue(domain.getEsContractual());
        
        // Verificar campos de integrity hash
        assertEquals(approvalHash, domain.getIntegrityHashApproval());
        assertEquals(executionHash, domain.getIntegrityHashExecution());
        assertEquals(generatedAt, domain.getIntegrityHashGeneratedAt());
        assertEquals(approvedBy, domain.getIntegrityHashGeneratedBy());
        assertEquals("SHA-256-v1", domain.getIntegrityHashAlgorithm());
        assertTrue(domain.isAprobado());
    }

    @Test
    void toDomain_sinIntegrityHash_debeReconstruirConNulls() {
        // Given: Entity sin integrity hash (presupuesto no aprobado)
        UUID proyectoId = UUID.randomUUID();
        UUID presupuestoId = UUID.randomUUID();

        PresupuestoEntity entity = new PresupuestoEntity();
        entity.setId(presupuestoId);
        entity.setProyectoId(proyectoId);
        entity.setNombre("Presupuesto Borrador");
        entity.setEstado(EstadoPresupuesto.BORRADOR);
        entity.setEsContractual(false);
        entity.setVersion(0);
        // Campos de integrity hash son null por defecto

        // When
        Presupuesto domain = mapper.toDomain(entity);

        // Then
        assertNotNull(domain);
        assertEquals(PresupuestoId.from(presupuestoId), domain.getId());
        assertEquals(proyectoId, domain.getProyectoId());
        assertEquals("Presupuesto Borrador", domain.getNombre());
        assertEquals(EstadoPresupuesto.BORRADOR, domain.getEstado());
        assertFalse(domain.getEsContractual());
        
        // Campos de integrity hash deben ser null
        assertNull(domain.getIntegrityHashApproval());
        assertNull(domain.getIntegrityHashExecution());
        assertNull(domain.getIntegrityHashGeneratedAt());
        assertNull(domain.getIntegrityHashGeneratedBy());
        assertNull(domain.getIntegrityHashAlgorithm());
        assertFalse(domain.isAprobado());
    }

    @Test
    void updateEntity_conIntegrityHash_debeActualizarTodosLosCampos() {
        // Given: Entity existente y Presupuesto con hash actualizado
        UUID proyectoId = UUID.randomUUID();
        UUID presupuestoId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        String oldHash = "old_hash_approval_123456789012345678901234567890123456789012345678901234567890";
        String newHash = "new_hash_approval_123456789012345678901234567890123456789012345678901234567890";
        LocalDateTime generatedAt = LocalDateTime.now();

        PresupuestoEntity existingEntity = new PresupuestoEntity();
        existingEntity.setId(presupuestoId);
        existingEntity.setProyectoId(proyectoId);
        existingEntity.setNombre("Presupuesto Original");
        existingEntity.setEstado(EstadoPresupuesto.BORRADOR);
        existingEntity.setEsContractual(false);
        existingEntity.setVersion(0);
        existingEntity.setIntegrityHashApproval(oldHash);

        Presupuesto presupuesto = Presupuesto.reconstruir(
                PresupuestoId.from(presupuestoId),
                proyectoId,
                "Presupuesto Actualizado",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                newHash,
                "execution_hash",
                generatedAt,
                approvedBy,
                "SHA-256-v1"
        );

        // When
        mapper.updateEntity(existingEntity, presupuesto);

        // Then
        assertEquals("Presupuesto Actualizado", existingEntity.getNombre());
        assertEquals(EstadoPresupuesto.CONGELADO, existingEntity.getEstado());
        assertTrue(existingEntity.getEsContractual());
        
        // Verificar que los campos de integrity hash fueron actualizados
        assertEquals(newHash, existingEntity.getIntegrityHashApproval());
        assertEquals("execution_hash", existingEntity.getIntegrityHashExecution());
        assertEquals(generatedAt, existingEntity.getIntegrityHashGeneratedAt());
        assertEquals(approvedBy, existingEntity.getIntegrityHashGeneratedBy());
        assertEquals("SHA-256-v1", existingEntity.getIntegrityHashAlgorithm());
    }

    @Test
    void roundTrip_conIntegrityHash_debePreservarDatos() {
        // Given: Presupuesto con integrity hash
        UUID proyectoId = UUID.randomUUID();
        UUID approvedBy = UUID.randomUUID();
        PresupuestoId presupuestoId = PresupuestoId.from(UUID.randomUUID());
        String approvalHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String executionHash = "b2c3d4e5f6789012345678901234567890123456789012345678901234567890abcd";
        LocalDateTime generatedAt = LocalDateTime.now();

        Presupuesto original = Presupuesto.reconstruir(
                presupuestoId,
                proyectoId,
                "Presupuesto Round-Trip",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                approvalHash,
                executionHash,
                generatedAt,
                approvedBy,
                "SHA-256-v1"
        );

        // When: Round-trip (domain -> entity -> domain)
        PresupuestoEntity entity = mapper.toEntity(original);
        PresupuestoEntity entityWithVersion = new PresupuestoEntity();
        entityWithVersion.setId(entity.getId());
        entityWithVersion.setProyectoId(entity.getProyectoId());
        entityWithVersion.setNombre(entity.getNombre());
        entityWithVersion.setEstado(entity.getEstado());
        entityWithVersion.setEsContractual(entity.getEsContractual());
        entityWithVersion.setVersion(1);
        entityWithVersion.setIntegrityHashApproval(entity.getIntegrityHashApproval());
        entityWithVersion.setIntegrityHashExecution(entity.getIntegrityHashExecution());
        entityWithVersion.setIntegrityHashGeneratedAt(entity.getIntegrityHashGeneratedAt());
        entityWithVersion.setIntegrityHashGeneratedBy(entity.getIntegrityHashGeneratedBy());
        entityWithVersion.setIntegrityHashAlgorithm(entity.getIntegrityHashAlgorithm());

        Presupuesto reconstructed = mapper.toDomain(entityWithVersion);

        // Then: Todos los campos deben preservarse
        assertEquals(original.getId(), reconstructed.getId());
        assertEquals(original.getProyectoId(), reconstructed.getProyectoId());
        assertEquals(original.getNombre(), reconstructed.getNombre());
        assertEquals(original.getEstado(), reconstructed.getEstado());
        assertEquals(original.getEsContractual(), reconstructed.getEsContractual());
        assertEquals(original.getIntegrityHashApproval(), reconstructed.getIntegrityHashApproval());
        assertEquals(original.getIntegrityHashExecution(), reconstructed.getIntegrityHashExecution());
        assertEquals(original.getIntegrityHashGeneratedAt(), reconstructed.getIntegrityHashGeneratedAt());
        assertEquals(original.getIntegrityHashGeneratedBy(), reconstructed.getIntegrityHashGeneratedBy());
        assertEquals(original.getIntegrityHashAlgorithm(), reconstructed.getIntegrityHashAlgorithm());
    }

    @Test
    void toEntity_conNull_debeRetornarNull() {
        // When
        PresupuestoEntity entity = mapper.toEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    void toDomain_conNull_debeRetornarNull() {
        // When
        Presupuesto domain = mapper.toDomain(null);

        // Then
        assertNull(domain);
    }
}
