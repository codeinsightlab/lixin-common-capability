package com.lixin.capability.weather.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lixin.capability.weather")
public class WeatherProperties {
    private boolean enabled = false;
    private String provider = "amap";
    private Amap amap = new Amap();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Amap getAmap() { return amap; }
    public void setAmap(Amap amap) { this.amap = amap; }

    public static class Amap {
        private String key;
        private String cityCode = "330100";
        private String extensions = "base";
        private int cacheMinutes = 10;
        private int timeoutMillis = 5000;
        private String baseUrl = "https://restapi.amap.com/v3/weather/weatherInfo";

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getCityCode() { return cityCode; }
        public void setCityCode(String cityCode) { this.cityCode = cityCode; }
        public String getExtensions() { return extensions; }
        public void setExtensions(String extensions) { this.extensions = extensions; }
        public int getCacheMinutes() { return cacheMinutes; }
        public void setCacheMinutes(int cacheMinutes) { this.cacheMinutes = cacheMinutes; }
        public int getTimeoutMillis() { return timeoutMillis; }
        public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
