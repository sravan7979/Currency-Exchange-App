package com.app.server.service;

import com.app.server.cache.CacheManager;
import com.app.server.config.CacheConfig;
import com.app.server.dto.CacheStatisticsResponse;
import com.app.server.repository.ExchangeRateHistoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private final CacheManager cacheManager;
    private final CacheConfig cacheConfig;
    private final ExchangeRateHistoryRepository historyRepository;

    public CacheService(CacheManager cacheManager, CacheConfig cacheConfig, ExchangeRateHistoryRepository historyRepository) {
        this.cacheManager = cacheManager;
        this.cacheConfig = cacheConfig;
        this.historyRepository = historyRepository;
    }

    public CacheStatisticsResponse getStatistics() {
        long historyCount = historyRepository.count();
        return CacheStatisticsResponse.builder()
                .cacheHits(cacheManager.getCacheHits())
                .cacheMisses(cacheManager.getCacheMisses())
                .refreshCount(cacheManager.getRefreshCount())
                .derivedRates(cacheManager.getDerivedRates())
                .freshEntries(cacheManager.getFreshEntries())
                .staleEntries(cacheManager.getStaleEntries())
                .currentCacheSize(cacheManager.getCurrentCacheSize())
                .maximumCacheSize(cacheConfig.getMaxCacheSize())
                .historyRecords(historyCount)
                .build();
    }

    public int removeStaleEntries() {
        return cacheManager.removeStaleEntries();
    }

    public void clearCache() {
        cacheManager.clearCache();
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
