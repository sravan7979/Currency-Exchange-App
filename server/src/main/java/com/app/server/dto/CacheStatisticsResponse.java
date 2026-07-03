package com.app.server.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CacheStatisticsResponse {
    private int cacheHits;
    private int cacheMisses;
    private int refreshCount;
    private int derivedRates;
    private int freshEntries;
    private int staleEntries;
    private int currentCacheSize;
    private int maximumCacheSize;
    private long historyRecords;
}
