package com.budgetpro.infrastructure.catalogo.adapter;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.recurso.model.TipoRecurso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockCatalogAdapterTest {

    private final MockCatalogAdapter adapter = new MockCatalogAdapter();

    @Test
    void fetchRecurso_debeRetornarSnapshot() {
        RecursoSnapshot snapshot = adapter.fetchRecurso("MAT-001", "CAPECO");
        assertEquals("MAT-001", snapshot.externalId());
        assertEquals("CAPECO", snapshot.catalogSource());
    }

    @Test
    void fetchRecurso_inexistente_debeLanzarNotFound() {
        assertThrows(CatalogNotFoundException.class,
                () -> adapter.fetchRecurso("NO-EXISTE", "CAPECO"));
    }

    @Test
    void searchRecursos_debeFiltrar() {
        RecursoSearchCriteria criteria = RecursoSearchCriteria.builder()
                .tipo(TipoRecurso.MATERIAL)
                .unidad("KG")
                .build();

        List<RecursoSnapshot> results = adapter.searchRecursos(criteria, "CAPECO");
        assertEquals(1, results.size());
        assertEquals("MAT-002", results.get(0).externalId());
    }

    @Test
    void fetchAPU_debeRetornarSnapshot() {
        APUSnapshot snapshot = adapter.fetchAPU("APU-001", "CAPECO");
        assertEquals("APU-001", snapshot.getExternalApuId());
        assertNotNull(snapshot.getInsumos());
        assertTrue(snapshot.getInsumos().size() >= 2);
    }

    @Test
    void isRecursoActive_debeRespetarEstados() {
        adapter.setRecursoActivo("MAT-001", "CAPECO", false);
        assertTrue(!adapter.isRecursoActive("MAT-001", "CAPECO"));
        adapter.setRecursoActivo("MAT-001", "CAPECO", true);
        assertTrue(adapter.isRecursoActive("MAT-001", "CAPECO"));
    }

    @Test
    void falloSimulado_debeLanzarServiceException() {
        adapter.setFailNext(true);
        assertThrows(CatalogServiceException.class,
                () -> adapter.fetchRecurso("MAT-001", "CAPECO"));
    }
}
