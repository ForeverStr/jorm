package io.github.foreverstr.cache;

public interface SecondLevelCache {
    void put(String region, String key, Object value);
    Object get(String region, String key);
    void remove(String region, String key);
    void clearRegion(String region);
    void clearAll();
}