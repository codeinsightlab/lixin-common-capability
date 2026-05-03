# AGENTS.md

本文件用于约束后续 AI / Codex / GPT 在 `common-capability` 项目中的协作规则。

所有后续改动必须遵守本文档。除非用户明确要求修改本文档，否则不得绕过、弱化或删除这些约束。

## 1. 项目定位

本项目名称为 `common-capability`。

本项目定位是通用能力 Spring Boot Starter 工程，不是业务系统。

项目目标是沉淀可复用的第三方能力接入标准件，让业务项目可以通过：

1. 引入 starter
2. 写配置
3. 注入 Client
4. 调用方法
5. 自己处理业务结果

本项目沉淀的内容包括：

- 第三方 API 调用
- 配置初始化
- 签名 / 验签 / 解密
- DTO
- 异常分类
- 自动装配
- 测试覆盖
- 外部 AI 使用 Skill

## 2. 当前模块结构原则

本项目采用：

`按能力拆 Starter + all-starter 聚合`

当前模块结构：

- `lixin-common-capability-core`
- `lixin-common-capability-wechat-spring-boot-starter`
- `lixin-common-capability-oss-spring-boot-starter`
- `lixin-common-capability-netease-im-spring-boot-starter`
- `lixin-common-capability-all-spring-boot-starter`

模块规则：

- 一个能力域一个 starter。
- `all-starter` 只聚合依赖，不写具体能力代码。
- `core` 只放真正跨能力复用的基础能力，不提前乱放代码。

## 3. 业务边界规则

Starter 只负责：

- 第三方 SDK / API 初始化
- 第三方 API 调用
- 签名 / 验签 / 解密
- 响应解析
- 字段校验
- 异常分类
- 自动装配

业务项目负责：

- 用户注册 / 登录
- 用户绑定
- 订单状态
- 钱包
- 会员
- 交易流水
- 业务幂等
- 业务 Controller
- 业务落库
- 业务状态机

禁止把业务逻辑写进 Starter。

## 4. 错误处理硬约束

错误处理必须显式、可追踪、可测试。

禁止以下行为：

- 禁止静默兜底。
- 禁止吞异常。
- 禁止 catch 后只 log 不抛。
- 禁止 catch 后返回 null / false / true / 空对象。
- 禁止 SDK 返回 null 后包装成功 DTO。
- 禁止配置缺失后静默跳过。
- 禁止关键字段缺失仍返回成功响应。
- 禁止伪造 rawResponse。
- 禁止伪造 rawPlaintext。
- 禁止为了测试通过加入生产兜底逻辑。
- 未实现能力必须明确抛异常，不能返回假成功。

## 5. Skill 同步硬约束

`skills/*.md` 是外部 AI 使用 starter 的接入契约。

凡是发生以下任一变化，必须同步更新对应 Skill：

- Maven artifactId 变化
- 模块名变化
- 配置前缀变化
- application.yml 示例变化
- 新增 / 删除 Client
- 新增 / 删除 Client 方法
- Request / Response DTO 字段变化
- 异常类型变化
- 异常语义变化
- AutoConfiguration 条件变化
- 支持能力变化
- 不支持范围变化
- rawResponse / rawPlaintext 语义变化
- 业务边界变化
- Maven 引入方式变化
- 使用示例变化

Skill 必须遵守以下规则：

- Skill 不允许描述未实现能力。
- Skill 不允许保留旧 artifactId。
- Skill 不允许保留旧配置前缀。
- Skill 不允许暗示 Starter 处理业务状态。
- Skill 必须以当前源码和 README 为准。
- 如果源码、README、Skill 三者不一致，发布前必须修正。
- 不更新 Skill，不允许认为任务完成。
- 不更新 Skill，不允许打 tag。

## 6. README 同步规则

凡是影响外部使用方式的改动，也必须同步 README。

README 和 Skill 的关系：

- README 面向人。
- Skill 面向外部 AI / Codex / GPT。
- 两者必须与源码一致。
- 两者不能写未来规划能力作为已支持能力。

外部使用方式包括但不限于：

- Maven 引入方式
- artifactId
- 模块名
- 配置前缀
- application.yml 示例
- Client 名称
- Client 方法
- Request / Response DTO 字段
- 异常类型和语义
- 自动装配条件
- 支持能力和不支持范围
- rawResponse / rawPlaintext 语义
- 业务边界
- 使用示例

## 7. 每次任务执行报告要求

后续每次 Codex 执行报告都必须包含：

- 修改文件清单
- 是否影响 README
- 是否影响 Skill
- 如果影响，说明已同步更新哪些 Skill
- 如果不影响，明确写：`本次不影响 Skill，无需更新`
- Maven 命令和结果
- 是否存在遗留风险

## 8. 发布前检查规则

发布前必须确认：

- 源码与 README 一致。
- README 与 `skills/*.md` 一致。
- `skills/*.md` 不包含未实现能力。
- `skills/*.md` 不包含旧 artifactId / 旧配置。
- `skills/*.md` 不暗示 Starter 处理业务状态。
- 没有静默兜底 / 假成功。
- 没有业务污染。
- 单模块测试通过。
- 整体 `mvn -q test` 通过。
- 工作区干净。
- tag 指向包含当前发布内容的 commit。

禁止在上述检查未完成或未通过时打 tag 或发布。

## 9. 微信 V1 当前边界

微信 V1 当前支持能力：

- `WechatMiniappClient.code2Session`
- `WechatMiniappClient.getPhoneNumber`
- `WechatMiniappClient.getAccessToken`
- `WechatSubscribeClient.send`
- `WechatPayClient.jsapiPrepay`
- `WechatPayClient.refund`
- `WechatPayNotifyClient.parsePaymentNotify`
- `WechatPayNotifyClient.parseRefundNotify`

微信 V1 当前不支持：

- 微信收付通
- 服务商模式
- subMchId / spMchId / subAppId
- 商户进件
- 分账
- 转账到零钱
- 订单状态处理
- 会员处理
- 钱包处理
- 交易流水
- 回调业务幂等
- Controller 默认业务处理

不得在 README 或 Skill 中把上述不支持能力描述为已支持能力。

## 10. OSS V1 当前边界

OSS V1 当前支持能力：

- `LixinOssClient.uploadInputStream`
- `LixinOssClient.uploadBytes`
- `LixinOssClient.deleteObject`
- `LixinOssClient.generateUrl`

OSS V1 当前只支持：

- 阿里云 OSS
- 业务方传入 `objectKey`
- 阿里云 OSS 签名 URL 生成
- `object-key-prefix` 作为 `dev/`、`test/`、`prod/` 等环境级通用前缀

OSS V1 当前不支持：

- 多厂商 SPI
- 腾讯 COS
- MinIO
- 七牛
- `base-url + objectKey` 公开 URL 拼接
- 自动 objectKey 生成
- 本地文件上传
- 文件表落库
- 用户头像绑定
- 订单图片关系
- 文件权限、审核、风控
- `avatar/{userId}`、`order/{orderId}` 等业务目录规则
- 图片压缩、水印、裁剪
- 微信进件媒体上传
- 汇付图片上传
- 订单、会员、钱包、用户、商品、附件关系等业务状态

不得在 README 或 Skill 中把上述不支持能力描述为已支持能力。

## 11. 网易云信 IM V1 当前边界

网易云信 IM V1 当前支持能力：

- `NeteaseImAccountClient.createAccount`
- `NeteaseImAccountClient.updateAccountProfile`
- `NeteaseImAccountClient.refreshToken`
- `NeteaseImCallbackVerifier.verify`

网易云信 IM V1 当前只支持：

- 网易云信账号 Gateway
- 网易云信回调签名校验
- 统一 HTTP client provider
- 业务方传入 `accountId`
- `extensionJson` 字符串扩展字段
- `bodyMd5` 表达回调 body 的 MD5

网易云信 IM V1 当前不支持：

- SDK v1 / SDK v2 / HTTP PATCH 混用
- 用户注册 / 登录
- `SysUser` / `LoginUser` / `SecurityUtils`
- `SysUser` 与 `accid` 绑定
- token 写库
- 会话列表
- 消息记录
- 已读 / 未读
- 删除会话
- 历史聊天同步
- 事件落库 / 事件分发
- 订单聊天关系
- `conversationId = from + "|1|" + to` 这类业务规则
- 暴露网易云信 SDK DTO
- Controller 默认业务处理

不得在 README 或 Skill 中把上述不支持能力描述为已支持能力。

## 12. 后续能力新增规则

新增网易云信、短信等能力时，必须按以下顺序执行：

1. 先只读审查历史项目。
2. 再做接口设计审查。
3. 再建模块 / Client / DTO / 异常 / AutoConfiguration。
4. 再逐个能力小步实现。
5. 每个能力必须补测试。
6. 每个能力完成后做兜底 / 假成功检查。
7. 最后补 README 和 Skill。
8. 再发布前验收。

新增能力时必须继续遵守业务边界规则、错误处理硬约束、README 同步规则和 Skill 同步硬约束。

## 13. 文档风格

项目文档必须使用 Markdown。

文档要求：

- 清晰。
- 可复制。
- 可被 AI 直接执行。
- 重点写约束和执行规则。
- 不写长篇产品愿景。
- 不把未来规划能力写成已支持能力。

## 14. 本文件变更规则

修改 `AGENTS.md` 时必须保持规则强度不降低。

如果后续源码、README、Skill 或发布流程发生变化，且影响本文档约束，必须同步更新 `AGENTS.md`。
