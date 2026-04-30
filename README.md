# lixin-common-capability

`lixin-common-capability` is a Spring Boot 2.x compatible common capability starter project.

V1 focuses on generic WeChat capability boundaries for Mini Program, subscribe messages, and normal merchant WeChat Pay. Business projects decide when to call these clients and how to handle their own domain state.

## V1 Supported Capabilities

- Mini Program `code2Session`
- Mini Program phone number parsing
- Mini Program `access_token` access
- Subscribe message sending
- WeChat Pay normal merchant JSAPI prepay
- WeChat Pay normal merchant refund request
- Payment notify signature verification, decryption, and parsing
- Refund notify signature verification, decryption, and parsing

## Explicitly Not Supported In V1

- WeChat ecommerce/commerce pay, also known as Shoufutong (微信收付通)
- WeChat service provider mode
- `subMchId`, `spMchId`, `subAppId`
- Merchant onboarding
- Profit sharing
- Transfer to user balance
- Order state handling
- Member handling
- Wallet balance handling
- Business transaction ledger handling
- Business notify idempotency handling
- Controller examples as a default business implementation
- Admin pages
- Multi payment channel SPI

## Maven Dependency

This is a local/internal module. Business projects are recommended to import capability starters on demand.

Use the WeChat starter when the project only needs WeChat capabilities:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-wechat-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Use the all-starter when the project wants to import every capability starter provided by this project:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-all-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The all-starter is only an aggregation package. It currently aggregates the WeChat starter only, and will aggregate NetEase IM, OSS, and other capability starters in future versions. The current WeChat configuration prefix, usage examples, and error handling rules remain valid.

## Configuration Example

The configuration prefix is `lixin.capability.wechat`.

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

V1 only supports Mini Program `storage.type: memory`.

## Mini Program Usage

```java
import com.lixin.capability.wechat.miniapp.client.WechatMiniappClient;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;

public class MiniappExample {
    private final WechatMiniappClient wechatMiniappClient;

    public MiniappExample(WechatMiniappClient wechatMiniappClient) {
        this.wechatMiniappClient = wechatMiniappClient;
    }

    public Code2SessionResponse code2Session(String code) {
        Code2SessionRequest request = new Code2SessionRequest();
        request.setCode(code);
        return wechatMiniappClient.code2Session(request);
    }

    public PhoneNumberResponse getPhoneNumber(String phoneCode) {
        PhoneNumberRequest request = new PhoneNumberRequest();
        request.setCode(phoneCode);
        return wechatMiniappClient.getPhoneNumber(request);
    }

    public String getAccessToken() {
        return wechatMiniappClient.getAccessToken();
    }
}
```

## Subscribe Message Usage

```java
import com.lixin.capability.wechat.subscribe.client.WechatSubscribeClient;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageData;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;

import java.util.Collections;

public class SubscribeExample {
    private final WechatSubscribeClient wechatSubscribeClient;

    public SubscribeExample(WechatSubscribeClient wechatSubscribeClient) {
        this.wechatSubscribeClient = wechatSubscribeClient;
    }

    public SubscribeMessageSendResponse send(String openId, String templateId) {
        SubscribeMessageData data = new SubscribeMessageData();
        data.setName("thing1");
        data.setValue("notification");

        SubscribeMessageSendRequest request = new SubscribeMessageSendRequest();
        request.setToUser(openId);
        request.setTemplateId(templateId);
        request.setPage("pages/index/index");
        request.setData(Collections.singletonList(data));
        return wechatSubscribeClient.send(request);
    }
}
```

## WeChat Pay JSAPI Usage

```java
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayRequest;
import com.lixin.capability.wechat.pay.dto.JsapiPrepayResponse;

public class JsapiPayExample {
    private final WechatPayClient wechatPayClient;

    public JsapiPayExample(WechatPayClient wechatPayClient) {
        this.wechatPayClient = wechatPayClient;
    }

    public JsapiPrepayResponse prepay(String openId, String outTradeNo) {
        JsapiPrepayRequest request = new JsapiPrepayRequest();
        request.setDescription("Product description");
        request.setOutTradeNo(outTradeNo);
        request.setAmountTotal(100);
        request.setCurrency("CNY");
        request.setPayerOpenId(openId);
        return wechatPayClient.jsapiPrepay(request);
    }
}
```

## Refund Usage

```java
import com.lixin.capability.wechat.pay.client.WechatPayClient;
import com.lixin.capability.wechat.pay.dto.RefundRequest;
import com.lixin.capability.wechat.pay.dto.RefundResponse;

public class RefundExample {
    private final WechatPayClient wechatPayClient;

    public RefundExample(WechatPayClient wechatPayClient) {
        this.wechatPayClient = wechatPayClient;
    }

    public RefundResponse refund(String outTradeNo, String outRefundNo) {
        RefundRequest request = new RefundRequest();
        request.setOutTradeNo(outTradeNo);
        request.setOutRefundNo(outRefundNo);
        request.setReason("Requested by user");
        request.setRefundAmount(100);
        request.setTotalAmount(100);
        request.setCurrency("CNY");
        return wechatPayClient.refund(request);
    }
}
```

## Payment Notify Usage

```java
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.notify.PaymentNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public class PaymentNotifyExample {
    private final WechatPayNotifyClient wechatPayNotifyClient;

    public PaymentNotifyExample(WechatPayNotifyClient wechatPayNotifyClient) {
        this.wechatPayNotifyClient = wechatPayNotifyClient;
    }

    public PaymentNotifyResponse parse(String serialNumber, String nonce,
                                       String timestamp, String signature, String body) {
        WechatNotifyRequest request = new WechatNotifyRequest();
        request.setSerialNumber(serialNumber);
        request.setNonce(nonce);
        request.setTimestamp(timestamp);
        request.setSignature(signature);
        request.setBody(body);

        PaymentNotifyResponse response = wechatPayNotifyClient.parsePaymentNotify(request);
        // Business projects handle outTradeNo and tradeState after parsing.
        return response;
    }
}
```

## Refund Notify Usage

```java
import com.lixin.capability.wechat.pay.client.WechatPayNotifyClient;
import com.lixin.capability.wechat.pay.notify.RefundNotifyResponse;
import com.lixin.capability.wechat.pay.notify.WechatNotifyRequest;

public class RefundNotifyExample {
    private final WechatPayNotifyClient wechatPayNotifyClient;

    public RefundNotifyExample(WechatPayNotifyClient wechatPayNotifyClient) {
        this.wechatPayNotifyClient = wechatPayNotifyClient;
    }

    public RefundNotifyResponse parse(String serialNumber, String nonce,
                                      String timestamp, String signature, String body) {
        WechatNotifyRequest request = new WechatNotifyRequest();
        request.setSerialNumber(serialNumber);
        request.setNonce(nonce);
        request.setTimestamp(timestamp);
        request.setSignature(signature);
        request.setBody(body);

        RefundNotifyResponse response = wechatPayNotifyClient.parseRefundNotify(request);
        // Business projects handle outRefundNo and refundStatus after parsing.
        return response;
    }
}
```

## Error Handling Rules

- Configuration errors throw `WechatCapabilityConfigException`.
- Invalid input throws `WechatCapabilityInvalidRequestException`.
- WeChat API or SDK call failures throw `WechatCapabilityApiException`.
- Notify signature verification failures throw `WechatCapabilityNotifyVerifyException`.
- Notify decryption failures throw `WechatCapabilityNotifyDecryptException`.
- Protocol or response parsing failures throw `WechatCapabilityParseException`.
- The starter does not silently fall back, swallow exceptions, or report fake success.
- SDK `null` responses and missing required response fields are exposed as exceptions.

## Raw Response Semantics

- The WeChat Mini Program SDK subscribe message send API does not expose a response body on success, so `SubscribeMessageSendResponse.rawResponse` is `null`.
- If the refund SDK does not expose a raw response body, `RefundResponse.rawResponse` is `null`.
- The starter does not synthesize `OK`, `rawResponse`, or `rawPlaintext` values.
- Notify `rawPlaintext` is the plaintext notification content, or its serialized representation, after SDK signature verification and decryption.
- `rawPlaintext` is intended for troubleshooting and audit. Business decisions should use structured fields such as `outTradeNo`, `tradeState`, `outRefundNo`, and `refundStatus`.

## Auto Configuration

- `miniapp.enabled=true` registers `WechatMiniappClient` and the default `WxMaService`.
- `subscribe.enabled=true` registers `WechatSubscribeClient` and requires a `WxMaService`.
- `pay.enabled=true` registers `WechatPayClient` and `WechatPayNotifyClient`.
- Missing required configuration fails explicitly at startup or client invocation.
- Spring Boot 2.x `spring.factories` auto configuration is supported.
- Default beans use `@ConditionalOnMissingBean`, so business projects can provide custom beans to override the defaults.

## Business Boundary

Business projects decide which openId to use, which trade or refund number to use, where the amount comes from, and how to handle business state after payment or refund. This starter only owns generic WeChat request building, response mapping, notify parsing, and exception boundaries.

## Version Suggestion

The recommended first WeChat V1 release tag is `v0.1.0-wechat`.
