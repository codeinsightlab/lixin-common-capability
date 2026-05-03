# Skill: WeChat Starter Usage for common-capability v0.1.0

This Skill is for external AI tools such as Codex, GPT, or project agents that need to integrate WeChat capabilities into a business Spring Boot project.

When a business project needs WeChat Mini Program login, phone number parsing, subscribe messages, normal merchant JSAPI payment, refund requests, payment notify parsing, or refund notify parsing, use this starter first. Do not reimplement WeChat SDK initialization, WeChat Pay signature verification, notify decryption, response parsing, or exception classification in the business project.

Do not scan the starter source code as the default integration path. Use this Skill, the public starter dependency, the configuration prefix, and the exposed clients.

## Applicable Scenarios

Use this Skill when the business project needs:

- WeChat Mini Program `code2Session`
- WeChat Mini Program phone number parsing
- WeChat Mini Program `access_token` access
- Subscribe message sending
- WeChat Pay normal merchant JSAPI prepay
- WeChat Pay normal merchant refund request
- WeChat Pay payment notify parsing
- WeChat Pay refund notify parsing

## Maven Dependency Rules

Prefer importing only the WeChat capability starter:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-wechat-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

If the business project intentionally wants every capability starter provided by `common-capability`, import the all-starter:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-all-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Rules:

- Prefer the on-demand WeChat starter for business projects that only need WeChat.
- The all-starter is only an aggregation package.
- In `v0.1.0-wechat`, the all-starter currently aggregates only the WeChat starter.
- Do not import the old `lixin-common-capability-spring-boot-starter`.

## Configuration Rules

Use the prefix `lixin.capability.wechat`.

```yaml
lixin:
  capability:
    wechat:
      miniapp:
        enabled: true
        app-id: wx_xxx
        secret: xxx
        token: xxx
        aes-key: xxx
        storage:
          type: memory
          key-prefix: lixin:wechat:miniapp
      subscribe:
        enabled: true
        default-mini-program-state: formal
        default-lang: zh_CN
      pay:
        enabled: true
        app-id: wx_xxx
        mch-id: xxx
        private-key-path: /path/to/apiclient_key.pem
        merchant-serial-number: xxx
        api-v3-key: xxx
        notify-url: https://example.com/pay/notify
        refund-notify-url: https://example.com/pay/refund/notify
```

Configuration rules:

- V1 only supports `miniapp.storage.type=memory`.
- Do not write Redis storage examples.
- Do not write service provider or ecommerce/commerce pay configuration.
- Do not write `subMchId`, `spMchId`, or `subAppId`.
- Do not put order, member, wallet, role, transaction ledger, or other business fields into this starter configuration.

## Available Clients

### `WechatMiniappClient`

- `code2Session(Code2SessionRequest request)`
- `getPhoneNumber(PhoneNumberRequest request)`
- `getAccessToken()`

### `WechatSubscribeClient`

- `send(SubscribeMessageSendRequest request)`

### `WechatPayClient`

- `jsapiPrepay(JsapiPrepayRequest request)`
- `refund(RefundRequest request)`

### `WechatPayNotifyClient`

- `parsePaymentNotify(WechatNotifyRequest request)`
- `parseRefundNotify(WechatNotifyRequest request)`

## Call Templates

These examples are short integration templates for business projects. Adapt class names and persistence calls to the business project. Keep the starter as the WeChat boundary, and keep business state handling in the business project.

### Mini Program Login: `code2Session`

```java
import com.lixin.capability.wechat.miniapp.client.WechatMiniappClient;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;

public class WechatLoginService {
    private final WechatMiniappClient wechatMiniappClient;

    public WechatLoginService(WechatMiniappClient wechatMiniappClient) {
        this.wechatMiniappClient = wechatMiniappClient;
    }

    public LoginResult loginByCode(String code) {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode(code);

        Code2SessionResponse response = wechatMiniappClient.code2Session(request);
        String openId = response.getOpenId();
        String sessionKey = response.getSessionKey();

        // Business project responsibility:
        // register or find the user, bind openId, issue the login token, and store session state if needed.
        return buildLoginResult(openId, sessionKey);
    }
}
```

### Phone Number Parsing

```java
import com.lixin.capability.wechat.miniapp.client.WechatMiniappClient;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;

public class WechatPhoneService {
    private final WechatMiniappClient wechatMiniappClient;

    public WechatPhoneService(WechatMiniappClient wechatMiniappClient) {
        this.wechatMiniappClient = wechatMiniappClient;
    }

    public void bindPhone(String userId, String phoneCode) {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode(phoneCode);

        PhoneNumberResponse response = wechatMiniappClient.getPhoneNumber(request);
        String phoneNumber = response.getPhoneNumber();

        // Business project responsibility:
        // verify uniqueness, bind the phone number, and handle account conflict rules.
        bindPhoneNumber(userId, phoneNumber);
    }
}
```

### Subscribe Message Sending

```java
import com.lixin.capability.wechat.subscribe.client.WechatSubscribeClient;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageData;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;

import java.util.Arrays;

public class WechatSubscribeMessageService {
    private final WechatSubscribeClient wechatSubscribeClient;

    public WechatSubscribeMessageService(WechatSubscribeClient wechatSubscribeClient) {
        this.wechatSubscribeClient = wechatSubscribeClient;
    }

    public SubscribeMessageSendResponse sendNotice(String openId, String templateId) {
        SubscribeMessageData first = new SubscribeMessageData();
        first.setName("thing1");
        first.setValue("notification");

        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser(openId);
        request.setTemplateId(templateId);
        request.setPage("pages/index/index");
        request.setData(Arrays.asList(first));

        // Business project responsibility:
        // choose templateId, map template fields, decide send timing, and manage failure retry policy.
        return wechatSubscribeClient.send(request);
    }
}
```

### JSAPI Prepay

```java
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;

public class WechatPaymentService {
    private final WechatPayClient wechatPayClient;

    public WechatPaymentService(WechatPayClient wechatPayClient) {
        this.wechatPayClient = wechatPayClient;
    }

    public JsapiPrepayResponse createPrepay(String openId, String outTradeNo, int amountTotal) {
        JsapiPrepayRequest request = new JsapiPrepayRequest();
        request.setDescription("Product description");
        request.setOutTradeNo(outTradeNo);
        request.setAmountTotal(amountTotal);
        request.setCurrency("CNY");
        request.setPayerOpenId(openId);

        JsapiPrepayResponse response = wechatPayClient.jsapiPrepay(request);

        // Business project responsibility:
        // create and persist the order, calculate amount, own outTradeNo, bind openId, and track order state.
        // Return response to the frontend so it can invoke WeChat Pay.
        return response;
    }
}
```

### Refund Request

```java
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.dto.RefundRequest;
import com.lixin.capability.wechat.pay.dto.RefundResponse;

public class WechatRefundService {
    private final WechatPayClient wechatPayClient;

    public WechatRefundService(WechatPayClient wechatPayClient) {
        this.wechatPayClient = wechatPayClient;
    }

    public RefundResponse requestRefund(String outTradeNo, String outRefundNo, int refundAmount, int totalAmount) {
        RefundRequest request = new RefundRequest();
        request.setOutTradeNo(outTradeNo);
        request.setOutRefundNo(outRefundNo);
        request.setRefundAmount(refundAmount);
        request.setTotalAmount(totalAmount);
        request.setCurrency("CNY");
        request.setReason("Requested by user");

        // Business project responsibility:
        // validate refund permission, create refund record, and track refund state.
        return wechatPayClient.refund(request);
    }
}
```

### Payment Notify Parsing

```java
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.notify.PaymentNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public class WechatPaymentNotifyService {
    private final WechatPayNotifyClient wechatPayNotifyClient;

    public WechatPaymentNotifyService(WechatPayNotifyClient wechatPayNotifyClient) {
        this.wechatPayNotifyClient = wechatPayNotifyClient;
    }

    public void handlePaymentNotify(String serialNumber, String nonce, String timestamp,
                                    String signature, String body) {
        WechatNotifyRequest request = new WechatNotifyRequest();
        request.setSerialNumber(serialNumber);
        request.setNonce(nonce);
        request.setTimestamp(timestamp);
        request.setSignature(signature);
        request.setBody(body);

        PaymentNotifyResponse response = wechatPayNotifyClient.parsePaymentNotify(request);

        String outTradeNo = response.getOutTradeNo();
        String tradeState = response.getTradeState();
        String transactionId = response.getTransactionId();

        // Business project responsibility:
        // perform idempotency checks and update business state by outTradeNo, tradeState, and transactionId.
        // The starter does not provide a default Controller response and does not update business state.
        markPaymentState(outTradeNo, tradeState, transactionId);
    }
}
```

### Building WechatNotifyRequest from HttpServletRequest

`WechatNotifyRequest` is built by the business Controller. The starter does not directly accept `HttpServletRequest`; this keeps the starter independent from the Web layer and Servlet API. The starter only verifies the signature, decrypts the notification, and parses structured fields.

The Controller must pass the original WeChat Pay callback body. Do not pass formatted, rebuilt, or re-serialized JSON. Header names must remain the official WeChat Pay callback header names. If reading the body fails, fail explicitly in the business project; never return default success.

```java
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

public class WechatNotifyRequestFactory {
    public WechatNotifyRequest buildWechatNotifyRequest(HttpServletRequest request) throws IOException {
        WechatNotifyRequest notifyRequest = new WechatNotifyRequest();
        notifyRequest.setSerialNumber(request.getHeader("Wechatpay-Serial"));
        notifyRequest.setNonce(request.getHeader("Wechatpay-Nonce"));
        notifyRequest.setTimestamp(request.getHeader("Wechatpay-Timestamp"));
        notifyRequest.setSignature(request.getHeader("Wechatpay-Signature"));
        notifyRequest.setBody(readBody(request));
        return notifyRequest;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        return request.getReader()
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
```

Header mapping:

- `Wechatpay-Serial` -> `WechatNotifyRequest.serialNumber`
- `Wechatpay-Nonce` -> `WechatNotifyRequest.nonce`
- `Wechatpay-Timestamp` -> `WechatNotifyRequest.timestamp`
- `Wechatpay-Signature` -> `WechatNotifyRequest.signature`
- HTTP request raw body -> `WechatNotifyRequest.body`

If a business filter or interceptor has already read the body, use a repeatable-read request body wrapper in the business Web layer. That wrapper is a business project responsibility, not a starter responsibility.

### Refund Notify Parsing

```java
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.notify.RefundNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public class WechatRefundNotifyService {
    private final WechatPayNotifyClient wechatPayNotifyClient;

    public WechatRefundNotifyService(WechatPayNotifyClient wechatPayNotifyClient) {
        this.wechatPayNotifyClient = wechatPayNotifyClient;
    }

    public void handleRefundNotify(String serialNumber, String nonce, String timestamp,
                                   String signature, String body) {
        WechatNotifyRequest request = new WechatNotifyRequest();
        request.setSerialNumber(serialNumber);
        request.setNonce(nonce);
        request.setTimestamp(timestamp);
        request.setSignature(signature);
        request.setBody(body);

        RefundNotifyResponse response = wechatPayNotifyClient.parseRefundNotify(request);

        String outRefundNo = response.getOutRefundNo();
        String refundStatus = response.getRefundStatus();
        String refundId = response.getRefundId();

        // Business project responsibility:
        // perform idempotency checks and update refund state by outRefundNo, refundStatus, and refundId.
        markRefundState(outRefundNo, refundStatus, refundId);
    }
}
```

Build `WechatNotifyRequest` from the WeChat Pay HTTP headers and request body. The same `buildWechatNotifyRequest(HttpServletRequest request)` method can be reused for refund notify parsing. The starter verifies the signature, decrypts the resource, and parses structured fields.

## Business Boundary Hard Rules

The starter is responsible for:

- WeChat SDK initialization
- WeChat API calls
- WeChat Pay notify signature verification
- Notify decryption
- Response parsing
- Field validation
- Exception classification

The business project is responsible for:

- User registration and login
- Binding `openId` to the user
- Phone number binding
- Order creation
- Payment amount calculation
- Updating the order after payment success
- Updating the refund record after refund success
- Wallet, member, and transaction ledger handling
- Notify idempotency
- Controller responses to the WeChat platform

## Prohibited AI Actions

When using this starter in a business project, AI must not:

- Reinitialize the WeChat SDK
- Handwrite WeChat Pay notify signature verification or decryption
- Copy old RuoYi project WeChat Manager or Service code
- Put order state logic into the starter
- Make the starter write to the database
- Make the starter handle wallet, member, or transaction ledger logic
- Add service provider, ecommerce/commerce pay, `subMchId`, `spMchId`, or `subAppId`
- Swallow exceptions
- Return default success after a failure
- Silently fall back for critical fields
- Forge `rawResponse` or `rawPlaintext`

## Exception Handling Rules

- `WechatCapabilityConfigException`: configuration error, such as missing `appId`, `secret`, `mchId`, certificate path, `apiV3Key`, or SDK initialization failure.
- `WechatCapabilityInvalidRequestException`: caller input error, such as blank `code`, blank `outTradeNo`, invalid amount, or missing notify headers.
- `WechatCapabilityApiException`: WeChat SDK or API call failure.
- `WechatCapabilityNotifyVerifyException`: WeChat Pay payment or refund notify signature verification failure.
- `WechatCapabilityNotifyDecryptException`: notify decryption failure.
- `WechatCapabilityParseException`: missing required response fields, malformed notify plaintext, or protocol parsing failure.

Business guidance:

- Do not swallow these exceptions in the business layer.
- Controllers may log, alert, and return a failure response according to business and platform requirements.
- Never convert these exceptions into default success.

## `rawResponse` and `rawPlaintext` Semantics

- The WeChat Mini Program SDK subscribe message send API does not expose a response body on success, so `SubscribeMessageSendResponse.rawResponse=null` is normal.
- If the refund SDK does not expose the original response body, `RefundResponse.rawResponse=null` is normal.
- `rawPlaintext` comes from the WeChat Pay SDK result after signature verification and decryption. It is not a forged field.
- Business decisions should prefer structured fields such as `outTradeNo`, `tradeState`, `transactionId`, `outRefundNo`, `refundStatus`, and `refundId`.
- Do not depend on `rawPlaintext` for core business decisions.

## Common Task Decision Table

| Business need | Client to use | Starter responsibility | Business project responsibility |
|---|---|---|---|
| Mini Program login | `WechatMiniappClient` | Call `code2Session` and parse `openId` / `sessionKey` | Register or find user, bind `openId`, issue login token |
| Phone number parsing | `WechatMiniappClient` | Call `getPhoneNumber` and parse phone fields | Bind phone number, handle uniqueness and conflicts |
| Subscribe message | `WechatSubscribeClient` | Send subscribe message through WeChat API | Choose template, fill fields, decide timing and retry policy |
| JSAPI payment | `WechatPayClient` | Build WeChat prepay request and return JSAPI pay parameters | Own `outTradeNo`, amount, `openId`, and order state |
| Refund request | `WechatPayClient` | Build and submit WeChat refund request | Validate refund, own refund record and refund state |
| Payment notify | `WechatPayNotifyClient` | Verify signature, decrypt, and parse payment notify | Idempotency and business state update by payment fields |
| Refund notify | `WechatPayNotifyClient` | Verify signature, decrypt, and parse refund notify | Idempotency and business state update by refund fields |

## AI Execution Checklist

After integrating this starter into a business project, AI must check:

- Only the new starter dependency is imported.
- No old WeChat code was copied.
- No custom signature verification or decryption was written.
- Business state is not delegated to the starter.
- Exceptions are handled explicitly and are not converted to default success.
- The configuration prefix is `lixin.capability.wechat`.
- No service provider or ecommerce/commerce pay fields were added.
- No `subMchId`, `spMchId`, or `subAppId` fields were added.
- No silent fallback was added for critical fields.
