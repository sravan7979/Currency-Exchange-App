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
    public ExchangeRateResponse addOrUpdateRate(String reqBase, String targetCurrency, double rate) {
        String sysBase = cacheConfig.getBaseCurrency();
        String target;
        double finalRate;
        
        if (sysBase.equalsIgnoreCase(targetCurrency)) {
            // e.g. User sent USD -> INR. System only stores INR -> X. We convert to INR -> USD.
            target = reqBase.toUpperCase();
            finalRate = 1.0 / rate;
        } else {
            target = targetCurrency.toUpperCase();
            finalRate = rate;
        }
        
        historyService.addEntry(sysBase, target, finalRate);
        cacheManager.addRate(sysBase, target, finalRate);
        
        return new ExchangeRateResponse(sysBase, target, finalRate, "DIRECT");
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

        // STEP 1: Check Cache for FROM -> TO
        CacheEntry directEntry = cacheManager.getCacheEntry(normalizedFrom, normalizedTo);
        if (directEntry != null && !cacheManager.isStale(directEntry)) {
            cacheManager.recordCacheHit();
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, directEntry.getRate(), "CACHE");
        }

        // STEP 2: Check Cache for TO -> FROM
        CacheEntry inverseEntry = cacheManager.getCacheEntry(normalizedTo, normalizedFrom);
        if (inverseEntry != null && !cacheManager.isStale(inverseEntry)) {
            cacheManager.recordCacheHit();
            double inverseRate = 1.0 / inverseEntry.getRate();
            String[] dependencies = new String[]{normalizedTo + "-" + normalizedFrom};
            cacheManager.cacheDerivedRate(normalizedFrom, normalizedTo, inverseRate, dependencies);
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, inverseRate, "INVERSE_CACHE");
        }

        cacheManager.recordCacheMiss();

        // STEP 3: Check History for FROM -> TO
        ExchangeRateHistory historyDirect = historyService.getLatestEntry(normalizedFrom, normalizedTo);
        if (historyDirect != null) {
            double rate = historyDirect.getExchangeRate();
            cacheManager.cacheRate(normalizedFrom, normalizedTo, rate);
            cacheManager.recordRefresh();
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, rate, "HISTORY");
        }

        // STEP 4: Check History for TO -> FROM
        ExchangeRateHistory historyInverse = historyService.getLatestEntry(normalizedTo, normalizedFrom);
        if (historyInverse != null) {
            double rate = 1.0 / historyInverse.getExchangeRate();
            String[] dependencies = new String[]{normalizedTo + "-" + normalizedFrom};
            cacheManager.cacheDerivedRate(normalizedFrom, normalizedTo, rate, dependencies);
            cacheManager.recordRefresh();
            return new ExchangeRateResponse(normalizedFrom, normalizedTo, rate, "INVERSE_HISTORY");
        }

        // STEP 5: Cross-rate derivation using baseCurrency
        if (!baseCurrency.equals(normalizedFrom) && !baseCurrency.equals(normalizedTo)) {
            Double rateFrom = getDirectBaseRate(normalizedFrom);
            Double rateTo = getDirectBaseRate(normalizedTo);

            if (rateFrom != null && rateTo != null) {
                double derivedRate = rateTo / rateFrom;
                
                // debugging logs
                // System.out.println("Requested Pair: " + normalizedFrom + " -> " + normalizedTo);
                // System.out.println("INR -> " + normalizedFrom + " rate = " + String.format("%.6f", rateFrom));
                // System.out.println("INR -> " + normalizedTo + " rate = " + String.format("%.6f", rateTo));
                // System.out.println("Formula used: " + String.format("%.6f", rateTo) + " / " + String.format("%.6f", rateFrom));
                // System.out.println("Final Result: " + String.format("%.9f", derivedRate));

                String[] dependencies = new String[]{baseCurrency + "-" + normalizedFrom, baseCurrency + "-" + normalizedTo};
                cacheManager.cacheDerivedRate(normalizedFrom, normalizedTo, derivedRate, dependencies);
                return new ExchangeRateResponse(normalizedFrom, normalizedTo, derivedRate, "DERIVED");
            }
        }

        // STEP 6: Return NOT FOUND
        throw new IllegalArgumentException("Exchange rate not found for " + normalizedFrom + " to " + normalizedTo);
    }

    private Double getDirectBaseRate(String targetCurrency) {
        String baseCurrency = cacheConfig.getBaseCurrency();

        CacheEntry entry = cacheManager.getCacheEntry(baseCurrency, targetCurrency);
        if (entry != null && !cacheManager.isStale(entry) && !entry.isDerived()) {
            return entry.getRate();
        }

        ExchangeRateHistory historyEntry = historyService.getLatestEntry(baseCurrency, targetCurrency);
        if (historyEntry != null) {
            double rate = historyEntry.getExchangeRate();
            cacheManager.cacheRate(baseCurrency, targetCurrency, rate);
            return rate;
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
