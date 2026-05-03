# Netease IM Starter Manual Integration Test

本文档用于后续使用真实网易云信 `appKey` / `appSecret` 对 `lixin-common-capability-netease-im-spring-boot-starter` 做手动集成验证。

当前仓库默认单元测试只使用 mock / stub，不访问真实网易云信接口。当前尚未提供会访问真实接口的 integration test；如需自动化真实联调，后续应单独新增显式 opt-in 的集成测试，并保证默认 `mvn test` 不触发真实请求。

## 测试目的

- 验证真实网易云信 IM 服务端接口可用性。
- 验证 HTTP provider 的请求路径、签名 Header、表单参数和响应解析符合真实接口契约。
- 验证 `createAccount`、`updateAccountProfile`、`refreshToken`、`callback verify` 的 starter DTO 语义与真实响应一致。
- 验证失败场景能显式暴露为异常或 `verified=false`，不返回 `null` 或假成功。

## 准备条件

- 网易云信测试应用的 `appKey`。
- 网易云信测试应用的 `appSecret`。
- 测试专用 `accountId`，例如 `test_im_20260503_001`。
- 测试 `name`，例如 `Manual Test User`。
- 测试 `avatar`，例如一个可公开访问的测试头像 URL。
- 本机网络可以访问网易云信服务端 API。
- 确认测试应用允许调用账号注册、用户名片更新、刷新 token 等服务端接口。

## 禁止事项

- 禁止提交真实 `appKey` / `appSecret`。
- 禁止把真实密钥写入 Git、README、Skill、测试源码或 `application.yml`。
- 禁止默认 `mvn test` 访问真实网易云信接口。
- 禁止使用生产用户 `accountId` 测试。
- 禁止在测试后把 token 写入业务表。
- 禁止把手动联调代码改成默认启用。
- 禁止新增会话、消息、事件、业务用户绑定等非 V1 能力。

## 建议运行方式

当前尚未提供真实接口 integration test。后续如需新增，可使用系统属性显式传入测试参数，并用单独 profile 或命名约定避免默认执行。

命令模板：

```bash
mvn -pl lixin-common-capability-netease-im-spring-boot-starter \
  -Dnetease.im.appKey=xxx \
  -Dnetease.im.appSecret=xxx \
  -Dnetease.im.accountId=test_xxx \
  -Dnetease.im.name="Manual Test User" \
  -Dnetease.im.avatar=https://example.com/avatar.png \
  test
```

要求：

- 上述命令只是后续新增显式集成测试时的模板。
- 不得把 `xxx` 替换后的真实值提交到仓库。
- 如果新增真实接口测试，必须默认跳过，只能通过显式参数或 profile 启用。

## 手动验证清单

### 1. createAccount

验证目标：

- 请求方法为 `POST`。
- 请求路径为 `/user/create.action`。
- 请求 Header 包含 `AppKey`、`Nonce`、`CurTime`、`CheckSum`。
- `CheckSum = sha1(appSecret + nonce + curTime)`。
- 请求体为 `application/x-www-form-urlencoded;charset=utf-8`。
- `accountId` 映射为 `accid`。
- `name` 映射为 `name`。
- `avatar` 映射为 `icon`。
- `extensionJson` 映射为 `ex`。
- 成功响应 `code=200`。
- 成功响应 `info.accid` 能映射为 `accountId`。
- 成功响应 `info.token` 能映射为 `token`，且不能为空。
- `rawResponse` 只记录真实响应体摘要，不伪造。

记录项：

| 字段 | 记录 |
| --- | --- |
| accountId |  |
| name |  |
| avatar |  |
| extensionJson |  |
| 是否成功 |  |
| providerCode |  |
| providerMessage |  |
| requestId |  |
| rawResponse 摘要 |  |
| 是否返回 token |  |
| 是否符合 starter DTO 语义 |  |

### 2. updateAccountProfile

验证目标：

- 请求方法为 `POST`。
- 请求路径为 `/user/updateUinfo.action`。
- 请求 Header 与账号接口一致。
- 请求体为 `application/x-www-form-urlencoded;charset=utf-8`。
- `accountId` 映射为 `accid`。
- `name` 映射为 `name`。
- `avatar` 映射为 `icon`。
- `extensionJson` 映射为 `ex`。
- 至少传入一个可更新字段。
- 成功响应 `code=200`。
- 响应不要求返回 token。
- `rawResponse` 只记录真实响应体摘要，不伪造。

记录项：

| 字段 | 记录 |
| --- | --- |
| accountId |  |
| name |  |
| avatar |  |
| extensionJson |  |
| 是否成功 |  |
| providerCode |  |
| providerMessage |  |
| requestId |  |
| rawResponse 摘要 |  |
| 是否返回 token | 不要求 |
| 是否符合 starter DTO 语义 |  |

### 3. refreshToken

验证目标：

- 请求方法为 `POST`。
- 请求路径为 `/user/refreshToken.action`。
- 请求 Header 与账号接口一致。
- 请求体为 `application/x-www-form-urlencoded;charset=utf-8`。
- `accountId` 映射为 `accid`。
- 成功响应 `code=200`。
- 成功响应 `info.accid` 能映射为 `accountId`。
- 成功响应 `info.token` 能映射为 `token`，且不能为空。
- `rawResponse` 只记录真实响应体摘要，不伪造。

记录项：

| 字段 | 记录 |
| --- | --- |
| accountId |  |
| 是否成功 |  |
| providerCode |  |
| providerMessage |  |
| requestId |  |
| rawResponse 摘要 |  |
| 是否返回 token |  |
| 是否符合 starter DTO 语义 |  |

### 4. callback verify

验证目标：

- `request.appKey` 来自回调 Header `AppKey`，并与配置 `app-key` 比对。
- `request.bodyMd5` 表达回调 Header `MD5`，即请求体 MD5。
- `request.checkSum` 来自回调 Header `CheckSum`。
- `CheckSum = sha1(appSecret + bodyMd5 + curTime)`。
- `bodyMd5` 不匹配时返回 `verified=false`。
- `checkSum` 不匹配时返回 `verified=false`。
- `appKey` 不匹配时返回 `verified=false`。
- 参数缺失时抛 `NeteaseImInvalidRequestException`。
- 算法异常时抛 `NeteaseImCallbackVerifyException`。
- starter 不决定业务 Controller 如何响应网易云信。

记录项：

| 字段 | 记录 |
| --- | --- |
| appKey 是否匹配 |  |
| curTime |  |
| bodyMd5 |  |
| checkSum |  |
| body 摘要 |  |
| verified |  |
| reason |  |
| providerCode |  |
| requestId |  |
| 是否符合 starter DTO 语义 |  |

## 失败排查

- `appKey` / `appSecret` 错误：检查配置是否来自测试应用，确认没有空格、换行或复制错误。
- 签名错误：检查 `Nonce`、`CurTime`、`CheckSum` 拼接顺序；账号接口为 `sha1(appSecret + nonce + curTime)`，回调验签为 `sha1(appSecret + MD5(body) + curTime)`。
- `accountId` 已存在：`createAccount` 可能返回失败 code，例如已注册。换一个测试专用 `accountId` 后重试。
- token 未返回：检查是否调用的是 `create.action` 或 `refreshToken.action`；如果真实成功响应仍缺 token，应阻塞发布并记录 `rawResponse` 摘要。
- HTTP path 错误：核对是否请求 `/user/create.action`、`/user/updateUinfo.action`、`/user/refreshToken.action`。
- 参数名错误：核对 `accid`、`name`、`icon`、`ex` 是否按表单参数提交。
- 云信错误码未正确映射：确认非 `200` body code 会转换为 `NeteaseImApiException`，不要包装为成功 DTO。
- 网络失败：检查 DNS、代理、防火墙、TLS 证书和网易云信服务状态。
- 回调验签失败：检查 Header 中 `MD5` 是否对应原始 body 字节内容，避免 JSON 格式化或编码变化后再计算。

## 发布前结论模板

```text
Netease IM Starter V1 发布前真实接口验证结论

- mock 测试已通过：是 / 否
- 真实接口 createAccount 是否通过：是 / 否 / 未执行
- 真实接口 updateAccountProfile 是否通过：是 / 否 / 未执行
- 真实接口 refreshToken 是否通过：是 / 否 / 未执行
- callback verify 手动验签是否通过：是 / 否 / 未执行
- 是否发现 token 缺失：是 / 否
- 是否发现 providerCode 映射异常：是 / 否
- 是否发现 rawResponse 伪造：是 / 否
- 是否访问了生产用户 accountId：是 / 否
- 是否允许发布：是 / 否
- 发布阻塞项：
```
