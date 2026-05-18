# WeChat Mini Program Code Capability

Updated: 2026-05-18

This note records the implementation review and usage contract for Mini Program unlimited code generation in `lixin-common-capability-wechat-spring-boot-starter`.

## Preflight Review

- Read project `AGENTS.md` and confirmed the starter boundary: only third-party SDK/API initialization, request validation, response parsing, exception classification, auto-configuration, tests, README, and Skill updates belong here.
- Read `README.md` and `skills/wechat-starter-usage-skill.md`; both are external usage contracts and must stay synchronized with source changes.
- Reviewed existing WeChat miniapp surface: `WechatMiniappClient` already owns `code2Session`, `getPhoneNumber`, and `getAccessToken` through `WxMaService`.
- Chosen implementation path: extend `WechatMiniappClient` with `createWxaCodeUnlimit`, reuse `WxMaService` and `wx-java-miniapp`, and avoid handwritten `getwxacodeunlimit` HTTP.

## Added Capability

`WechatMiniappClient#createWxaCodeUnlimit(WxaCodeUnlimitRequest request)`

Request fields:

- `scene`: required, not blank, max 32 characters.
- `page`: required, must not start with `/`, must not contain query or fragment text.
- `checkPath`: optional, defaults to `true`.
- `envVersion`: optional, defaults to `release`; supported values are `release`, `trial`, and `develop`.
- `width`: optional, defaults to `430`; must be positive when provided.

Response fields:

- `bytes`: PNG image bytes.
- `contentType`: `image/png`.
- `base64`: Base64 encoded image bytes.
- `errorCode`, `errorMessage`, `rawErrorMessage`: reserved structured error fields for callers that need a response-like shape; runtime WeChat failures are still thrown as exceptions and are not converted to fake success.

## Error Handling

- Invalid request fields throw `WechatCapabilityInvalidRequestException`.
- WeChat SDK failures throw `WechatCapabilityApiException` and preserve WeChat error code plus raw error details when the SDK exposes them.
- Runtime SDK failures are wrapped as `WechatCapabilityApiException` with method, page, scene length, `envVersion`, and `checkPath`.
- Full access tokens, app secrets, and full business scene tokens are not logged or added to exception messages.

## Business Boundary

The starter does not generate invite tokens, store invite state, read or write databases, handle family/member/baby rules, or decide scanned-user join behavior. Business projects pass a ready `scene` and `page`, then handle their own API response shape and business flow.

## Verification

- `mvn -q -pl lixin-common-capability-wechat-spring-boot-starter -Dtest=DefaultWechatMiniappClientTest test`
- `mvn clean install`

Result:

- Targeted WeChat miniapp client test passed.
- Full root `mvn clean install` passed on 2026-05-18.
- Installed local version: `0.1.0-SNAPSHOT`.
- Installed starter artifact: `/Users/user/apache-maven-3.6.1/repository/com/lixin/lixin-common-capability-wechat-spring-boot-starter/0.1.0-SNAPSHOT/lixin-common-capability-wechat-spring-boot-starter-0.1.0-SNAPSHOT.jar`.
- No deploy and no tag were performed.
