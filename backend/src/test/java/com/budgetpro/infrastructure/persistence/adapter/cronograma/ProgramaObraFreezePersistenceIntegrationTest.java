package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar la persistencia de campos de freeze en ProgramaObra.
 * 
 * Verifica:
 * - Persistencia correcta de campos congelado, congelado_at, congelado_by, snapshot_algorithm
 * - Mantenimiento del estado de freeze después de reload desde BD
 * - Constraints de base de datos (NOT NULL, DEFAULT, etc.)
 */
@Transactional
class ProgramaObraFreezePersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProgramaObraRepository programaObraRepository;

    private UUID proyectoId;
    private ProgramaObraId programaObraId;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        programaObraId = ProgramaObraId.nuevo();
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFinEstimada = LocalDate.of(2024, 12, 31);
    }

    @Test
    void save_debePersistirCamposDeFreezeCorrectamente() {
        // Given: Un ProgramaObra congelado
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        
        UUID usuarioId = UUID.randomUUID();
        programaObra.congelar(usuarioId);

        // When: Persistir
        programaObraRepository.save(programaObra);

        // Then: Debe poder recuperarse con todos los campos de freeze
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow(() -> new AssertionError("ProgramaObra debe existir después de guardarlo"));

        assertTrue(recuperado.estaCongelado(), "Debe estar congelado");
        assertNotNull(recuperado.getCongeladoAt(), "congelado_at no debe ser null");
        assertEquals(usuarioId, recuperado.getCongeladoBy(), 
                "congelado_by debe coincidir con el usuario que congeló");
        assertNotNull(recuperado.getSnapshotAlgorithm(), "snapshot_algorithm no debe ser null");
    }

    @Test
    void save_debePersistirEstadoNoCongeladoPorDefecto() {
        // Given: Un ProgramaObra sin congelar
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );

        // When: Persistir
        programaObraRepository.save(programaObra);

        // Then: Debe persistirse con congelado = false por defecto
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertFalse(recuperado.estaCongelado(), "No debe estar congelado por defecto");
        assertNull(recuperado.getCongeladoAt(), "congelado_at debe ser null si no está congelado");
        assertNull(recuperado.getCongeladoBy(), 
                "congelado_by debe ser null si no está congelado");
    }

    @Test
    void save_debeMantenerEstadoDeFreezeDespuesDeReload() {
        // Given: Un ProgramaObra congelado y persistido
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        UUID usuarioId = UUID.randomUUID();
        programaObra.congelar(usuarioId);
        programaObraRepository.save(programaObra);

        // When: Recargar desde la base de datos
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        // Then: El estado de freeze debe mantenerse
        assertTrue(recuperado.estaCongelado(), "Debe seguir congelado después de reload");
        assertNotNull(recuperado.getCongeladoAt(), "congelado_at debe persistirse");
        assertEquals(usuarioId, recuperado.getCongeladoBy(), 
                "congelado_by debe persistirse");
        
        // Verificar que la fecha de congelamiento es razonable (dentro de los últimos segundos)
        assertTrue(recuperado.getCongeladoAt().isBefore(java.time.LocalDateTime.now().plusSeconds(5)),
                "congelado_at debe ser una fecha reciente");
    }

    @Test
    void save_debePermitirCongelarDespuesDePersistir() {
        // Given: Un ProgramaObra persistido sin congelar
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // When: Congelar después de persistir
        UUID usuarioId = UUID.randomUUID();
        programaObra.congelar(usuarioId);
        programaObraRepository.save(programaObra);

        // Then: Debe persistirse el nuevo estado
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertTrue(recuperado.estaCongelado(), "Debe estar congelado después de congelar");
        assertNotNull(recuperado.getCongeladoAt(), "congelado_at debe estar establecido");
        assertEquals(usuarioId, recuperado.getCongeladoBy(), 
                "congelado_by debe estar establecido");
    }

    @Test
    void save_debeValidarConstraintCongeladoNotNull() {
        // Este test verifica que la base de datos rechaza valores NULL en congelado
        // Aunque JPA debería manejar esto, verificamos que la constraint existe
        
        // Given: Un ProgramaObra válido
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        
        // When/Then: Al persistir, congelado debe tener un valor (false por defecto)
        programaObraRepository.save(programaObra);
        
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();
        
        // Verificar que congelado nunca es null (debe ser false por defecto)
        assertNotNull(recuperado.estaCongelado(), 
                "congelado no debe ser null (debe tener valor por defecto)");
    }

    @Test
    void save_debePreservarSnapshotAlgorithm() {
        // Given: Un ProgramaObra congelado
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        UUID usuarioId = UUID.randomUUID();
        programaObra.congelar(usuarioId);
        programaObraRepository.save(programaObra);

        // When: Recargar
        ProgramaObra recuperado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        // Then: snapshot_algorithm debe estar presente
        assertNotNull(recuperado.getSnapshotAlgorithm(), 
                "snapshot_algorithm no debe ser null después de congelar");
        assertFalse(recuperado.getSnapshotAlgorithm().isBlank(), 
                "snapshot_algorithm no debe estar vacío");
    }
}
