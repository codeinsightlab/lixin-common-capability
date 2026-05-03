# OSS Starter Usage Skill

Use this skill when an external AI / Codex / GPT needs to integrate the `lixin-common-capability-oss-spring-boot-starter`.

## Source Boundary

- Do not scan business repositories for OSS logic before using this starter.
- Use the README and this Skill as the integration contract.
- This starter is a generic Aliyun OSS gateway. It is not a file business system.

## Maven

Use the OSS starter when only OSS capability is needed:

```xml
<dependency>
    <groupId>com.lixin</groupId>
    <artifactId>lixin-common-capability-oss-spring-boot-starter</artifactId>
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

The all-starter currently aggregates WeChat and OSS.

## Configuration

Prefix: `lixin.capability.oss`

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

Rules:

- `enabled` defaults to `false`.
- V1 only supports `provider: aliyun`.
- When `enabled=true`, `provider`, `endpoint`, `bucket-name`, `access-key-id`, and `access-key-secret` are required.
- `default-expire-seconds` defaults to `3600`.
- `object-key-prefix` is optional and only for environment-level prefixes such as `dev/`, `test/`, or `prod/`.
- Do not add `base-url`.
- Do not hardcode AK/SK/bucket/endpoint in source code.
- Do not add business fields to OSS configuration.

## Client

Inject:

```java
import com.lixin.capability.oss.client.LixinOssClient;

private final LixinOssClient lixinOssClient;
```

Methods:

- `UploadObjectResponse uploadInputStream(UploadInputStreamRequest request)`
- `UploadObjectResponse uploadBytes(UploadBytesRequest request)`
- `void deleteObject(DeleteObjectRequest request)`
- `GenerateUrlResponse generateUrl(GenerateUrlRequest request)`

## Upload Bytes

```java
UploadBytesRequest request = new UploadBytesRequest();
request.setObjectKey(objectKey);
request.setBytes(bytes);
request.setContentType("image/png");

UploadObjectResponse response = lixinOssClient.uploadBytes(request);
```

Business code must provide `objectKey`. The starter does not generate business paths.

## Upload InputStream

```java
UploadInputStreamRequest request = new UploadInputStreamRequest();
request.setObjectKey(objectKey);
request.setInputStream(inputStream);
request.setContentLength(contentLength);
request.setContentType("application/pdf");

UploadObjectResponse response = lixinOssClient.uploadInputStream(request);
```

`contentLength` must be greater than `0`.

## Delete Object

```java
DeleteObjectRequest request = new DeleteObjectRequest();
request.setObjectKey(objectKey);

lixinOssClient.deleteObject(request);
```

Deletion failure must be handled as an exception. Do not treat exceptions as success.

## Generate Signed URL

```java
GenerateUrlRequest request = new GenerateUrlRequest();
request.setObjectKey(objectKey);
request.setExpireSeconds(600L);

GenerateUrlResponse response = lixinOssClient.generateUrl(request);
```

If `expireSeconds` is absent, the starter uses `default-expire-seconds`. The starter generates an Aliyun OSS signed URL. It does not concatenate `base-url + objectKey`.

## Business Boundary

Business projects own:

- objectKey strategy
- file table persistence
- user avatar binding
- order image relation
- file permission
- audit
- risk control
- idempotency
- controllers
- business state changes

Do not put business file logic into this starter.

## Not Supported

OSS V1 does not support:

- multi-provider SPI
- Tencent COS
- MinIO
- Qiniu
- local file upload
- automatic objectKey generation
- public URL assembly by `base-url + objectKey`
- user avatar or order image logic
- file table persistence
- image compression, watermarking, or cropping
- WeChat onboarding media upload
- Huifu image upload

## Exception Rules

- Configuration errors: `OssCapabilityConfigException`
- Invalid request: `OssCapabilityInvalidRequestException`
- Aliyun SDK call failures: `OssCapabilityApiException`
- SDK `null` responses, empty signed URLs, missing ETag, or missing critical fields: `OssCapabilityParseException`

Forbidden:

- Do not return `null` to hide failures.
- Do not return a default URL after upload failure.
- Do not silently succeed after delete failure.
- Do not return `null` after signed URL generation failure.
- Do not catch and only log.
- Do not fabricate `rawResponse`.
