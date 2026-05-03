package com.lixin.capability.netease.im.provider.netease;

import com.lixin.capability.netease.im.exception.NeteaseImApiException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JdkNeteaseImHttpTransport implements NeteaseImHttpTransport {
    @Override
    public NeteaseImHttpResponse postForm(String url, Map<String, String> headers, Map<String, String> form, int timeoutMillis) {
        HttpURLConnection connection = null;
        try {
            byte[] body = encodeForm(form).getBytes(StandardCharsets.UTF_8);
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setDoOutput(true);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            connection.setRequestProperty("Content-Length", String.valueOf(body.length));
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(body);
            }

            int statusCode = connection.getResponseCode();
            InputStream inputStream = statusCode >= 200 && statusCode < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String responseBody = inputStream == null ? "" : readToString(inputStream);
            return new NeteaseImHttpResponse(statusCode, responseBody, connection.getHeaderFields());
        } catch (Exception e) {
            throw new NeteaseImApiException("Netease IM HTTP request failed", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String encodeForm(Map<String, String> form) throws Exception {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : form.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
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
