package com.lixin.capability.weather.cache;

import com.lixin.capability.weather.dto.WeatherResult;

public interface WeatherCache {
    WeatherResult get(String key);

    void put(String key, WeatherResult result, long ttlMillis);

    void clear();
}
