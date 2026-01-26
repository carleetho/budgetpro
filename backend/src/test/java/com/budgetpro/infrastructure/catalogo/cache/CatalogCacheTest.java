package com.budgetpro.infrastructure.catalogo.cache;

import com.budgetpro.domain.catalogo.model.APUSnapshot;
import com.budgetpro.domain.catalogo.model.APUSnapshotId;
import com.budgetpro.domain.catalogo.model.RecursoSnapshot;
import com.budgetpro.domain.shared.model.TipoRecurso;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogCacheTest {

    @Test
    void getRecursoL2_debePromoverAL1() {
        CacheManager l1 = caffeineManager();
        CacheManager l2 = new ConcurrentMapCacheManager(
                DefaultCatalogCache.RECURSOS_L2,
                DefaultCatalogCache.APUS_L2
        );
        CatalogCache cache = new DefaultCatalogCache(l1, l2);

        String key = "CAPECO:MAT-001";
        RecursoSnapshot recurso = new RecursoSnapshot(
                "MAT-001",
                "CAPECO",
                "CEMENTO",
                TipoRecurso.MATERIAL,
                "BOL",
                new BigDecimal("25.50"),
                LocalDateTime.now()
        );

        cache.putRecursoL2(key, recurso);
        Optional<RecursoSnapshot> fromL2 = cache.getRecursoL2(key);
        assertTrue(fromL2.isPresent());

        cache.putRecursoL1(key, recurso);
        RecursoSnapshot fromL1 = java.util.Objects.requireNonNull(
                l1.getCache(DefaultCatalogCache.RECURSOS_L1),
                "Cache L1 no puede ser nula"
        ).get(key, RecursoSnapshot.class);
        assertEquals("MAT-001", java.util.Objects.requireNonNull(fromL1).externalId());
    }

    @Test
    void putApuL2_y_eviction_debenFuncionar() {
        CacheManager l1 = caffeineManager();
        CacheManager l2 = new ConcurrentMapCacheManager(
                DefaultCatalogCache.RECURSOS_L2,
                DefaultCatalogCache.APUS_L2
        );
        CatalogCache cache = new DefaultCatalogCache(l1, l2);

        APUSnapshot apu = APUSnapshot.crear(
                APUSnapshotId.generate(),
                UUID.randomUUID(),
                "APU-001",
                "CAPECO",
                new BigDecimal("1.0"),
                "UND",
                LocalDateTime.now()
        );

        String key = "CAPECO:APU-001";
        cache.putApuL2(key, apu);
        assertTrue(cache.getApuL2(key).isPresent());

        cache.evictApu(key);
        assertTrue(cache.getApuL2(key).isEmpty());
    }

    private CacheManager caffeineManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                DefaultCatalogCache.RECURSOS_L1,
                DefaultCatalogCache.APUS_L1
        );
        manager.setCaffeine(java.util.Objects.requireNonNull(Caffeine.newBuilder().maximumSize(10)));
        return manager;
    }
}
