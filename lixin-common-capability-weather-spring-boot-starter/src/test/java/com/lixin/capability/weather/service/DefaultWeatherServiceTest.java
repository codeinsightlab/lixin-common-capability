package com.lixin.capability.weather.service;

import com.lixin.capability.weather.cache.LocalWeatherCache;
import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;
import com.lixin.capability.weather.properties.WeatherProperties;
import com.lixin.capability.weather.provider.WeatherProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultWeatherServiceTest {
    private WeatherProperties properties;
    private MutableClock clock;
    private RecordingWeatherProvider provider;
    private DefaultWeatherService service;

    @BeforeEach
    void setUp() {
        properties = new WeatherProperties();
        properties.setEnabled(true);
        properties.setProvider("amap");
        properties.getAmap().setKey("test-key");
        properties.getAmap().setCityCode("330100");
        properties.getAmap().setCacheMinutes(10);

        clock = new MutableClock(Instant.parse("2026-05-11T00:00:00Z"));
        provider = new RecordingWeatherProvider();
        service = new DefaultWeatherService(
                properties,
                Collections.<WeatherProvider>singletonList(provider),
                new LocalWeatherCache(clock));
    }

    @Test
    void cacheAvoidsRepeatedProviderCallForSameCityAndProvider() {
        WeatherQuery query = query("330100");

        WeatherResult first = service.getCurrentWeather(query);
        WeatherResult second = service.getCurrentWeather(query);

        assertThat(first.isSuccess()).isTrue();
        assertThat(second.getTemperature()).isEqualTo(first.getTemperature());
        assertThat(provider.callCount).isEqualTo(1);
    }

    @Test
    void cacheKeyIncludesProviderAndCityCode() {
        service.getCurrentWeather(query("330100"));
        service.getCurrentWeather(query("310000"));

        assertThat(provider.callCount).isEqualTo(2);
    }

    @Test
    void cacheExpiresAndProviderIsCalledAgain() {
        WeatherQuery query = query("330100");

        WeatherResult first = service.getCurrentWeather(query);
        clock.plusMillis(10L * 60L * 1000L + 1L);
        WeatherResult second = service.getCurrentWeather(query);

        assertThat(provider.callCount).isEqualTo(2);
        assertThat(second.getTemperature()).isNotEqualTo(first.getTemperature());
    }

    @Test
    void disabledCapabilityReturnsFailureAndDoesNotCallProvider() {
        properties.setEnabled(false);

        WeatherResult result = service.getCurrentWeather(query("330100"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("disabled");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void blankAmapKeyReturnsFailureAndDoesNotCallProvider() {
        properties.getAmap().setKey(" ");

        WeatherResult result = service.getCurrentWeather(query("330100"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("amap.key");
        assertThat(provider.callCount).isZero();
    }

    @Test
    void usesConfiguredCityCodeWhenQueryCityCodeIsBlank() {
        WeatherQuery query = new WeatherQuery();

        WeatherResult result = service.getCurrentWeather(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(provider.cityCode).isEqualTo("330100");
    }

    private WeatherQuery query(String cityCode) {
        WeatherQuery query = new WeatherQuery();
        query.setCityCode(cityCode);
        return query;
    }

    private static class RecordingWeatherProvider implements WeatherProvider {
        private int callCount;
        private String cityCode;

        @Override
        public String provider() {
            return "amap";
        }

        @Override
        public WeatherResult queryCurrent(WeatherQuery query) {
            callCount++;
            cityCode = query.getCityCode();
            WeatherResult result = new WeatherResult();
            result.setSuccess(true);
            result.setProvider("amap");
            result.setProvince("浙江");
            result.setCity("杭州市");
            result.setWeather("多云");
            result.setTemperature(String.valueOf(20 + callCount));
            result.setHumidity("60");
            result.setWindDirection("东");
            result.setWindPower("≤3");
            result.setReportTime("2026-05-11 10:00:00");
            return result;
        }
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void plusMillis(long millis) {
            instant = instant.plusMillis(millis);
        }
    }
}
