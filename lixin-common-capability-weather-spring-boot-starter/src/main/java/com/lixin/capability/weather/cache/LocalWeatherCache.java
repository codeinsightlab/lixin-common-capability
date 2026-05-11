package com.lixin.capability.weather.cache;

import com.lixin.capability.weather.dto.WeatherResult;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalWeatherCache implements WeatherCache {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Clock clock;

    public LocalWeatherCache() {
        this(Clock.systemUTC());
    }

    public LocalWeatherCache(Clock clock) {
        this.clock = clock;
    }

    @Override
    public WeatherResult get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expireAtMillis <= clock.millis()) {
            cache.remove(key, entry);
            return null;
        }
        return entry.result;
    }

    @Override
    public void put(String key, WeatherResult result, long ttlMillis) {
        if (ttlMillis <= 0 || result == null || !result.isSuccess()) {
            return;
        }
        cache.put(key, new CacheEntry(result, clock.millis() + ttlMillis));
    }

    @Override
    public void clear() {
        cache.clear();
    }

    private static class CacheEntry {
        private final WeatherResult result;
        private final long expireAtMillis;

        private CacheEntry(WeatherResult result, long expireAtMillis) {
            this.result = result;
            this.expireAtMillis = expireAtMillis;
        }
    }
}
