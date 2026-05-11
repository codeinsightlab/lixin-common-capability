package com.lixin.capability.weather.service;

import com.lixin.capability.weather.cache.WeatherCache;
import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.exception.WeatherException;
import com.lixin.capability.weather.exception.WeatherInvalidRequestException;
import com.lixin.capability.weather.properties.WeatherProperties;
import com.lixin.capability.weather.provider.AmapWeatherProvider;
import com.lixin.capability.weather.provider.WeatherProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultWeatherService implements WeatherService {
    private static final String PROVIDER_AMAP = "amap";

    private final WeatherProperties properties;
    private final Map<String, WeatherProvider> providers;
    private final WeatherCache cache;

    public DefaultWeatherService(WeatherProperties properties, List<WeatherProvider> providers, WeatherCache cache) {
        this.properties = properties;
        this.providers = toProviderMap(providers);
        this.cache = cache;
    }

    @Override
    public WeatherResult getCurrentWeather(WeatherQuery query) {
        String providerName = resolveProvider(query);
        if (properties == null || !properties.isEnabled()) {
            return WeatherResult.failure(providerName, "weather capability is disabled");
        }
        if (PROVIDER_AMAP.equals(providerName) && !hasText(properties.getAmap().getKey())) {
            return WeatherResult.failure(providerName, "lixin.capability.weather.amap.key is required");
        }

        WeatherQuery effectiveQuery = normalizeQuery(query, providerName);
        String cacheKey = cacheKey(providerName, effectiveQuery.getCityCode());
        WeatherResult cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        WeatherProvider provider = providers.get(providerName);
        if (provider == null) {
            return WeatherResult.failure(providerName, "weather provider is not available: " + providerName);
        }

        try {
            WeatherResult result = provider.queryCurrent(effectiveQuery);
            if (result != null && result.isSuccess()) {
                cache.put(cacheKey, result, cacheMillis(providerName));
            }
            return result == null
                    ? WeatherResult.failure(providerName, "weather provider returned null result")
                    : result;
        } catch (WeatherException e) {
            return WeatherResult.failure(providerName, e.getMessage());
        } catch (RuntimeException e) {
            return WeatherResult.failure(providerName, "weather provider failed");
        }
    }

    private WeatherQuery normalizeQuery(WeatherQuery query, String providerName) {
        WeatherQuery normalized = new WeatherQuery();
        if (query != null) {
            normalized.setCityCode(query.getCityCode());
            normalized.setCityName(query.getCityName());
        }
        normalized.setProvider(providerName);
        if (!hasText(normalized.getCityCode()) && PROVIDER_AMAP.equals(providerName)) {
            normalized.setCityCode(properties.getAmap().getCityCode());
        }
        if (!hasText(normalized.getCityCode())) {
            throw new WeatherInvalidRequestException("weather cityCode must not be empty");
        }
        return normalized;
    }

    private String resolveProvider(WeatherQuery query) {
        String provider = query == null ? null : query.getProvider();
        if (!hasText(provider) && properties != null) {
            provider = properties.getProvider();
        }
        if (!hasText(provider)) {
            provider = AmapWeatherProvider.PROVIDER;
        }
        return provider.toLowerCase(Locale.ROOT);
    }

    private long cacheMillis(String providerName) {
        int cacheMinutes = 0;
        if (PROVIDER_AMAP.equals(providerName) && properties.getAmap() != null) {
            cacheMinutes = properties.getAmap().getCacheMinutes();
        }
        if (cacheMinutes <= 0) {
            return 0L;
        }
        return cacheMinutes * 60L * 1000L;
    }

    private String cacheKey(String providerName, String cityCode) {
        return providerName + ":" + cityCode;
    }

    private Map<String, WeatherProvider> toProviderMap(List<WeatherProvider> providers) {
        Map<String, WeatherProvider> map = new HashMap<>();
        if (providers == null) {
            return map;
        }
        for (WeatherProvider provider : providers) {
            if (provider != null && hasText(provider.provider())) {
                map.put(provider.provider().toLowerCase(Locale.ROOT), provider);
            }
        }
        return map;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
