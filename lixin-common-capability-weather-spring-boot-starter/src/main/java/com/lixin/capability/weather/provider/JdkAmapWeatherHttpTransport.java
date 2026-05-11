package com.lixin.capability.weather.provider;

import com.lixin.capability.weather.exception.WeatherApiException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JdkAmapWeatherHttpTransport implements AmapWeatherHttpTransport {
    @Override
    public String get(String url, Map<String, String> queryParams, int timeoutMillis) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolveUrl(url, queryParams)).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);

            int statusCode = connection.getResponseCode();
            InputStream inputStream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = inputStream == null ? "" : readToString(inputStream);
            if (statusCode < 200 || statusCode >= 300) {
                throw new WeatherApiException("Amap weather HTTP status " + statusCode, null);
            }
            return responseBody;
        } catch (WeatherApiException e) {
            throw e;
        } catch (Exception e) {
            throw new WeatherApiException("Amap weather HTTP request failed", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String resolveUrl(String url, Map<String, String> queryParams) throws Exception {
        StringBuilder builder = new StringBuilder(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            builder.append(url.contains("?") ? '&' : '?');
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    builder.append('&');
                }
                first = false;
                builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        return builder.toString();
    }

    private String readToString(InputStream inputStream) throws Exception {
        try (InputStream in = inputStream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
