package com.app.server.cache;

public class CacheEntry {

    private double rate;
    private long cachedTime;
    private boolean derived;
    private String[] derivedFrom;

    public CacheEntry(double rate, long cachedTime) {
        this.rate = rate;
        this.cachedTime = cachedTime;
        this.derived = false;
        this.derivedFrom = null;
    }

    public CacheEntry(double rate, long cachedTime, boolean derived, String[] derivedFrom) {
        this.rate = rate;
        this.cachedTime = cachedTime;
        this.derived = derived;
        this.derivedFrom = derivedFrom;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public long getCachedTime() {
        return cachedTime;
    }

    public void setCachedTime(long cachedTime) {
        this.cachedTime = cachedTime;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public String[] getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(String[] derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    @Override
    public String toString() {
        return "CacheEntry{" +
                "rate=" + rate +
                ", cachedTime=" + cachedTime +
                ", derived=" + derived +
                ", derivedFrom=" + (derivedFrom != null ? String.join(", ", derivedFrom) : "null") +
                '}';
    }
}
