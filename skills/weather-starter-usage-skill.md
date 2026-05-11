# Weather Starter Usage Skill

Use this skill when an external AI / Codex / GPT needs to integrate the `lixin-common-capability-weather-spring-boot-starter`.

## Source Boundary

- Do not scan MES dashboard or frontend projects before using this starter.
- Use the README and this Skill as the integration contract.
- This starter is a generic current weather query gateway. It is not a dashboard business system.

## Maven

Use the Weather starter when only weather capability is needed:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-weather-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use the all-starter only when the project wants all current starters:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-all-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The all-starter currently aggregates WeChat, OSS, Netease IM, and Weather starters.

## Configuration

Prefix: `lixin.capability.weather`

```yaml
lixin:
  capability:
    weather:
      enabled: true
      provider: amap
      amap:
        key: ${AMAP_WEATHER_KEY:}
        city-code: 330100
        extensions: base
        cache-minutes: 10
        timeout-millis: 5000
```

Rules:

- `enabled` defaults to `false`.
- V1 only supports `provider: amap`.
- V1 only supports Amap current weather with `extensions: base`.
- `amap.key` must come from configuration, normally `${AMAP_WEATHER_KEY:}`.
- Do not hardcode the Amap API key in source code or tests.
- `amap.city-code` defaults to Hangzhou `330100` and can be overridden by `WeatherQuery.cityCode`.
- `amap.cache-minutes` defaults to `10`; cache key is `provider + cityCode`.
- The cache is local in-memory cache. Do not assume Redis or multi-node cache coherence.
- Do not add dashboard, MES, frontend, user, order, or business-state fields to Weather configuration.

## Client

Inject:

```java
import com.lixin.capability.weather.service.WeatherService;

private final WeatherService weatherService;
```

Method:

- `WeatherResult getCurrentWeather(WeatherQuery query)`

## Current Weather

```java
import com.lixin.capability.weather.dto.WeatherQuery;
import com.lixin.capability.weather.dto.WeatherResult;

WeatherQuery query = new WeatherQuery();
query.setCityCode("330100");

WeatherResult result = weatherService.getCurrentWeather(query);
```

`WeatherResult` contains normalized fields:

- `city`
- `province`
- `weather`
- `temperature`
- `humidity`
- `windDirection`
- `windPower`
- `reportTime`
- `provider`
- `success`
- `message`

The starter does not expose Amap raw JSON and does not return `AjaxResult`.

## Unsupported In V1

Weather V1 does not support:

- Auto location
- Geocode or reverse geocode
- Multi-city management
- Weather forecast
- Hourly weather forecast
- Multi-provider routing beyond Amap
- MES `/bigdatadashboard/weather`
- MES dashboard code changes
- Frontend integration
- Business Controller default implementation

## Error Handling

- Startup configuration errors throw `WeatherConfigException`.
- Invalid input can throw `WeatherInvalidRequestException`.
- Amap HTTP failures throw `WeatherApiException`.
- Amap response parse or protocol failures throw `WeatherParseException`.
- `WeatherService.getCurrentWeather` returns `success=false` for disabled capability, missing Amap key, unsupported provider, unavailable provider, or provider exceptions.
- A failed provider response must not be cached as a successful result.
- Do not swallow exceptions and then report fake success.

## Business Boundary

Business projects decide which city code to query, how to expose the result through their own Controller, how to map it into dashboard DTOs, and how to display failures. The starter only owns provider invocation, Amap response parsing, normalized `WeatherResult` mapping, local cache, and weather capability exception boundaries.
