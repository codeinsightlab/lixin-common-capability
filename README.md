# lixin-common-capability

`lixin-common-capability` is a Spring Boot 2.x compatible common capability starter project.

V1 focuses on generic WeChat capability boundaries and Aliyun OSS basic gateway capability. Business projects decide when to call these clients and how to handle their own domain state.

## V1 Supported Capabilities

- Mini Program `code2Session`
- Mini Program phone number parsing
- Mini Program `access_token` access
- Subscribe message sending
- WeChat Pay normal merchant JSAPI prepay
- WeChat Pay normal merchant refund request
- Payment notify signature verification, decryption, and parsing
- Refund notify signature verification, decryption, and parsing
- Aliyun OSS upload from `InputStream`
- Aliyun OSS upload from `byte[]`
- Aliyun OSS object deletion
- Aliyun OSS signed URL generation

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
- OSS multi provider SPI
- Tencent COS, MinIO, Qiniu, or other OSS providers
- Public URL assembly by `base-url + objectKey`
- Automatic objectKey generation
- Local file upload handling
- File table persistence
- User avatar binding
- Order image relation handling
- File permission, audit, or risk-control handling
- Business objectKey directory rules such as `avatar/{userId}` or `order/{orderId}`
- Image compression, watermarking, or cropping
- WeChat onboarding media upload or other business-specific file upload flows

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

Use the OSS starter when the project only needs Aliyun OSS gateway capabilities:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-oss-spring-boot-starter</artifactId>
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

The all-starter is only an aggregation package. It currently aggregates the WeChat starter and OSS starter. The current WeChat and OSS configuration prefixes, usage examples, and error handling rules remain valid.

## Configuration Example

The WeChat configuration prefix is `lixin.capability.wechat`.

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

The OSS configuration prefix is `lixin.capability.oss`. `enabled` defaults to `false`; when it is `true`, `provider`, `endpoint`, `bucket-name`, `access-key-id`, and `access-key-secret` are required. OSS V1 only supports `provider: aliyun`.

```yaml
lixin:
  capability:
    oss:
      enabled: true
      provider: aliyun
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      bucket-name: example-bucket
      access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID}
      access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET}
      default-expire-seconds: 3600
      object-key-prefix: dev/
```

`object-key-prefix` is only an environment-level common prefix such as `dev/`, `test/`, or `prod/`. Business projects must still pass the `objectKey` and own business directory strategy outside the starter.

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

## OSS Usage

```java
import com.lixin.capability.oss.client.LixinOssClient;
import com.lixin.capability.oss.dto.DeleteObjectRequest;
import com.lixin.capability.oss.dto.GenerateUrlRequest;
import com.lixin.capability.oss.dto.GenerateUrlResponse;
import com.lixin.capability.oss.dto.UploadBytesRequest;
import com.lixin.capability.oss.dto.UploadObjectResponse;

public class OssExample {
    private final LixinOssClient lixinOssClient;

    public OssExample(LixinOssClient lixinOssClient) {
        this.lixinOssClient = lixinOssClient;
    }

    public UploadObjectResponse upload(byte[] bytes, String objectKey) {
        UploadBytesRequest request = new UploadBytesRequest();
        request.setObjectKey(objectKey);
        request.setBytes(bytes);
        request.setContentType("image/png");
        return lixinOssClient.uploadBytes(request);
    }

    public GenerateUrlResponse generateUrl(String objectKey) {
        GenerateUrlRequest request = new GenerateUrlRequest();
        request.setObjectKey(objectKey);
        request.setExpireSeconds(600L);
        return lixinOssClient.generateUrl(request);
    }

    public void delete(String objectKey) {
        DeleteObjectRequest request = new DeleteObjectRequest();
        request.setObjectKey(objectKey);
        lixinOssClient.deleteObject(request);
    }
}
```

`LixinOssClient.uploadInputStream` accepts an `InputStream`, required `objectKey`, positive `contentLength`, optional `contentType`, and optional `Map<String, String>` metadata. `uploadBytes` wraps bytes into an input stream internally. `generateUrl` creates an Aliyun OSS signed URL; it does not assemble a public URL from a base URL.

## Error Handling Rules

- Configuration errors throw `WechatCapabilityConfigException`.
- Invalid input throws `WechatCapabilityInvalidRequestException`.
- WeChat API or SDK call failures throw `WechatCapabilityApiException`.
- Notify signature verification failures throw `WechatCapabilityNotifyVerifyException`.
- Notify decryption failures throw `WechatCapabilityNotifyDecryptException`.
- Protocol or response parsing failures throw `WechatCapabilityParseException`.
- The starter does not silently fall back, swallow exceptions, or report fake success.
- SDK `null` responses and missing required response fields are exposed as exceptions.
- OSS configuration errors throw `OssCapabilityConfigException`.
- OSS invalid input throws `OssCapabilityInvalidRequestException`.
- Aliyun OSS SDK call failures throw `OssCapabilityApiException`.
- OSS SDK `null` responses, empty signed URLs, missing ETag, or missing critical response fields throw `OssCapabilityParseException`.

## Raw Response Semantics

- The WeChat Mini Program SDK subscribe message send API does not expose a response body on success, so `SubscribeMessageSendResponse.rawResponse` is `null`.
- If the refund SDK does not expose a raw response body, `RefundResponse.rawResponse` is `null`.
- The starter does not synthesize `OK`, `rawResponse`, or `rawPlaintext` values.
- Notify `rawPlaintext` is the plaintext notification content, or its serialized representation, after SDK signature verification and decryption.
- `rawPlaintext` is intended for troubleshooting and audit. Business decisions should use structured fields such as `outTradeNo`, `tradeState`, `outRefundNo`, and `refundStatus`.
- OSS `UploadObjectResponse.rawResponse` is only populated from real Aliyun OSS SDK response information when the SDK exposes it. It is not synthesized.

## Auto Configuration

- `miniapp.enabled=true` registers `WechatMiniappClient` and the default `WxMaService`.
- `subscribe.enabled=true` registers `WechatSubscribeClient` and requires a `WxMaService`.
- `pay.enabled=true` registers `WechatPayClient` and `WechatPayNotifyClient`.
- Missing required configuration fails explicitly at startup or client invocation.
- Spring Boot 2.x `spring.factories` auto configuration is supported.
- Default beans use `@ConditionalOnMissingBean`, so business projects can provide custom beans to override the defaults.
- `lixin.capability.oss.enabled=true` registers `LixinOssClient` for Aliyun OSS.
- `lixin.capability.oss.enabled=false` or missing does not register OSS beans.
- OSS V1 rejects any provider other than `aliyun`.

## Business Boundary

Business projects decide which openId to use, which trade or refund number to use, where the amount comes from, which OSS objectKey to use, where file records are stored, and how to handle business state after payment, refund, upload, deletion, or URL generation. This starter only owns generic WeChat request building, OSS SDK calls, response mapping, notify parsing, signed URL generation, and exception boundaries.

## Version Suggestion

The recommended first WeChat V1 release tag is `v0.1.0-wechat`.
