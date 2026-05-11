package com.lixin.capability.weather.exception;

public class WeatherParseException extends WeatherException {
    public WeatherParseException(String message) {
        super(message);
    }

    public WeatherParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
