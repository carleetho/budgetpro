package com.budgetpro.infrastructure.catalogo.adapter;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.RecursoSearchCriteria;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.catalogo.port.CatalogPort;
import com.budgetpro.infrastructure.catalogo.cache.CatalogCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

/**
 * Decorator con cache multinivel para el catálogo.
 */
@Component
@Primary
@Profile("!test")
@ConditionalOnProperty(name = "catalog.cache.enabled", havingValue = "true", matchIfMissing = true)
public class CachedCatalogAdapter implements CatalogPort {

    private final CatalogPort delegate;
    private final CatalogCache catalogCache;
    private final String warmSources;
    private final int warmLimit;

    public CachedCatalogAdapter(@Qualifier("capecoApiAdapter") CatalogPort delegate,
                                CatalogCache catalogCache,
                                @org.springframework.beans.factory.annotation.Value("${catalog.cache.warm.sources:CAPECO}") String warmSources,
                                @org.springframework.beans.factory.annotation.Value("${catalog.cache.warm.limit:100}") int warmLimit) {
        this.delegate = delegate;
        this.catalogCache = catalogCache;
        this.warmSources = warmSources;
        this.warmLimit = warmLimit;
    }

    @PostConstruct
    public void warmCache() {
        String[] sources = warmSources.split(",");
        for (String source : sources) {
            String catalogSource = source.trim();
            if (catalogSource.isBlank()) {
                continue;
            }
            try {
                RecursoSearchCriteria criteria = RecursoSearchCriteria.builder()
                        .limit(warmLimit)
                        .build();
                List<RecursoSnapshot> recursos = delegate.searchRecursos(criteria, catalogSource);
                for (RecursoSnapshot recurso : recursos) {
                    String key = buildKey(catalogSource, recurso.externalId());
                    catalogCache.putRecursoL1(key, recurso);
                    catalogCache.putRecursoL2(key, recurso);
                }
            } catch (RuntimeException ignored) {
                // Best-effort cache warming
            }
        }
    }

    @Override
    @Cacheable(cacheNames = "catalog-recursos-l1",
               cacheManager = "catalogCaffeineCacheManager",
               key = "#catalogSource + ':' + #externalId")
    public RecursoSnapshot fetchRecurso(String externalId, String catalogSource) {
        String key = buildKey(catalogSource, externalId);
        Optional<RecursoSnapshot> cached = catalogCache.getRecursoL2(key);
        if (cached.isPresent()) {
            return cached.get();
        }
        RecursoSnapshot snapshot = delegate.fetchRecurso(externalId, catalogSource);
        catalogCache.putRecursoL2(key, snapshot);
        return snapshot;
    }

    @Override
    @Cacheable(cacheNames = "catalog-recursos-search-l1",
               cacheManager = "catalogCaffeineCacheManager",
               key = "#catalogSource + ':' + (#criteria != null ? #criteria.hashCode() : 0)")
    public List<RecursoSnapshot> searchRecursos(RecursoSearchCriteria criteria, String catalogSource) {
        return delegate.searchRecursos(criteria, catalogSource);
    }

    @Override
    @Cacheable(cacheNames = "catalog-apus-l1",
               cacheManager = "catalogCaffeineCacheManager",
               key = "#catalogSource + ':' + #externalApuId")
    public APUSnapshot fetchAPU(String externalApuId, String catalogSource) {
        String key = buildKey(catalogSource, externalApuId);
        Optional<APUSnapshot> cached = catalogCache.getApuL2(key);
        if (cached.isPresent()) {
            return cached.get();
        }
        APUSnapshot snapshot = delegate.fetchAPU(externalApuId, catalogSource);
        catalogCache.putApuL2(key, snapshot);
        return snapshot;
    }

    @Override
    @Cacheable(cacheNames = "catalog-recurso-active-l1",
               cacheManager = "catalogCaffeineCacheManager",
               key = "#catalogSource + ':' + #externalId")
    public boolean isRecursoActive(String externalId, String catalogSource) {
        return delegate.isRecursoActive(externalId, catalogSource);
    }

    private String buildKey(String catalogSource, String externalId) {
        return catalogSource + ":" + externalId;
    }
}
