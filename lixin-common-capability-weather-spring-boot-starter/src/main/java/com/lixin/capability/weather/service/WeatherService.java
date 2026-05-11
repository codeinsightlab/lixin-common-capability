package com.lixin.capability.weather.service;

import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;

public interface WeatherService {
    WeatherResult getCurrentWeather(WeatherQuery query);
}
