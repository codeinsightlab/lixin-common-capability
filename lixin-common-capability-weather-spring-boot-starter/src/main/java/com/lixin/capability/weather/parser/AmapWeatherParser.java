package com.lixin.capability.weather.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.exception.WeatherParseException;

public class AmapWeatherParser {
    private static final String PROVIDER_AMAP = "amap";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResult parseCurrentWeather(String json) {
        if (!hasText(json)) {
            throw new WeatherParseException("Amap weather response body is empty");
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            String status = text(root, "status");
            if (!"1".equals(status)) {
                String info = text(root, "info");
                throw new WeatherParseException("Amap weather API returned failure: " + defaultText(info, "unknown"));
            }
            JsonNode lives = root.get("lives");
            if (lives == null || !lives.isArray() || lives.size() == 0) {
                throw new WeatherParseException("Amap weather response missing lives");
            }

            JsonNode live = lives.get(0);
            WeatherResult result = new WeatherResult();
            result.setProvince(clean(text(live, "province")));
            result.setCity(clean(text(live, "city")));
            result.setWeather(clean(text(live, "weather")));
            result.setTemperature(clean(text(live, "temperature")));
            result.setWindDirection(clean(text(live, "winddirection")));
            result.setWindPower(clean(text(live, "windpower")));
            result.setHumidity(clean(text(live, "humidity")));
            result.setReportTime(clean(text(live, "reporttime")));
            result.setProvider(PROVIDER_AMAP);
            result.setSuccess(true);
            return result;
        } catch (WeatherParseException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherParseException("Amap weather response parse failed", e);
        }
    }

    private String text(JsonNode node, String name) {
        if (node == null || node.get(name) == null || node.get(name).isNull()) {
            return null;
        }
        return node.get(name).asText();
    }

    private String clean(String value) {
        if (value == null || "null".equalsIgnoreCase(value) || "undefined".equalsIgnoreCase(value)) {
            return "";
        }
        return value;
    }

    private String defaultText(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
