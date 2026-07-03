package com.app.server.cache;

import com.app.server.config.CacheConfig;
import org.springframework.stereotype.Component;

@Component
public class CacheManager {

    private final CacheConfig cacheConfig;
    private final Cache cache;
    
    private int cacheHits;
    private int cacheMisses;
    private int refreshCount;
    private int derivedRates;

    public CacheManager(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        this.cache = new Cache();
    }

    public void addRate(String fromCurrency, String toCurrency, double rate) {
        validateRate(rate);
        cacheRate(fromCurrency, toCurrency, rate);
    }

    public void cacheRate(String fromCurrency, String toCurrency, double rate) {
        validateRate(rate);
        storeRate(fromCurrency, toCurrency, rate);
        
        if (cacheConfig.getBaseCurrency().equals(fromCurrency) || cacheConfig.getBaseCurrency().equals(toCurrency)) {
            invalidateDerivedRates(buildKey(fromCurrency, toCurrency));
        }
    }

    public void cacheDerivedRate(String fromCurrency, String toCurrency, double rate, String[] deps) {
        validateRate(rate);
        storeDerivedRate(fromCurrency, toCurrency, rate, deps);
    }

    public double getRate(String fromCurrency, String toCurrency) {
        String key = buildKey(fromCurrency, toCurrency);
        CacheEntry entry = cache.get(key);

        if (entry == null || isStale(entry)) {
            return Double.NaN;
        }

        cacheHits++;
        return entry.getRate();
    }

    public CacheEntry deriveCrossRate(String fromCurrency, String toCurrency) {
        String normalizedFrom = normalizeCurrency(fromCurrency);
        String normalizedTo = normalizeCurrency(toCurrency);
        String base = cacheConfig.getBaseCurrency();

        if (base.equals(normalizedFrom) || base.equals(normalizedTo)) {
            return null;
        }

        CacheEntry fromBase = cache.get(buildKey(base, normalizedFrom));
        CacheEntry baseTo = cache.get(buildKey(base, normalizedTo));

        if (fromBase == null || baseTo == null || isStale(fromBase) || isStale(baseTo)) {
            return null;
        }

        double derivedRate = (1.0 / fromBase.getRate()) * baseTo.getRate();
        return new CacheEntry(derivedRate, System.currentTimeMillis());
    }

    public void invalidateRate(String fromCurrency, String toCurrency) {
        String key = buildKey(fromCurrency, toCurrency);
        cache.remove(key);
    }
    
    public void clearCache() {
        cache.clear();
    }

    private void invalidateDerivedRates(String changedKey) {
        var snapshot = cache.getEntriesSnapshot();
        for (var entry : snapshot) {
            CacheEntry cacheEntry = entry.getValue();
            if (cacheEntry.isDerived()) {
                String[] deps = cacheEntry.getDerivedFrom();
                if (deps != null) {
                    for (String dep : deps) {
                        if (dep.equals(changedKey)) {
                            cache.remove(entry.getKey());
                            break;
                        }
                    }
                }
            }
        }
    }

    public int removeStaleEntries() {
        return cache.removeStaleEntries(this::isStale);
    }

    public void recordCacheMiss() {
        cacheMisses++;
    }

    public void recordCacheHit() {
        cacheHits++;
    }
    
    public void recordRefresh() {
        refreshCount++;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getCacheMisses() {
        return cacheMisses;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public int getDerivedRates() {
        return derivedRates;
    }

    public void recordDerivedRate() {
        derivedRates++;
    }

    public boolean isStale(CacheEntry entry) {
        return cache.isStale(entry, cacheConfig.getCacheTtl());
    }

    public CacheEntry getCacheEntry(String fromCurrency, String toCurrency) {
        return cache.get(buildKey(fromCurrency, toCurrency));
    }

    public boolean containsRate(String fromCurrency, String toCurrency) {
        return cache.contains(buildKey(fromCurrency, toCurrency));
    }

    public int getCurrentCacheSize() {
        return cache.size();
    }
    
    public int getFreshEntries() {
        return cache.countFreshEntries(this::isStale);
    }
    
    public int getStaleEntries() {
        return cache.countStaleEntries(this::isStale);
    }
    
    public Cache getUnderlyingCache() {
        return cache;
    }

    private String buildKey(String fromCurrency, String toCurrency) {
        return normalizeCurrency(fromCurrency) + "-" + normalizeCurrency(toCurrency);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || !currency.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Currency codes must contain exactly 3 alphabetic characters.");
        }
        return currency.toUpperCase();
    }

    private void validateRate(double rate) {
        if (rate <= 0.0) {
            throw new IllegalArgumentException("Rate must be a positive number.");
        }
    }

    private void storeRate(String fromCurrency, String toCurrency, double rate) {
        String key = buildKey(fromCurrency, toCurrency);

        if (!cache.contains(key) && cache.size() >= cacheConfig.getMaxCacheSize()) {
            cache.removeOldestEntry();
        }

        cache.put(key, rate, System.currentTimeMillis());
    }

    private void storeDerivedRate(String fromCurrency, String toCurrency, double rate, String[] deps) {
        String key = buildKey(fromCurrency, toCurrency);

        if (!cache.contains(key) && cache.size() >= cacheConfig.getMaxCacheSize()) {
            cache.removeOldestEntry();
        }

        cache.putDerived(key, rate, System.currentTimeMillis(), deps);
    }
}
