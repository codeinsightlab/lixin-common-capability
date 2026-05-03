package com.lixin.capability.oss.client;

import com.lixin.capability.oss.dto.DeleteObjectRequest;
import com.lixin.capability.oss.dto.GenerateUrlRequest;
import com.lixin.capability.oss.dto.GenerateUrlResponse;
import com.lixin.capability.oss.dto.UploadBytesRequest;
import com.lixin.capability.oss.dto.UploadInputStreamRequest;
import com.lixin.capability.oss.dto.UploadObjectResponse;
import com.lixin.capability.oss.exception.OssCapabilityApiException;
import com.lixin.capability.oss.exception.OssCapabilityInvalidRequestException;
import com.lixin.capability.oss.exception.OssCapabilityParseException;
import com.lixin.capability.oss.properties.OssCapabilityProperties;
import com.lixin.capability.oss.provider.aliyun.AliyunOssProvider;
import com.lixin.capability.oss.provider.aliyun.AliyunOssUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultLixinOssClientTest {
    private OssCapabilityProperties properties;
    private RecordingAliyunOssProvider provider;
    private DefaultLixinOssClient client;

    @BeforeEach
    void setUp() {
        properties = new OssCapabilityProperties();
        properties.setProvider("aliyun");
        properties.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        properties.setBucketName("test-bucket");
        properties.setAccessKeyId("test-ak");
        properties.setAccessKeySecret("test-sk");
        properties.setDefaultExpireSeconds(3600);

        provider = new RecordingAliyunOssProvider();
        client = new DefaultLixinOssClient(properties, provider);
    }

    @Test
    void rejectsInvalidUploadInputStreamRequests() {
        assertThatThrownBy(() -> client.uploadInputStream(null))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        UploadInputStreamRequest blankObjectKey = validInputStreamRequest();
        blankObjectKey.setObjectKey(" ");
        assertThatThrownBy(() -> client.uploadInputStream(blankObjectKey))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        UploadInputStreamRequest emptyInputStream = validInputStreamRequest();
        emptyInputStream.setInputStream(null);
        assertThatThrownBy(() -> client.uploadInputStream(emptyInputStream))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        UploadInputStreamRequest invalidLength = validInputStreamRequest();
        invalidLength.setContentLength(0);
        assertThatThrownBy(() -> client.uploadInputStream(invalidLength))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);
    }

    @Test
    void rejectsInvalidUploadBytesRequests() {
        assertThatThrownBy(() -> client.uploadBytes(null))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        UploadBytesRequest blankObjectKey = validBytesRequest();
        blankObjectKey.setObjectKey("");
        assertThatThrownBy(() -> client.uploadBytes(blankObjectKey))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        UploadBytesRequest emptyBytes = validBytesRequest();
        emptyBytes.setBytes(new byte[0]);
        assertThatThrownBy(() -> client.uploadBytes(emptyBytes))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);
    }

    @Test
    void rejectsInvalidDeleteObjectRequests() {
        assertThatThrownBy(() -> client.deleteObject(null))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        DeleteObjectRequest request = new DeleteObjectRequest();
        request.setObjectKey(" ");
        assertThatThrownBy(() -> client.deleteObject(request))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);
    }

    @Test
    void rejectsInvalidGenerateUrlRequests() {
        assertThatThrownBy(() -> client.generateUrl(null))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        GenerateUrlRequest blankObjectKey = new GenerateUrlRequest();
        blankObjectKey.setObjectKey(" ");
        assertThatThrownBy(() -> client.generateUrl(blankObjectKey))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);

        GenerateUrlRequest invalidExpireSeconds = new GenerateUrlRequest();
        invalidExpireSeconds.setObjectKey("image/a.png");
        invalidExpireSeconds.setExpireSeconds(0L);
        assertThatThrownBy(() -> client.generateUrl(invalidExpireSeconds))
                .isInstanceOf(OssCapabilityInvalidRequestException.class);
    }

    @Test
    void uploadsInputStreamAndMapsResponse() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("trace", "test");
        UploadInputStreamRequest request = validInputStreamRequest();
        request.setContentType("image/png");
        request.setMetadata(metadata);

        UploadObjectResponse response = client.uploadInputStream(request);

        assertThat(provider.bucketName).isEqualTo("test-bucket");
        assertThat(provider.objectKey).isEqualTo("image/a.png");
        assertThat(provider.contentLength).isEqualTo(3);
        assertThat(provider.contentType).isEqualTo("image/png");
        assertThat(provider.metadata).containsEntry("trace", "test");
        assertThat(response.getObjectKey()).isEqualTo("image/a.png");
        assertThat(response.getEtag()).isEqualTo("etag-1");
        assertThat(response.getBucketName()).isEqualTo("test-bucket");
        assertThat(response.getProvider()).isEqualTo("aliyun");
        assertThat(response.getRawResponse()).isEqualTo("raw-from-sdk");
    }

    @Test
    void uploadsBytesAndAppliesObjectKeyPrefix() {
        properties.setObjectKeyPrefix("dev/");
        UploadObjectResponse response = client.uploadBytes(validBytesRequest());

        assertThat(provider.objectKey).isEqualTo("dev/image/a.png");
        assertThat(provider.contentLength).isEqualTo(3);
        assertThat(response.getObjectKey()).isEqualTo("dev/image/a.png");
    }

    @Test
    void deletesObject() {
        DeleteObjectRequest request = new DeleteObjectRequest();
        request.setObjectKey("image/a.png");

        client.deleteObject(request);

        assertThat(provider.deletedBucketName).isEqualTo("test-bucket");
        assertThat(provider.deletedObjectKey).isEqualTo("image/a.png");
    }

    @Test
    void generatesUrlAndUsesDefaultExpireSecondsWhenMissing() {
        properties.setDefaultExpireSeconds(10);
        GenerateUrlRequest request = new GenerateUrlRequest();
        request.setObjectKey("image/a.png");

        GenerateUrlResponse response = client.generateUrl(request);

        assertThat(provider.generatedBucketName).isEqualTo("test-bucket");
        assertThat(provider.generatedObjectKey).isEqualTo("image/a.png");
        assertThat(provider.generatedExpireAt).isNotNull();
        assertThat(response.getUrl()).isEqualTo("https://signed.example.com/image/a.png");
        assertThat(response.getObjectKey()).isEqualTo("image/a.png");
        assertThat(response.getProvider()).isEqualTo("aliyun");
        assertThat(response.getExpireAt()).isEqualTo(provider.generatedExpireAt);
    }

    @Test
    void convertsProviderApiException() {
        provider.apiFailure = true;

        assertThatThrownBy(() -> client.uploadBytes(validBytesRequest()))
                .isInstanceOf(OssCapabilityApiException.class);
    }

    @Test
    void failsWhenUploadResultIsNullOrEtagMissing() {
        provider.nullUploadResult = true;
        assertThatThrownBy(() -> client.uploadBytes(validBytesRequest()))
                .isInstanceOf(OssCapabilityParseException.class);

        provider.nullUploadResult = false;
        provider.missingEtag = true;
        assertThatThrownBy(() -> client.uploadBytes(validBytesRequest()))
                .isInstanceOf(OssCapabilityParseException.class);
    }

    @Test
    void failsWhenGeneratedUrlIsNull() {
        provider.nullGeneratedUrl = true;
        GenerateUrlRequest request = new GenerateUrlRequest();
        request.setObjectKey("image/a.png");

        assertThatThrownBy(() -> client.generateUrl(request))
                .isInstanceOf(OssCapabilityParseException.class);
    }

    private UploadInputStreamRequest validInputStreamRequest() {
        UploadInputStreamRequest request = new UploadInputStreamRequest();
        request.setObjectKey("image/a.png");
        request.setInputStream(new ByteArrayInputStream("abc".getBytes(StandardCharsets.UTF_8)));
        request.setContentLength(3);
        return request;
    }

    private UploadBytesRequest validBytesRequest() {
        UploadBytesRequest request = new UploadBytesRequest();
        request.setObjectKey("image/a.png");
        request.setBytes("abc".getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private static class RecordingAliyunOssProvider extends AliyunOssProvider {
        private String bucketName;
        private String objectKey;
        private long contentLength;
        private String contentType;
        private Map<String, String> metadata;
        private String deletedBucketName;
        private String deletedObjectKey;
        private String generatedBucketName;
        private String generatedObjectKey;
        private Date generatedExpireAt;
        private boolean apiFailure;
        private boolean nullUploadResult;
        private boolean missingEtag;
        private boolean nullGeneratedUrl;

        RecordingAliyunOssProvider() {
            super(null);
        }

        @Override
        public AliyunOssUploadResult upload(String bucketName, String objectKey, InputStream inputStream,
                                            long contentLength, String contentType, Map<String, String> metadata) {
            if (apiFailure) {
                throw new OssCapabilityApiException("provider failed", new RuntimeException("sdk"));
            }
            this.bucketName = bucketName;
            this.objectKey = objectKey;
            this.contentLength = contentLength;
            this.contentType = contentType;
            this.metadata = metadata;
            if (nullUploadResult) {
                return null;
            }
            AliyunOssUploadResult result = new AliyunOssUploadResult();
            result.setEtag(missingEtag ? null : "etag-1");
            result.setRawResponse("raw-from-sdk");
            return result;
        }

        @Override
        public void deleteObject(String bucketName, String objectKey) {
            this.deletedBucketName = bucketName;
            this.deletedObjectKey = objectKey;
        }

        @Override
        public URL generateUrl(String bucketName, String objectKey, Date expireAt) {
            this.generatedBucketName = bucketName;
            this.generatedObjectKey = objectKey;
            this.generatedExpireAt = expireAt;
            if (nullGeneratedUrl) {
                return null;
            }
            try {
                return new URL("https://signed.example.com/" + objectKey);
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
