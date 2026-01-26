package com.budgetpro.infrastructure.rest.catalogo.controller;

import com.budgetpro.infrastructure.catalogo.cache.CatalogCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de invalidación y métricas de cache de catálogo.
 */
@RestController
@RequestMapping("/api/v1/catalogo/cache")
public class CatalogCacheController {

    private final CatalogCache catalogCache;

    public CatalogCacheController(CatalogCache catalogCache) {
        this.catalogCache = catalogCache;
    }

    @PostMapping("/evict")
    public ResponseEntity<Void> evict(@RequestParam String tipo,
                                      @RequestParam(required = false) String externalId,
                                      @RequestParam(required = false) String catalogSource) {
        if ("all".equalsIgnoreCase(tipo)) {
            catalogCache.evictAll();
            return ResponseEntity.noContent().build();
        }
        if (externalId == null || catalogSource == null) {
            return ResponseEntity.badRequest().build();
        }
        String key = catalogSource + ":" + externalId;
        if ("recurso".equalsIgnoreCase(tipo)) {
            catalogCache.evictRecurso(key);
        } else if ("apu".equalsIgnoreCase(tipo)) {
            catalogCache.evictApu(key);
        } else {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of("l1HitRate", catalogCache.getL1HitRate());
    }
}
