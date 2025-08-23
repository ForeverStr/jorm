package io.github.foreverstr.cache;

import io.github.foreverstr.cache.impl.NoOpSecondLevelCache;

public class CacheManager {
    private static SecondLevelCache secondLevelCache = new NoOpSecondLevelCache();
    private static boolean cacheEnabled = false;

    // 设置缓存实现
    public static void setSecondLevelCache(SecondLevelCache cache) {
        secondLevelCache = cache;
        cacheEnabled = (cache != null && !(cache instanceof NoOpSecondLevelCache));
    }

    public static SecondLevelCache getSecondLevelCache() {
        return secondLevelCache;
    }

    public static boolean isCacheEnabled() {
        return cacheEnabled && secondLevelCache != null;
    }

    public static void setCacheEnabled(boolean enabled) {
        cacheEnabled = enabled;
        if (!enabled) {
            secondLevelCache = new NoOpSecondLevelCache();
        }
    }
}