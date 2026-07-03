package com.app.server.controller;

import com.app.server.cache.CacheEntry;
import com.app.server.dto.CacheStatisticsResponse;
import com.app.server.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getCache() {
        var snapshot = cacheService.getCacheManager().getUnderlyingCache().getEntriesSnapshot();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<String, CacheEntry> entry : snapshot) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", entry.getKey());
            map.put("rate", entry.getValue().getRate());
            map.put("isDerived", entry.getValue().isDerived());
            map.put("derivedFrom", entry.getValue().getDerivedFrom());
            map.put("ageSeconds", cacheService.getCacheManager().getUnderlyingCache().getAgeInSeconds(entry.getValue()));
            map.put("isStale", cacheService.getCacheManager().isStale(entry.getValue()));
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics")
    public ResponseEntity<CacheStatisticsResponse> getCacheStatistics() {
        return ResponseEntity.ok(cacheService.getStatistics());
    }

    @DeleteMapping("/stale")
    public ResponseEntity<Map<String, Integer>> removeStaleEntries() {
        int removed = cacheService.removeStaleEntries();
        return ResponseEntity.ok(Map.of("removed", removed));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCache() {
        cacheService.clearCache();
        return ResponseEntity.noContent().build();
    }
}
