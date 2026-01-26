package com.budgetpro.infrastructure.catalogo.cache;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Cache de catálogo con estrategia multinivel (L1 in-memory, L2 Redis).
 */
public interface CatalogCache {

    Optional<RecursoSnapshot> getRecursoL2(String key);

    void putRecursoL2(String key, RecursoSnapshot value);

    void putRecursoL1(String key, RecursoSnapshot value);

    Optional<APUSnapshot> getApuL2(String key);

    void putApuL2(String key, APUSnapshot value);

    void putApuL1(String key, APUSnapshot value);

    void evictRecurso(String key);

    void evictApu(String key);

    void evictAll();

    double getL1HitRate();
}

@Component
class DefaultCatalogCache implements CatalogCache {

    static final String RECURSOS_L1 = "catalog-recursos-l1";
    static final String APUS_L1 = "catalog-apus-l1";
    static final String RECURSOS_L2 = "catalog-recursos-l2";
    static final String APUS_L2 = "catalog-apus-l2";

    private final CacheManager l1CacheManager;
    private final CacheManager l2CacheManager;

    DefaultCatalogCache(CacheManager catalogCaffeineCacheManager,
                        CacheManager catalogRedisCacheManager) {
        this.l1CacheManager = Objects.requireNonNull(catalogCaffeineCacheManager, "L1 cache manager no puede ser nulo");
        this.l2CacheManager = Objects.requireNonNull(catalogRedisCacheManager, "L2 cache manager no puede ser nulo");
    }

    @Override
    public Optional<RecursoSnapshot> getRecursoL2(String key) {
        return getFromCache(l2CacheManager, RECURSOS_L2, key, RecursoSnapshot.class);
    }

    @Override
    public void putRecursoL2(String key, RecursoSnapshot value) {
        putInCache(l2CacheManager, RECURSOS_L2, key, value);
    }

    @Override
    public void putRecursoL1(String key, RecursoSnapshot value) {
        putInCache(l1CacheManager, RECURSOS_L1, key, value);
    }

    @Override
    public Optional<APUSnapshot> getApuL2(String key) {
        return getFromCache(l2CacheManager, APUS_L2, key, APUSnapshot.class);
    }

    @Override
    public void putApuL2(String key, APUSnapshot value) {
        putInCache(l2CacheManager, APUS_L2, key, value);
    }

    @Override
    public void putApuL1(String key, APUSnapshot value) {
        putInCache(l1CacheManager, APUS_L1, key, value);
    }

    @Override
    public void evictRecurso(String key) {
        evictFromCache(l1CacheManager, RECURSOS_L1, key);
        evictFromCache(l2CacheManager, RECURSOS_L2, key);
    }

    @Override
    public void evictApu(String key) {
        evictFromCache(l1CacheManager, APUS_L1, key);
        evictFromCache(l2CacheManager, APUS_L2, key);
    }

    @Override
    public void evictAll() {
        clearCache(l1CacheManager, RECURSOS_L1);
        clearCache(l1CacheManager, APUS_L1);
        clearCache(l2CacheManager, RECURSOS_L2);
        clearCache(l2CacheManager, APUS_L2);
    }

    @Override
    public double getL1HitRate() {
        Cache recursosCache = l1CacheManager.getCache(RECURSOS_L1);
        Cache apusCache = l1CacheManager.getCache(APUS_L1);
        double recursos = getHitRate(recursosCache);
        double apus = getHitRate(apusCache);
        return (recursos + apus) / 2.0;
    }

    private <T> Optional<T> getFromCache(CacheManager manager, String cacheName, String key, Class<T> type) {
        Cache cache = manager.getCache(Objects.requireNonNull(cacheName, "cacheName no puede ser nulo"));
        if (cache == null) {
            return Optional.empty();
        }
        T value = cache.get(Objects.requireNonNull(key, "key no puede ser nulo"), type);
        return Optional.ofNullable(value);
    }

    private void putInCache(CacheManager manager, String cacheName, String key, Object value) {
        Cache cache = manager.getCache(Objects.requireNonNull(cacheName, "cacheName no puede ser nulo"));
        if (cache != null) {
            cache.put(Objects.requireNonNull(key, "key no puede ser nulo"), value);
        }
    }

    private void evictFromCache(CacheManager manager, String cacheName, String key) {
        Cache cache = manager.getCache(Objects.requireNonNull(cacheName, "cacheName no puede ser nulo"));
        if (cache != null) {
            cache.evict(Objects.requireNonNull(key, "key no puede ser nulo"));
        }
    }

    private void clearCache(CacheManager manager, String cacheName) {
        Cache cache = manager.getCache(Objects.requireNonNull(cacheName, "cacheName no puede ser nulo"));
        if (cache != null) {
            cache.clear();
        }
    }

    private double getHitRate(Cache cache) {
        if (cache instanceof CaffeineCache caffeineCache) {
            CacheStats stats = caffeineCache.getNativeCache().stats();
            return stats.hitRate();
        }
        return 0.0;
    }
}
