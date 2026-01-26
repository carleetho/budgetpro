package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.compra.model.RelacionContractual;
import com.budgetpro.domain.logistica.compra.model.RubroInsumo;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de InventarioSnapshotService, incluyendo auto-creación por cambio de unidad (Authority by PO).
 */
@ExtendWith(MockitoExtension.class)
class InventarioSnapshotServiceTest {

    private static final UUID PROYECTO_ID = UUID.randomUUID();
    private static final BodegaId BODEGA_ID = BodegaId.generate();
    private static final String CATALOG_SOURCE = "MOCK";

    @Mock
    private CatalogPort catalogPort;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private DefaultBodegaPort defaultBodegaPort;

    private InventarioSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new InventarioSnapshotService(catalogPort, inventarioRepository, defaultBodegaPort, CATALOG_SOURCE);
    }

    /**
     * Unit change auto-creation.
     * Setup: Existing inventory "Cemento" in KG, catalog changed to LIBRAS, purchase arrives in LIBRAS.
     * Action: Process purchase via crearDesdeCompra.
     * Expect: New InventarioItem created for LIBRAS; old KG item unchanged (not looked up).
     */
    @Test
    void crearDesdeCompra_cambioUnidadKgALibras_autoCreaNuevoItemLibras() {
        // Catalog now returns LIBRAS for MAT-001 (Cemento)
        RecursoSnapshot snapshotLibras = new RecursoSnapshot(
                "MAT-001",
                CATALOG_SOURCE,
                "Cemento",
                TipoRecurso.MATERIAL,
                "LIBRAS",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        );
        when(catalogPort.fetchRecurso(eq("MAT-001"), eq(CATALOG_SOURCE))).thenReturn(snapshotLibras);

        when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));

        // No LIBRAS item exists yet (existing KG item is separate – we search by LIBRAS)
        when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                eq(PROYECTO_ID), eq("MAT-001"), eq("LIBRAS"), eq(BODEGA_ID)))
                .thenReturn(Optional.empty());

        CompraDetalle detalle = CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                "MAT-001",
                "Cemento",
                "LIBRAS", // Purchase arrives in LIBRAS (Authority by PO)
                null,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("100"),
                new BigDecimal("25.50")
        );
        Compra compra = Compra.crear(CompraId.nuevo(), PROYECTO_ID, LocalDate.now(), "Proveedor", List.of(detalle));

        InventarioItem item = snapshotService.crearDesdeCompra(compra, detalle);

        assertThat(item).isNotNull();
        assertThat(item.getRecursoExternalId()).isEqualTo("MAT-001");
        assertThat(item.getUnidadBase()).isEqualTo("LIBRAS");
        assertThat(item.getNombre()).isEqualTo("Cemento");
        assertThat(item.getClasificacion()).isEqualTo(TipoRecurso.MATERIAL.name());
        assertThat(item.getProyectoId()).isEqualTo(PROYECTO_ID);
        assertThat(item.getBodegaId()).isEqualTo(BODEGA_ID);
        assertThat(item.getCantidadFisica()).isEqualByComparingTo(BigDecimal.ZERO);
        // Service does not persist; KG item (if any) is never touched
        verify(catalogPort).fetchRecurso("MAT-001", CATALOG_SOURCE);
        verify(defaultBodegaPort).getDefaultForProject(PROYECTO_ID);
        verify(inventarioRepository).findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                PROYECTO_ID, "MAT-001", "LIBRAS", BODEGA_ID);
    }

    /**
     * Mismatch explícito: PO en LIBRAS, catálogo en KG.
     * Authority by PO: auto-crear ítem con unidad de la compra (LIBRAS).
     */
    @Test
    void crearDesdeCompra_mismatchPoLibrasCatalogKg_autoCreaConUnidadPo() {
        RecursoSnapshot snapshotKg = new RecursoSnapshot(
                "MAT-001",
                CATALOG_SOURCE,
                "Cemento",
                TipoRecurso.MATERIAL,
                "KG",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        );
        when(catalogPort.fetchRecurso(eq("MAT-001"), eq(CATALOG_SOURCE))).thenReturn(snapshotKg);
        when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));

        CompraDetalle detalle = CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                "MAT-001",
                "Cemento",
                "LIBRAS", // PO en LIBRAS; catálogo KG → mismatch
                null,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("50"),
                new BigDecimal("30.00")
        );
        Compra compra = Compra.crear(CompraId.nuevo(), PROYECTO_ID, LocalDate.now(), "Proveedor", List.of(detalle));

        InventarioItem item = snapshotService.crearDesdeCompra(compra, detalle);

        assertThat(item).isNotNull();
        assertThat(item.getUnidadBase()).isEqualTo("LIBRAS"); // Authority by PO
        assertThat(item.getRecursoExternalId()).isEqualTo("MAT-001");
        assertThat(item.getNombre()).isEqualTo("Cemento");
        // No find by unit when mismatch; we create directly
        verify(catalogPort).fetchRecurso("MAT-001", CATALOG_SOURCE);
        verify(defaultBodegaPort).getDefaultForProject(PROYECTO_ID);
    }

    @Test
    void crearDesdeCompra_matchUnidad_findOrCreateUsaExistente() {
        RecursoSnapshot snapshot = new RecursoSnapshot(
                "MAT-001",
                CATALOG_SOURCE,
                "Cemento",
                TipoRecurso.MATERIAL,
                "BOL",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        );
        when(catalogPort.fetchRecurso(eq("MAT-001"), eq(CATALOG_SOURCE))).thenReturn(snapshot);
        when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));

        InventarioItem existente = InventarioItem.crearConSnapshot(
                com.budgetpro.domain.logistica.inventario.model.InventarioId.generate(),
                PROYECTO_ID,
                "MAT-001",
                BODEGA_ID,
                "Cemento",
                TipoRecurso.MATERIAL.name(),
                "BOL"
        );
        when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(
                eq(PROYECTO_ID), eq("MAT-001"), eq("BOL"), eq(BODEGA_ID)))
                .thenReturn(Optional.of(existente));

        CompraDetalle detalle = CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                "MAT-001",
                "Cemento",
                "BOL",
                null,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("10"),
                new BigDecimal("25.50")
        );
        Compra compra = Compra.crear(CompraId.nuevo(), PROYECTO_ID, LocalDate.now(), "Proveedor", List.of(detalle));

        InventarioItem item = snapshotService.crearDesdeCompra(compra, detalle);

        assertThat(item).isSameAs(existente);
    }

}
