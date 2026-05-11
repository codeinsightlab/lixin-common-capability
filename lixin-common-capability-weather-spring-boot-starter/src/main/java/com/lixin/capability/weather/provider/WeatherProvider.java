package com.lixin.capability.weather.provider;

import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;

public interface WeatherProvider {
    String provider();

    WeatherResult queryCurrent(WeatherQuery query);
}
