package com.lixin.capability.weather.parser;

import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.exception.WeatherParseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmapWeatherParserTest {
    private final AmapWeatherParser parser = new AmapWeatherParser();

    @Test
    void parsesSuccessJson() {
        String json = "{"
                + "\"status\":\"1\","
                + "\"info\":\"OK\","
                + "\"lives\":[{"
                + "\"province\":\"浙江\","
                + "\"city\":\"杭州市\","
                + "\"weather\":\"多云\","
                + "\"temperature\":\"26\","
                + "\"winddirection\":\"东\","
                + "\"windpower\":\"≤3\","
                + "\"humidity\":\"60\","
                + "\"reporttime\":\"2026-05-11 10:00:00\""
                + "}]}";

        WeatherResult result = parser.parseCurrentWeather(json);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProvider()).isEqualTo("amap");
        assertThat(result.getProvince()).isEqualTo("浙江");
        assertThat(result.getCity()).isEqualTo("杭州市");
        assertThat(result.getWeather()).isEqualTo("多云");
        assertThat(result.getTemperature()).isEqualTo("26");
        assertThat(result.getWindDirection()).isEqualTo("东");
        assertThat(result.getWindPower()).isEqualTo("≤3");
        assertThat(result.getHumidity()).isEqualTo("60");
        assertThat(result.getReportTime()).isEqualTo("2026-05-11 10:00:00");
    }

    @Test
    void rejectsFailureStatusJson() {
        String json = "{\"status\":\"0\",\"info\":\"INVALID_USER_KEY\",\"lives\":[]}";

        assertThatThrownBy(() -> parser.parseCurrentWeather(json))
                .isInstanceOf(WeatherParseException.class)
                .hasMessageContaining("INVALID_USER_KEY");
    }

    @Test
    void rejectsEmptyLives() {
        String json = "{\"status\":\"1\",\"info\":\"OK\",\"lives\":[]}";

        assertThatThrownBy(() -> parser.parseCurrentWeather(json))
                .isInstanceOf(WeatherParseException.class)
                .hasMessageContaining("missing lives");
    }

    @Test
    void missingFieldsDoNotProduceNullDirtyValues() {
        String json = "{"
                + "\"status\":\"1\","
                + "\"info\":\"OK\","
                + "\"lives\":[{"
                + "\"province\":\"浙江\","
                + "\"city\":\"杭州市\","
                + "\"weather\":\"多云\""
                + "}]}";

        WeatherResult result = parser.parseCurrentWeather(json);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTemperature()).isEqualTo("");
        assertThat(result.getHumidity()).isEqualTo("");
        assertThat(result.getWindDirection()).isEqualTo("");
        assertThat(result.getWindPower()).isEqualTo("");
        assertThat(result.getReportTime()).isEqualTo("");
    }
}
