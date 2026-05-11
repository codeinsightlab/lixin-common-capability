package com.lixin.capability.weather.provider;

import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.exception.WeatherConfigException;
import com.lixin.capability.weather.exception.WeatherInvalidRequestException;
import com.lixin.capability.weather.parser.AmapWeatherParser;
import com.lixin.capability.weather.properties.WeatherProperties;

import java.util.LinkedHashMap;
import java.util.Map;

public class AmapWeatherProvider implements WeatherProvider {
    public static final String PROVIDER = "amap";

    private final WeatherProperties properties;
    private final AmapWeatherHttpTransport httpTransport;
    private final AmapWeatherParser parser;

    public AmapWeatherProvider(WeatherProperties properties,
                               AmapWeatherHttpTransport httpTransport,
                               AmapWeatherParser parser) {
        this.properties = properties;
        this.httpTransport = httpTransport;
        this.parser = parser;
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public WeatherResult queryCurrent(WeatherQuery query) {
        validate(query);
        Map<String, String> params = new LinkedHashMap<>();
        params.put("key", properties.getAmap().getKey());
        params.put("city", query.getCityCode());
        params.put("extensions", properties.getAmap().getExtensions());
        params.put("output", "JSON");
        return parser.parseCurrentWeather(httpTransport.get(
                properties.getAmap().getBaseUrl(),
                params,
                properties.getAmap().getTimeoutMillis()));
    }

    private void validate(WeatherQuery query) {
        if (properties == null || properties.getAmap() == null) {
            throw new WeatherConfigException("lixin.capability.weather.amap properties must not be null");
        }
        if (!hasText(properties.getAmap().getKey())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.key is required when weather uses amap");
        }
        if (!hasText(properties.getAmap().getBaseUrl())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.base-url is required when weather uses amap");
        }
        if (properties.getAmap().getTimeoutMillis() <= 0) {
            throw new WeatherConfigException("lixin.capability.weather.amap.timeout-millis must be greater than 0");
        }
        if (!hasText(properties.getAmap().getExtensions())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.extensions is required when weather uses amap");
        }
        if (query == null || !hasText(query.getCityCode())) {
            throw new WeatherInvalidRequestException("weather cityCode must not be empty");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
