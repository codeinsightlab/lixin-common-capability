# lixin-common-capability

`lixin-common-capability` is a Spring Boot 2.x compatible common capability starter project.

## V1 Scope

V1 only provides generic WeChat client boundaries:

- WeChat Mini Program configuration
- access_token access through `WechatMiniappClient`
- code2Session
- phone code parsing
- subscribe message sending
- WeChat Pay V3 normal merchant configuration
- JSAPI prepay
- refund request
- payment notify verification/decryption/parsing boundary
- refund notify verification/decryption/parsing boundary

Current default implementations are placeholders. They throw `WechatCapabilityException` with a clear message because real WeChat API calls are not implemented yet.

## Explicitly Not Supported In V1

- WeChat service provider mode
- WeChat ecommerce/commerce pay
- sub merchant id, service provider merchant id, sub app id
- merchant onboarding
- profit sharing
- transfer to user balance
- order state handling
- member handling
- wallet balance handling
- business transaction ledger handling
- business refund record handling
- admin pages
- multi payment channel SPI

## Configuration Example

```yaml
lixin:
  capability:
    wechat:
      miniapp:
        enabled: true
        app-id: wx_xxx
        secret: xxx
        token:
        aes-key:
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
        wechat-pay-public-key-path: /path/to/pub_key.pem
        wechat-pay-public-key-id: xxx
        notify-url: https://example.com/pay/notify
        refund-notify-url: https://example.com/pay/refund/notify
```

## Usage Boundary

Business projects decide when to call clients, which openId to use, which order number to use, where the amount comes from, and how to handle business state after payment or refund. This starter only owns generic WeChat request, response, notify parsing, and exception boundaries.
