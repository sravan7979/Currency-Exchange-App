package com.app.server.cache;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Cache {

    private final HashMap<String, CacheEntry> cache;
    private final DateTimeFormatter dateTimeFormatter;

    public Cache() {
        this.cache = new HashMap<>();
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public void put(String key, double rate, long cachedTime) {
        cache.put(key, new CacheEntry(rate, cachedTime));
    }

    public void putDerived(String key, double rate, long cachedTime, String[] derivedFrom) {
        cache.put(key, new CacheEntry(rate, cachedTime, true, derivedFrom));
    }

    public CacheEntry get(String key) {
        return cache.get(key);
    }

    public CacheEntry remove(String key) {
        return cache.remove(key);
    }

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public ArrayList<Map.Entry<String, CacheEntry>> getEntriesSnapshot() {
        ArrayList<Map.Entry<String, CacheEntry>> entries = new ArrayList<>();
        for (Map.Entry<String, CacheEntry> item : cache.entrySet()) {
            entries.add(Map.entry(item.getKey(), item.getValue()));
        }
        return entries;
    }

    public String removeOldestEntry() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (Map.Entry<String, CacheEntry> item : cache.entrySet()) {
            long cachedTime = item.getValue().getCachedTime();
            if (cachedTime < oldestTime) {
                oldestTime = cachedTime;
                oldestKey = item.getKey();
            }
        }

        if (oldestKey != null) {
            cache.remove(oldestKey);
        }

        return oldestKey;
    }

    public int removeStaleEntries(Predicate<CacheEntry> staleChecker) {
        ArrayList<String> keysToRemove = new ArrayList<>();

        for (Map.Entry<String, CacheEntry> item : cache.entrySet()) {
            if (staleChecker.test(item.getValue())) {
                keysToRemove.add(item.getKey());
            }
        }

        for (String key : keysToRemove) {
            cache.remove(key);
        }

        return keysToRemove.size();
    }

    public int countFreshEntries(Predicate<CacheEntry> staleChecker) {
        int freshCount = 0;
        for (Map.Entry<String, CacheEntry> item : cache.entrySet()) {
            if (!staleChecker.test(item.getValue())) {
                freshCount++;
            }
        }
        return freshCount;
    }

    public int countStaleEntries(Predicate<CacheEntry> staleChecker) {
        int staleCount = 0;
        for (Map.Entry<String, CacheEntry> item : cache.entrySet()) {
            if (staleChecker.test(item.getValue())) {
                staleCount++;
            }
        }
        return staleCount;
    }

    public void clear() {
        cache.clear();
    }

    public boolean isStale(CacheEntry entry, long cacheTtlMillis) {
        if (entry == null) {
            return true;
        }
        return System.currentTimeMillis() - entry.getCachedTime() > cacheTtlMillis;
    }

    public long getAgeInSeconds(CacheEntry entry) {
        if (entry == null) {
            return 0L;
        }
        return (System.currentTimeMillis() - entry.getCachedTime()) / 1000L;
    }

    private String formatDateTime(long cachedTime) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(cachedTime), ZoneId.systemDefault());
        return dateTimeFormatter.format(localDateTime);
    }
}
