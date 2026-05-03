# Netease IM Starter Usage Skill

Use this skill when an external AI / Codex / GPT needs to integrate the `lixin-common-capability-netease-im-spring-boot-starter`.

## Source Boundary

- Do not scan business repositories to infer IM business boundaries.
- Use the README and this Skill as the integration contract.
- This starter is a Netease IM account gateway plus callback signature verifier. It is not an IM business system.
- Do not copy old RuoYi IM code into a business project or into this starter.
- Do not mix SDK v1, SDK v2, and HTTP PATCH patterns. V1 uses one HTTP provider boundary.

## Maven

Use the Netease IM starter when only IM account gateway and callback verification are needed:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-netease-im-spring-boot-starter</artifactId>
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

The all-starter currently aggregates WeChat, OSS, and Netease IM.

## Configuration

Prefix: `lixin.capability.netease.im`

```yaml
lixin:
  capability:
    netease:
      im:
        enabled: true
        app-key: ${NETEASE_IM_APP_KEY}
        app-secret: ${NETEASE_IM_APP_SECRET}
        timeout-millis: 10000
        base-url: https://api.yunxinapi.com/nimserver
```

Rules:

- `enabled` defaults to `false`.
- When `enabled=true`, `app-key` and `app-secret` are required.
- `timeout-millis` defaults to `10000`.
- `base-url` defaults to `https://api.yunxinapi.com/nimserver`.
- `app-secret` is also used for callback signature verification.
- Do not add business fields such as `userId`, `sysUser`, `tokenStorage`, `role`, `order`, or `conversation`.

## Clients

Inject:

```java
import com.lixin.capability.netease.im.callback.NeteaseImCallbackVerifier;
import com.lixin.capability.netease.im.client.NeteaseImAccountClient;

private final NeteaseImAccountClient accountClient;
private final NeteaseImCallbackVerifier callbackVerifier;
```

Methods:

- `CreateImAccountResponse createAccount(CreateImAccountRequest request)`
- `UpdateImAccountResponse updateAccountProfile(UpdateImAccountRequest request)`
- `RefreshImTokenResponse refreshToken(RefreshImTokenRequest request)`
- `VerifyImCallbackResponse verify(VerifyImCallbackRequest request)`

## Create Account

```java
CreateImAccountRequest request = new CreateImAccountRequest();
request.setAccountId(accountId);
request.setName(name);
request.setAvatar(avatarUrl);
request.setExtensionJson("{\"k\":\"v\"}");

CreateImAccountResponse response = accountClient.createAccount(request);
String token = response.getToken();
```

`accountId` is provided by the business project. The starter does not know whether it equals a business user ID. A successful create response must contain `token`; otherwise the starter throws `NeteaseImParseException`.

## Update Profile

```java
UpdateImAccountRequest request = new UpdateImAccountRequest();
request.setAccountId(accountId);
request.setName(name);
request.setAvatar(avatarUrl);
request.setExtensionJson("{\"k\":\"v\"}");

UpdateImAccountResponse response = accountClient.updateAccountProfile(request);
```

At least one of `name`, `avatar`, or `extensionJson` must be non-empty.

## Refresh Token

```java
RefreshImTokenRequest request = new RefreshImTokenRequest();
request.setAccountId(accountId);

RefreshImTokenResponse response = accountClient.refreshToken(request);
String newToken = response.getToken();
```

The starter only returns the token. Business code decides whether and where to persist it.

## Callback Verify

```java
VerifyImCallbackRequest request = new VerifyImCallbackRequest();
request.setAppKey(appKeyFromHeader);
request.setCurTime(curTimeFromHeader);
request.setBodyMd5(bodyMd5FromHeader);
request.setCheckSum(checkSumFromHeader);
request.setBody(rawBody);

VerifyImCallbackResponse response = callbackVerifier.verify(request);
if (!response.isVerified()) {
    // Business controller decides its own response.
}
```

`bodyMd5` is the MD5 of the callback body. Do not call it `nonce`. Signature mismatch returns `verified=false`; missing callback parameters throw `NeteaseImInvalidRequestException`.

## Business Boundary

Business projects own:

- user registration and login
- `SysUser`, `LoginUser`, and business user binding
- token storage
- conversation lists
- message records
- read or unread state
- conversation deletion
- historical chat synchronization
- event persistence
- event dispatch
- order chat relation
- controller response policy
- business idempotency

Do not put those responsibilities into this starter.

## Not Supported

Netease IM V1 does not support:

- SDK DTO exposure
- SDK v1 / SDK v2 / HTTP PATCH mixed implementation
- session or conversation APIs
- message APIs
- event storage or event dispatch
- order, member, wallet, user, or ledger state
- `conversationId = from + "|1|" + to` business rules
- writing `sys_user.token`
- default business controllers

## Exception Rules

- Configuration errors: `NeteaseImConfigException`
- Invalid request: `NeteaseImInvalidRequestException`
- Netease IM HTTP failures or provider failure codes: `NeteaseImApiException`
- Empty response, missing `accountId`, missing required `token`, or parse failure: `NeteaseImParseException`
- Callback algorithm failure: `NeteaseImCallbackVerifyException`

Forbidden:

- Do not return `null` to hide failures.
- Do not fabricate `rawResponse`.
- Do not wrap missing token as success.
- Do not catch and only log.
- Do not persist tokens in the starter.
- Do not copy `SysUser`, `LoginUser`, `SecurityUtils`, Mapper, or Controller logic into the starter.
