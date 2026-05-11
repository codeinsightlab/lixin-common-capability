package com.lixin.capability.weather.dto;

public class WeatherQuery {
    private String cityCode;
    private String cityName;
    private String provider;

    public String getCityCode() { return cityCode; }
    public void setCityCode(String cityCode) { this.cityCode = cityCode; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
}
