package com.budgetpro.infrastructure.catalogo.adapter;

import com.budgetpro.domain.catalogo.exception.CatalogNotFoundException;
import com.budgetpro.domain.catalogo.exception.CatalogServiceException;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshot;
import com.budgetpro.domain.catalogo.model.APUInsumoSnapshotId;
import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Adaptador mock para catálogos externos. Usa datos en memoria para pruebas y
 * fase de dual-write.
 */
@Component
@Profile({ "test", "mock" })
public class MockCatalogAdapter implements CatalogPort {

    private static final UUID MOCK_PARTIDA_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Map<String, RecursoSnapshot> recursosPorClave = new HashMap<>();
    private final Map<String, APUSnapshot> apusPorClave = new HashMap<>();
    private final Set<String> recursosInactivos = new HashSet<>();
    private final AtomicBoolean failNext = new AtomicBoolean(false);

    private long delayMs;

    public MockCatalogAdapter() {
        this.delayMs = 0;
        cargarDatosBase();
    }

    @Override
    public RecursoSnapshot fetchRecurso(String externalId, String catalogSource) {
        simularLatencia();
        verificarFalloForzado();

        String key = buildKey(externalId, catalogSource);
        RecursoSnapshot snapshot = recursosPorClave.get(key);
        if (snapshot == null) {
            throw new CatalogNotFoundException(externalId, catalogSource);
        }
        return snapshot;
    }

    @Override
    public List<RecursoSnapshot> searchRecursos(RecursoSearchCriteria criteria, String catalogSource) {
        simularLatencia();
        verificarFalloForzado();

        List<RecursoSnapshot> base = recursosPorClave.values().stream()
                .filter(snapshot -> snapshot.catalogSource().equals(catalogSource)).collect(Collectors.toList());

        if (criteria == null) {
            return base;
        }

        return base.stream().filter(snapshot -> matchesQuery(snapshot, criteria))
                .filter(snapshot -> matchesTipo(snapshot, criteria))
                .filter(snapshot -> matchesUnidad(snapshot, criteria))
                .limit(criteria.getLimit() != null ? criteria.getLimit() : base.size()).collect(Collectors.toList());
    }

    @Override
    public APUSnapshot fetchAPU(String externalApuId, String catalogSource) {
        simularLatencia();
        verificarFalloForzado();

        String key = buildKey(externalApuId, catalogSource);
        APUSnapshot snapshot = apusPorClave.get(key);
        if (snapshot == null) {
            throw new CatalogNotFoundException(externalApuId, catalogSource);
        }
        return snapshot;
    }

    @Override
    public boolean isRecursoActive(String externalId, String catalogSource) {
        simularLatencia();
        verificarFalloForzado();

        String key = buildKey(externalId, catalogSource);
        return recursosPorClave.containsKey(key) && !recursosInactivos.contains(key);
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = Math.max(0, delayMs);
    }

    public void setFailNext(boolean value) {
        this.failNext.set(value);
    }

    public void setRecursoActivo(String externalId, String catalogSource, boolean activo) {
        String key = buildKey(externalId, catalogSource);
        if (activo) {
            recursosInactivos.remove(key);
        } else {
            recursosInactivos.add(key);
        }
    }

    private void cargarDatosBase() {
        RecursoSnapshot cemento = new RecursoSnapshot("MAT-001", "CAPECO", "CEMENTO PORTLAND TIPO I",
                TipoRecurso.MATERIAL, "BOL", new BigDecimal("25.50"), LocalDateTime.now());
        RecursoSnapshot acero = new RecursoSnapshot("MAT-002", "CAPECO", "ACERO CORRUGADO 3/8\"", TipoRecurso.MATERIAL,
                "KG", new BigDecimal("4.20"), LocalDateTime.now());
        recursosPorClave.put(buildKey(cemento.externalId(), cemento.catalogSource()), cemento);
        recursosPorClave.put(buildKey(acero.externalId(), acero.catalogSource()), acero);

        APUSnapshot apu = APUSnapshot.crear(APUSnapshotId.generate(), MOCK_PARTIDA_ID, "APU-001", "CAPECO",
                new BigDecimal("1.0"), "UND", LocalDateTime.now());
        APUInsumoSnapshot insumo1 = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(), "MAT-001",
                "CEMENTO PORTLAND TIPO I", new BigDecimal("2.0"), new BigDecimal("25.50"));
        APUInsumoSnapshot insumo2 = APUInsumoSnapshot.crear(APUInsumoSnapshotId.generate(), "MAT-002",
                "ACERO CORRUGADO 3/8\"", new BigDecimal("3.5"), new BigDecimal("4.20"));
        apu = apu.agregarInsumo(insumo1);
        apu = apu.agregarInsumo(insumo2);
        apusPorClave.put(buildKey("APU-001", "CAPECO"), apu);
    }

    private void simularLatencia() {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatalogServiceException("MOCK", "Interrupción durante la latencia simulada", e);
        }
    }

    private void verificarFalloForzado() {
        if (failNext.getAndSet(false)) {
            throw new CatalogServiceException("MOCK", "Fallo simulado del catálogo");
        }
    }

    private boolean matchesQuery(RecursoSnapshot snapshot, RecursoSearchCriteria criteria) {
        String query = criteria.getQuery();
        if (query == null) {
            return true;
        }
        return snapshot.nombre().toLowerCase().contains(query.toLowerCase());
    }

    private boolean matchesTipo(RecursoSnapshot snapshot, RecursoSearchCriteria criteria) {
        TipoRecurso tipo = criteria.getTipo();
        return tipo == null || snapshot.tipo() == tipo;
    }

    private boolean matchesUnidad(RecursoSnapshot snapshot, RecursoSearchCriteria criteria) {
        String unidad = criteria.getUnidad();
        if (unidad == null) {
            return true;
        }
        return snapshot.unidad().equalsIgnoreCase(unidad);
    }

    private String buildKey(String externalId, String catalogSource) {
        return externalId + "|" + catalogSource;
    }
}
