package com.lixin.capability.weather.autoconfigure;

import com.lixin.capability.weather.cache.LocalWeatherCache;
import com.lixin.capability.weather.cache.WeatherCache;
import com.lixin.capability.weather.exception.WeatherConfigException;
import com.lixin.capability.weather.parser.AmapWeatherParser;
import com.lixin.capability.weather.properties.WeatherProperties;
import com.lixin.capability.weather.provider.AmapWeatherHttpTransport;
import com.lixin.capability.weather.provider.AmapWeatherProvider;
import com.lixin.capability.weather.provider.JdkAmapWeatherHttpTransport;
import com.lixin.capability.weather.provider.WeatherProvider;
import com.lixin.capability.weather.service.DefaultWeatherService;
import com.lixin.capability.weather.service.WeatherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(WeatherProperties.class)
@ConditionalOnProperty(prefix = "lixin.capability.weather", name = "enabled", havingValue = "true")
public class LixinWeatherAutoConfiguration {
    private static final String PROVIDER_AMAP = "amap";

    @Bean
    @ConditionalOnMissingBean
    public AmapWeatherParser amapWeatherParser() {
        return new AmapWeatherParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public AmapWeatherHttpTransport amapWeatherHttpTransport() {
        return new JdkAmapWeatherHttpTransport();
    }

    @Bean
    @ConditionalOnMissingBean
    public WeatherCache weatherCache() {
        return new LocalWeatherCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public AmapWeatherProvider amapWeatherProvider(WeatherProperties properties,
                                                   AmapWeatherHttpTransport httpTransport,
                                                   AmapWeatherParser parser) {
        validateProperties(properties);
        return new AmapWeatherProvider(properties, httpTransport, parser);
    }

    @Bean
    @ConditionalOnMissingBean(WeatherService.class)
    public WeatherService weatherService(WeatherProperties properties,
                                         List<WeatherProvider> providers,
                                         WeatherCache weatherCache) {
        validateProperties(properties);
        return new DefaultWeatherService(properties, providers, weatherCache);
    }

    private void validateProperties(WeatherProperties properties) {
        if (properties == null || properties.getAmap() == null) {
            throw new WeatherConfigException("Weather properties must not be null");
        }
        if (!hasText(properties.getProvider())) {
            throw new WeatherConfigException("lixin.capability.weather.provider is required when weather is enabled");
        }
        if (!PROVIDER_AMAP.equalsIgnoreCase(properties.getProvider())) {
            throw new WeatherConfigException("Weather V1 only supports provider amap");
        }
        if (!hasText(properties.getAmap().getKey())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.key is required when weather is enabled");
        }
        if (!hasText(properties.getAmap().getCityCode())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.city-code is required when weather is enabled");
        }
        if (!hasText(properties.getAmap().getExtensions())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.extensions is required when weather is enabled");
        }
        if (!"base".equalsIgnoreCase(properties.getAmap().getExtensions())) {
            throw new WeatherConfigException("Weather V1 only supports amap.extensions=base");
        }
        if (properties.getAmap().getCacheMinutes() < 0) {
            throw new WeatherConfigException("lixin.capability.weather.amap.cache-minutes must not be negative");
        }
        if (properties.getAmap().getTimeoutMillis() <= 0) {
            throw new WeatherConfigException("lixin.capability.weather.amap.timeout-millis must be greater than 0");
        }
        if (!hasText(properties.getAmap().getBaseUrl())) {
            throw new WeatherConfigException("lixin.capability.weather.amap.base-url is required when weather is enabled");
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
