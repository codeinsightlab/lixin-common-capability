package com.lixin.capability.oss.client;

import com.lixin.capability.oss.dto.DeleteObjectRequest;
import com.lixin.capability.oss.dto.GenerateUrlRequest;
import com.lixin.capability.oss.dto.GenerateUrlResponse;
import com.lixin.capability.oss.dto.UploadBytesRequest;
import com.lixin.capability.oss.dto.UploadInputStreamRequest;
import com.lixin.capability.oss.dto.UploadObjectResponse;
import com.lixin.capability.oss.exception.OssCapabilityInvalidRequestException;
import com.lixin.capability.oss.exception.OssCapabilityParseException;
import com.lixin.capability.oss.properties.OssCapabilityProperties;
import com.lixin.capability.oss.provider.aliyun.AliyunOssProvider;
import com.lixin.capability.oss.provider.aliyun.AliyunOssUploadResult;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

public class DefaultLixinOssClient implements LixinOssClient {
    private static final String PROVIDER_ALIYUN = "aliyun";

    private final OssCapabilityProperties properties;
    private final AliyunOssProvider aliyunOssProvider;

    public DefaultLixinOssClient(OssCapabilityProperties properties, AliyunOssProvider aliyunOssProvider) {
        this.properties = properties;
        this.aliyunOssProvider = aliyunOssProvider;
    }

    @Override
    public UploadObjectResponse uploadInputStream(UploadInputStreamRequest request) {
        validateUploadInputStream(request);
        String finalObjectKey = resolveObjectKey(request.getObjectKey());
        AliyunOssUploadResult result = aliyunOssProvider.upload(
                properties.getBucketName(),
                finalObjectKey,
                request.getInputStream(),
                request.getContentLength(),
                request.getContentType(),
                request.getMetadata());
        return toUploadResponse(finalObjectKey, result);
    }

    @Override
    public UploadObjectResponse uploadBytes(UploadBytesRequest request) {
        validateUploadBytes(request);
        UploadInputStreamRequest inputStreamRequest = new UploadInputStreamRequest();
        inputStreamRequest.setObjectKey(request.getObjectKey());
        inputStreamRequest.setInputStream(new ByteArrayInputStream(request.getBytes()));
        inputStreamRequest.setContentLength(request.getBytes().length);
        inputStreamRequest.setContentType(request.getContentType());
        inputStreamRequest.setMetadata(request.getMetadata());
        return uploadInputStream(inputStreamRequest);
    }

    @Override
    public void deleteObject(DeleteObjectRequest request) {
        validateDeleteObject(request);
        aliyunOssProvider.deleteObject(properties.getBucketName(), resolveObjectKey(request.getObjectKey()));
    }

    @Override
    public GenerateUrlResponse generateUrl(GenerateUrlRequest request) {
        validateGenerateUrl(request);
        String finalObjectKey = resolveObjectKey(request.getObjectKey());
        long expireSeconds = request.getExpireSeconds() == null
                ? properties.getDefaultExpireSeconds()
                : request.getExpireSeconds();
        Date expireAt = new Date(System.currentTimeMillis() + expireSeconds * 1000L);
        URL url = aliyunOssProvider.generateUrl(properties.getBucketName(), finalObjectKey, expireAt);
        if (url == null || !hasText(url.toString())) {
            throw new OssCapabilityParseException("Aliyun OSS generate URL returned empty URL");
        }

        GenerateUrlResponse response = new GenerateUrlResponse();
        response.setObjectKey(finalObjectKey);
        response.setUrl(url.toString());
        response.setExpireAt(expireAt);
        response.setProvider(PROVIDER_ALIYUN);
        return response;
    }

    private UploadObjectResponse toUploadResponse(String objectKey, AliyunOssUploadResult result) {
        if (result == null) {
            throw new OssCapabilityParseException("Aliyun OSS upload returned null result");
        }
        if (!hasText(result.getEtag())) {
            throw new OssCapabilityParseException("Aliyun OSS upload result missing ETag");
        }

        UploadObjectResponse response = new UploadObjectResponse();
        response.setObjectKey(objectKey);
        response.setUrl(result.getUrl());
        response.setEtag(result.getEtag());
        response.setBucketName(properties.getBucketName());
        response.setProvider(PROVIDER_ALIYUN);
        response.setRawResponse(result.getRawResponse());
        return response;
    }

    private void validateUploadInputStream(UploadInputStreamRequest request) {
        if (request == null) {
            throw new OssCapabilityInvalidRequestException("upload request must not be null");
        }
        validateObjectKey(request.getObjectKey());
        if (request.getInputStream() == null) {
            throw new OssCapabilityInvalidRequestException("inputStream must not be null");
        }
        if (request.getContentLength() <= 0) {
            throw new OssCapabilityInvalidRequestException("contentLength must be greater than 0");
        }
    }

    private void validateUploadBytes(UploadBytesRequest request) {
        if (request == null) {
            throw new OssCapabilityInvalidRequestException("upload request must not be null");
        }
        validateObjectKey(request.getObjectKey());
        if (request.getBytes() == null || request.getBytes().length == 0) {
            throw new OssCapabilityInvalidRequestException("bytes must not be empty");
        }
    }

    private void validateDeleteObject(DeleteObjectRequest request) {
        if (request == null) {
            throw new OssCapabilityInvalidRequestException("delete request must not be null");
        }
        validateObjectKey(request.getObjectKey());
    }

    private void validateGenerateUrl(GenerateUrlRequest request) {
        if (request == null) {
            throw new OssCapabilityInvalidRequestException("generate URL request must not be null");
        }
        validateObjectKey(request.getObjectKey());
        if (request.getExpireSeconds() != null && request.getExpireSeconds() <= 0) {
            throw new OssCapabilityInvalidRequestException("expireSeconds must be greater than 0");
        }
        if (request.getExpireSeconds() == null && properties.getDefaultExpireSeconds() <= 0) {
            throw new OssCapabilityInvalidRequestException("defaultExpireSeconds must be greater than 0");
        }
    }

    private void validateObjectKey(String objectKey) {
        if (!hasText(objectKey)) {
            throw new OssCapabilityInvalidRequestException("objectKey must not be empty");
        }
    }

    private String resolveObjectKey(String objectKey) {
        if (!hasText(properties.getObjectKeyPrefix())) {
            return objectKey;
        }
        return properties.getObjectKeyPrefix() + objectKey;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
