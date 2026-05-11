package com.lixin.capability.weather.dto;

public class WeatherResult {
    private String city;
    private String province;
    private String weather;
    private String temperature;
    private String humidity;
    private String windDirection;
    private String windPower;
    private String reportTime;
    private String provider;
    private boolean success;
    private String message;

    public static WeatherResult failure(String provider, String message) {
        WeatherResult result = new WeatherResult();
        result.setProvider(provider);
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }
    public String getTemperature() { return temperature; }
    public void setTemperature(String temperature) { this.temperature = temperature; }
    public String getHumidity() { return humidity; }
    public void setHumidity(String humidity) { this.humidity = humidity; }
    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }
    public String getWindPower() { return windPower; }
    public void setWindPower(String windPower) { this.windPower = windPower; }
    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
