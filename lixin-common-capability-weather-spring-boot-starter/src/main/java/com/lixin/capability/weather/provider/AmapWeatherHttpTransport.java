package com.lixin.capability.weather.provider;

import java.util.Map;

public interface AmapWeatherHttpTransport {
    String get(String url, Map<String, String> queryParams, int timeoutMillis);
}
