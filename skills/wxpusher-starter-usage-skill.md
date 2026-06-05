# WxPusher Starter Usage Skill

Use this skill when an external AI / Codex / GPT needs to integrate the `lixin-common-capability-wxpusher-spring-boot-starter`.

## Source Boundary

- Do not scan transfer, order, todo, Mini Program, or other business projects before using this starter.
- Use the README and this Skill as the integration contract.
- This starter is a generic WxPusher standard message sending gateway. It is not a business notification workflow, binding system, or state machine.
- WxPusher is notification enhancement only. Business state must be guaranteed by the business system's own todo, transfer, receive, order, or workflow state.

## Maven

Use the WxPusher starter when only WxPusher standard message sending capability is needed:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-wxpusher-spring-boot-starter</artifactId>
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

The all-starter currently aggregates WeChat, OSS, Netease IM, Weather, and WxPusher starters.

## Auto Configuration

The WxPusher starter supports Spring Boot 3/4 auto configuration through:

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

It also keeps `META-INF/spring.factories` for Spring Boot 2.x compatibility. Do not add a long-term business-project `@Import` bridge for `LixinWxPusherAutoConfiguration`.

Auto configuration creates `WxPusherService` only when:

- `WxPusherService` is on the classpath.
- `lixin.capability.wxpusher.enabled=true`.
- No user-defined `WxPusherService` bean already exists.

## Configuration

Prefix: `lixin.capability.wxpusher`

```yaml
lixin:
  capability:
    wxpusher:
      enabled: true
      app-token: ${WXPUSHER_APP_TOKEN:}
      base-url: https://wxpusher.zjiecode.com/api
      connect-timeout-ms: 3000
      read-timeout-ms: 5000
```

Rules:

- `enabled` defaults to `false`.
- `app-token` must come from backend configuration and must not be hardcoded.
- Do not log `app-token`.
- `base-url` defaults to the official API base URL but is retained for compatibility only in SDK mode.
- `connect-timeout-ms` and `read-timeout-ms` are retained for compatibility only in SDK mode.
- The starter delegates sending to official SDK `com.smjcco.wxpusher:client-sdk:3.0.2`.
- The official SDK fixes its own base URL and timeout internally, so `base-url`, `connect-timeout-ms`, and `read-timeout-ms` are not applied by the starter while SDK mode is used.
- The starter sends to `/send/message` through the official SDK.
- Disabled capability returns `success=false` and skips third-party HTTP calls.
- Missing `app-token` returns `success=false` and skips third-party HTTP calls.

## Client

Inject:

```java
import com.lixin.capability.wxpusher.service.WxPusherService;

private final WxPusherService wxPusherService;
```

Do not import or call official SDK types such as `com.smjcco.wxpusher.client.sdk.WxPusher`, `Message`, `Result`, or `MessageResult` from business modules. Official SDK types are internal implementation details of this starter.

Methods:

- `WxPusherSendResult sendToUid(String uid, String title, String content, Integer contentType, String url)`
- `WxPusherSendResult sendToUids(List<String> uids, String title, String content, Integer contentType, String url)`
- `WxPusherSendResult sendToTopic(Integer topicId, String title, String content, Integer contentType, String url)`

## Send Example

```java
import com.lixin.capability.wxpusher.dto.WxPusherSendResult;

WxPusherSendResult result = wxPusherService.sendToUid(
        "UID_xxx",
        "待办提醒",
        "你有一条新的待办，请及时处理。",
        1,
        "https://example.com/todo");

if (!result.isSuccess()) {
    // Record or display notification failure if needed.
    // Do not roll back the main business transaction only because WxPusher failed.
}
```

## Official Field Mapping

The starter uses official SDK `com.smjcco.wxpusher:client-sdk:3.0.2` for the standard send-message API:

- Request URL: `/send/message`
- `appToken`: from `lixin.capability.wxpusher.app-token`
- `content`: from method argument `content`
- `summary`: from method argument `title`
- `contentType`: from method argument `contentType`; defaults to `1` when omitted
- `uids`: from `sendToUid` / `sendToUids`
- `topicIds`: from `sendToTopic`
- `url`: from method argument `url`

Content type values:

- `1`: text
- `2`: HTML
- `3`: Markdown

Applied official limits:

- `content` must not exceed `40000` characters.
- `summary` must not exceed `100` characters.
- `url` must not exceed `1000` characters.
- A single request must not exceed `2000` UIDs.
- A single request must not exceed `5` topic IDs.

## Result

`WxPusherSendResult` contains:

- `success`
- `message`
- `providerCode`
- `providerMessage`
- `messageContentId`
- `sendRecordIds`
- `rawResponse`

`messageContentId` maps to the official message content id when the underlying provider exposes it. `sendRecordIds` map to official send record ids when the underlying provider exposes them. In current official SDK v3.0.2 mode, SDK `MessageResult` exposes deprecated `messageId` but does not expose newer `messageContentId` or `sendRecordId`, so these fields may be empty.

## Error Handling

- Third-party HTTP, parse, API, network, and unexpected runtime errors are swallowed by `WxPusherService` and returned as `success=false`.
- The starter must not throw third-party failures into the business main transaction path.
- The SDK provider layer may throw WxPusher capability exceptions internally, but the public `WxPusherService` methods convert them to `WxPusherSendResult`.
- Third-party raw response is available in the result and should only be logged at debug level by the starter.
- Send success logs only a short target type and target count.
- Send failure logs only a warning summary and never logs `app-token`.
- Do not report fake success when WxPusher returns a non-`1000` code, non-success item, null response, empty response, parse failure, or network failure.

## Unsupported In V1

WxPusher V1 does not support:

- Simple push token (SPT) sending
- UID binding table
- User follow or binding workflow
- Parameter QR code creation
- Querying scanned UID
- Callback handling
- Send status query
- Message deletion
- Paid subscription authorization query
- Message queue
- Retry center
- Async executor
- Business Controller default implementation
- Transfer order, order, todo, receive state, or workflow state transition
- Using WxPusher delivery result as business state transition basis

## Business Boundary

Business projects decide which UID or Topic to send to, when to send, how to store UID bindings if needed, how to expose any Controller, and how to handle notification failure. The starter only owns WxPusher request building, official SDK invocation, response parsing, result mapping, configuration, auto configuration, and notification capability exception boundaries.
