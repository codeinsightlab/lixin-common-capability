package com.lixin.capability.weather.provider;

import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.exception.WeatherConfigException;
import com.lixin.capability.weather.parser.AmapWeatherParser;
import com.lixin.capability.weather.properties.WeatherProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmapWeatherProviderTest {
    private WeatherProperties properties;
    private RecordingHttpTransport transport;
    private AmapWeatherProvider provider;

    @BeforeEach
    void setUp() {
        properties = new WeatherProperties();
        properties.getAmap().setKey("test-key");
        properties.getAmap().setBaseUrl("https://example.com/weather");
        properties.getAmap().setTimeoutMillis(3000);
        transport = new RecordingHttpTransport();
        provider = new AmapWeatherProvider(properties, transport, new AmapWeatherParser());
    }

    @Test
    void sendsExpectedAmapQueryParametersAndParsesResult() {
        WeatherQuery query = new WeatherQuery();
        query.setCityCode("330100");

        WeatherResult result = provider.queryCurrent(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProvider()).isEqualTo("amap");
        assertThat(transport.url).isEqualTo("https://example.com/weather");
        assertThat(transport.timeoutMillis).isEqualTo(3000);
        assertThat(transport.queryParams)
                .containsEntry("key", "test-key")
                .containsEntry("city", "330100")
                .containsEntry("extensions", "base")
                .containsEntry("output", "JSON");
    }

    @Test
    void blankAmapKeyFailsBeforeHttpCall() {
        properties.getAmap().setKey(" ");
        WeatherQuery query = new WeatherQuery();
        query.setCityCode("330100");

        assertThatThrownBy(() -> provider.queryCurrent(query))
                .isInstanceOf(WeatherConfigException.class)
                .hasMessageContaining("amap.key");
        assertThat(transport.callCount).isZero();
    }

    private static class RecordingHttpTransport implements AmapWeatherHttpTransport {
        private String url;
        private Map<String, String> queryParams;
        private int timeoutMillis;
        private int callCount;

        @Override
        public String get(String url, Map<String, String> queryParams, int timeoutMillis) {
            callCount++;
            this.url = url;
            this.queryParams = queryParams;
            this.timeoutMillis = timeoutMillis;
            return "{"
                    + "\"status\":\"1\","
                    + "\"info\":\"OK\","
                    + "\"lives\":[{"
                    + "\"province\":\"浙江\","
                    + "\"city\":\"杭州市\","
                    + "\"weather\":\"晴\","
                    + "\"temperature\":\"28\","
                    + "\"winddirection\":\"东\","
                    + "\"windpower\":\"≤3\","
                    + "\"humidity\":\"55\","
                    + "\"reporttime\":\"2026-05-11 11:00:00\""
                    + "}]}";
        }
    }
}
