package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.almacen.model.AlmacenId;
import com.budgetpro.domain.logistica.almacen.model.MovimientoAlmacenId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.Recepcion;
import com.budgetpro.domain.logistica.compra.model.RecepcionDetalle;
import com.budgetpro.domain.logistica.compra.model.RecepcionDetalleId;
import com.budgetpro.domain.logistica.compra.model.RecepcionId;
import com.budgetpro.domain.logistica.compra.port.out.RecepcionRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.compra.RecepcionEntity;
import com.budgetpro.infrastructure.persistence.repository.compra.RecepcionJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para RecepcionRepositoryAdapter.
 * 
 * Verifica:
 * - Persistencia correcta de Recepcion con detalles
 * - Detección de guías de remisión duplicadas
 * - Enforcement de constraint único (compra_id, guia_remision)
 * - Mapeo bidireccional dominio-entidad
 */
@Transactional
class RecepcionRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private RecepcionRepository recepcionRepository;

    @Autowired
    private RecepcionJpaRepository recepcionJpaRepository;

    @Autowired
    private EntityManager entityManager;

    private UUID compraId;
    private UUID usuarioId;
    private UUID almacenId;
    private UUID recursoId;
    private UUID detalleCompraId;
    private CompraId compraDomainId;
    private AlmacenId almacenDomainId;
    private String guiaRemision;
    private LocalDate fechaRecepcion;

    @BeforeEach
    void setUp() {
        compraId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        almacenId = UUID.randomUUID();
        recursoId = UUID.randomUUID();
        detalleCompraId = UUID.randomUUID();
        
        compraDomainId = CompraId.from(compraId);
        almacenDomainId = AlmacenId.of(almacenId);
        
        guiaRemision = "GR-2024-001";
        fechaRecepcion = LocalDate.now();
    }

    @Test
    @DisplayName("Debe guardar una recepción con detalles correctamente")
    void debeGuardarRecepcionConDetalles() {
        // Arrange
        RecepcionId recepcionId = RecepcionId.generate();
        RecepcionDetalle detalle = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("100.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion = Recepcion.crear(
            recepcionId,
            compraDomainId,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle),
            usuarioId
        );

        // Act
        recepcionRepository.save(recepcion);

        // Assert - Verificar que se persistió
        entityManager.flush();
        entityManager.clear();

        RecepcionEntity entity = recepcionJpaRepository.findById(recepcionId.getValue())
            .orElseThrow(() -> new AssertionError("La recepción debería existir"));

        assertNotNull(entity);
        assertEquals(recepcionId.getValue(), entity.getId());
        assertEquals(compraId, entity.getCompraId());
        assertEquals(fechaRecepcion, entity.getFechaRecepcion());
        assertEquals(guiaRemision, entity.getGuiaRemision());
        assertEquals(usuarioId, entity.getCreadoPorUsuarioId());
        assertNotNull(entity.getFechaCreacion());
        assertEquals(1L, entity.getVersion()); // Version inicializado por Hibernate
        assertEquals(1, entity.getDetalles().size());

        // Verificar detalle
        var detalleEntity = entity.getDetalles().get(0);
        assertEquals(detalleCompraId, detalleEntity.getCompraDetalleId());
        assertEquals(recursoId, detalleEntity.getRecursoId());
        assertEquals(almacenId, detalleEntity.getAlmacenId());
        assertEquals(new BigDecimal("100.00"), detalleEntity.getCantidadRecibida());
        assertEquals(new BigDecimal("10.50"), detalleEntity.getPrecioUnitario());
    }

    @Test
    @DisplayName("Debe detectar guía de remisión duplicada para la misma compra")
    void debeDetectarGuiaRemisionDuplicada() {
        // Arrange - Crear primera recepción
        RecepcionId recepcionId1 = RecepcionId.generate();
        RecepcionDetalle detalle1 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("50.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion1 = Recepcion.crear(
            recepcionId1,
            compraDomainId,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle1),
            usuarioId
        );

        recepcionRepository.save(recepcion1);
        entityManager.flush();

        // Act - Intentar crear segunda recepción con misma guía
        boolean existe = recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision);

        // Assert
        assertTrue(existe, "Debe detectar que la guía de remisión ya existe para esta compra");
    }

    @Test
    @DisplayName("Debe permitir misma guía de remisión para diferentes compras")
    void debePermitirMismaGuiaParaDiferentesCompras() {
        // Arrange - Crear recepción para compra 1
        UUID compraId1 = UUID.randomUUID();
        CompraId compraDomainId1 = CompraId.from(compraId1);
        
        RecepcionId recepcionId1 = RecepcionId.generate();
        RecepcionDetalle detalle1 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("50.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion1 = Recepcion.crear(
            recepcionId1,
            compraDomainId1,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle1),
            usuarioId
        );

        recepcionRepository.save(recepcion1);
        entityManager.flush();

        // Act - Verificar guía para compra diferente
        boolean existe = recepcionRepository.existsByCompraIdAndGuiaRemision(compraDomainId, guiaRemision);

        // Assert
        assertFalse(existe, "Debe permitir la misma guía para diferentes compras");
    }

    @Test
    @DisplayName("Debe lanzar DataIntegrityViolationException al violar constraint único")
    void debeLanzarExcepcionAlViolarConstraintUnico() {
        // Arrange - Crear primera recepción
        RecepcionId recepcionId1 = RecepcionId.generate();
        RecepcionDetalle detalle1 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("50.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion1 = Recepcion.crear(
            recepcionId1,
            compraDomainId,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle1),
            usuarioId
        );

        recepcionRepository.save(recepcion1);
        entityManager.flush();
        entityManager.clear();

        // Act & Assert - Intentar crear segunda recepción con misma compra y guía
        RecepcionId recepcionId2 = RecepcionId.generate();
        RecepcionDetalle detalle2 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("30.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion2 = Recepcion.crear(
            recepcionId2,
            compraDomainId,
            fechaRecepcion,
            guiaRemision, // Misma guía
            List.of(detalle2),
            usuarioId
        );

        // Debe lanzar excepción al intentar guardar
        assertThrows(DataIntegrityViolationException.class, () -> {
            recepcionRepository.save(recepcion2);
            entityManager.flush();
        }, "Debe lanzar DataIntegrityViolationException al violar constraint único");
    }

    @Test
    @DisplayName("Debe retornar false cuando la guía de remisión no existe")
    void debeRetornarFalseCuandoGuiaNoExiste() {
        // Act
        boolean existe = recepcionRepository.existsByCompraIdAndGuiaRemision(
            compraDomainId,
            "GR-2024-999"
        );

        // Assert
        assertFalse(existe, "Debe retornar false cuando la guía no existe");
    }

    @Test
    @DisplayName("Debe mapear correctamente dominio a entidad y viceversa")
    void debeMapearCorrectamenteDominioYEntidad() {
        // Arrange
        RecepcionId recepcionId = RecepcionId.generate();
        RecepcionDetalle detalle = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("75.50"),
            new BigDecimal("12.75"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcionOriginal = Recepcion.crear(
            recepcionId,
            compraDomainId,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle),
            usuarioId
        );

        // Act - Guardar y recuperar
        recepcionRepository.save(recepcionOriginal);
        entityManager.flush();
        entityManager.clear();

        // Verificar que se puede recuperar desde JPA directamente
        RecepcionEntity entity = recepcionJpaRepository.findById(recepcionId.getValue())
            .orElseThrow();

        // Assert - Verificar mapeo
        assertEquals(recepcionId.getValue(), entity.getId());
        assertEquals(compraId, entity.getCompraId());
        assertEquals(fechaRecepcion, entity.getFechaRecepcion());
        assertEquals(guiaRemision, entity.getGuiaRemision());
        assertEquals(usuarioId, entity.getCreadoPorUsuarioId());
        assertEquals(1, entity.getDetalles().size());

        var detalleEntity = entity.getDetalles().get(0);
        assertEquals(detalle.getId().getValue(), detalleEntity.getId());
        assertEquals(detalleCompraId, detalleEntity.getCompraDetalleId());
        assertEquals(recursoId, detalleEntity.getRecursoId());
        assertEquals(almacenId, detalleEntity.getAlmacenId());
        assertEquals(new BigDecimal("75.50"), detalleEntity.getCantidadRecibida());
        assertEquals(new BigDecimal("12.75"), detalleEntity.getPrecioUnitario());
    }

    @Test
    @DisplayName("Debe guardar recepción con múltiples detalles")
    void debeGuardarRecepcionConMultiplesDetalles() {
        // Arrange
        RecepcionId recepcionId = RecepcionId.generate();
        UUID detalleCompraId2 = UUID.randomUUID();
        UUID recursoId2 = UUID.randomUUID();
        UUID almacenId2 = UUID.randomUUID();

        RecepcionDetalle detalle1 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId,
            recursoId,
            almacenDomainId,
            new BigDecimal("100.00"),
            new BigDecimal("10.50"),
            MovimientoAlmacenId.generate()
        );

        RecepcionDetalle detalle2 = RecepcionDetalle.crear(
            RecepcionDetalleId.generate(),
            detalleCompraId2,
            recursoId2,
            AlmacenId.of(almacenId2),
            new BigDecimal("200.00"),
            new BigDecimal("15.75"),
            MovimientoAlmacenId.generate()
        );

        Recepcion recepcion = Recepcion.crear(
            recepcionId,
            compraDomainId,
            fechaRecepcion,
            guiaRemision,
            List.of(detalle1, detalle2),
            usuarioId
        );

        // Act
        recepcionRepository.save(recepcion);
        entityManager.flush();
        entityManager.clear();

        // Assert
        RecepcionEntity entity = recepcionJpaRepository.findById(recepcionId.getValue())
            .orElseThrow();

        assertEquals(2, entity.getDetalles().size());
        assertThat(entity.getDetalles())
            .extracting("compraDetalleId")
            .containsExactlyInAnyOrder(detalleCompraId, detalleCompraId2);
    }
}
