package com.app.server.service;

import com.app.server.cache.CacheEntry;
import com.app.server.cache.CacheManager;
import com.app.server.config.CacheConfig;
import com.app.server.dto.ExchangeRateResponse;
import com.app.server.entity.ExchangeRateHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final CacheManager cacheManager;
    private final HistoryService historyService;
    private final CacheConfig cacheConfig;

    public ExchangeRateService(CacheManager cacheManager, HistoryService historyService, CacheConfig cacheConfig) {
        this.cacheManager = cacheManager;
        this.historyService = historyService;
        this.cacheConfig = cacheConfig;
    }

    @Transactional
    public ExchangeRateResponse addOrUpdateRate(String targetCurrency, double rate) {
        String normalizedTarget = targetCurrency.toUpperCase();
        String baseCurrency = cacheConfig.getBaseCurrency();
        
        historyService.addEntry(baseCurrency, normalizedTarget, rate);
        cacheManager.addRate(baseCurrency, normalizedTarget, rate);
        
        return new ExchangeRateResponse(baseCurrency, normalizedTarget, rate, "DIRECT");
    }

    public void invalidateRate(String targetCurrency) {
        String baseCurrency = cacheConfig.getBaseCurrency();
        cacheManager.invalidateRate(baseCurrency, targetCurrency.toUpperCase());
    }

    public ExchangeRateResponse resolveLookupRate(String fromCurrency, String toCurrency) {
        String normalizedFrom = fromCurrency.toUpperCase();
        String normalizedTo = toCurrency.toUpperCase();
        String baseCurrency = cacheConfig.getBaseCurrency();

        if (normalizedFrom.equals(normalizedTo)) {
            cacheManager.recordCacheHit();
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, 1.0, "CACHE");
        }

        CacheEntry directEntry = cacheManager.getCacheEntry(normalizedFrom, normalizedTo);
        if (directEntry != null && !cacheManager.isStale(directEntry)) {
            cacheManager.recordCacheHit();
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, directEntry.getRate(), "CACHE");
        }

        if (baseCurrency.equals(normalizedFrom)) {
            Double directRate = resolveBaseBasedRate(normalizedTo, true);
            if (directRate != null) {
                return new ExchangeRateResponse(normalizedFrom, normalizedTo, directRate, "HISTORY/CACHE");
            }
            throw new IllegalArgumentException("Exchange rate not found for " + normalizedFrom + " to " + normalizedTo);
        }

        if (baseCurrency.equals(normalizedTo)) {
            Double baseRate = resolveBaseBasedRate(normalizedFrom, true);
            if (baseRate == null || baseRate == 0.0) {
                throw new IllegalArgumentException("Exchange rate not found for " + normalizedFrom + " to " + normalizedTo);
            }

            double inverseRate = 1.0 / baseRate;
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, inverseRate, "INVERSE");
        }

        Double sourceRate = resolveBaseBasedRate(normalizedFrom, false);
        Double targetRate = resolveBaseBasedRate(normalizedTo, false);

        if (sourceRate == null || targetRate == null) {
            throw new IllegalArgumentException("Exchange rate not found for cross rate derivation.");
        }

        CacheEntry derivedEntry = cacheManager.deriveCrossRate(normalizedFrom, normalizedTo);
        if (derivedEntry == null) {
            throw new IllegalArgumentException("Exchange rate not found for " + normalizedFrom + " to " + normalizedTo);
        }

        cacheManager.recordDerivedRate();
        String[] dependencies = new String[]{baseCurrency + "-" + normalizedFrom, baseCurrency + "-" + normalizedTo};
        cacheManager.cacheDerivedRate(normalizedFrom, normalizedTo, derivedEntry.getRate(), dependencies);
        
        return new ExchangeRateResponse(normalizedFrom, normalizedTo, derivedEntry.getRate(), "DERIVED");
    }

    private Double resolveBaseBasedRate(String targetCurrency, boolean countFreshHit) {
        String baseCurrency = cacheConfig.getBaseCurrency();
        CacheEntry cacheEntry = cacheManager.getCacheEntry(baseCurrency, targetCurrency);

        if (cacheEntry != null) {
            if (!cacheManager.isStale(cacheEntry)) {
                if (countFreshHit) {
                    cacheManager.recordCacheHit();
                }
                return cacheEntry.getRate();
            }

            cacheManager.recordCacheMiss();
            // In API context, if stale, we try to fetch latest from history
            ExchangeRateHistory historyEntry = historyService.getLatestEntry(baseCurrency, targetCurrency);
            if (historyEntry != null) {
                cacheManager.cacheRate(baseCurrency, targetCurrency, historyEntry.getExchangeRate());
                cacheManager.recordRefresh();
                return historyEntry.getExchangeRate();
            }
            return null;
        }

        cacheManager.recordCacheMiss();
        ExchangeRateHistory historyEntry = historyService.getLatestEntry(baseCurrency, targetCurrency);

        if (historyEntry != null) {
            cacheManager.cacheRate(baseCurrency, targetCurrency, historyEntry.getExchangeRate());
            return historyEntry.getExchangeRate();
        }

        return null;
    }
    
    public List<ExchangeRateResponse> getAllDirectRates() {
        String baseCurrency = cacheConfig.getBaseCurrency();
        List<ExchangeRateResponse> rates = new ArrayList<>();
        var snapshot = cacheManager.getUnderlyingCache().getEntriesSnapshot();
        for (Map.Entry<String, CacheEntry> entry : snapshot) {
            String key = entry.getKey();
            if (key.startsWith(baseCurrency + "-")) {
                String target = key.substring(baseCurrency.length() + 1);
                rates.add(new ExchangeRateResponse(baseCurrency, target, entry.getValue().getRate(), "CACHE"));
            }
        }
        return rates;
    }
}
