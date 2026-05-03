package com.lixin.capability.oss.provider.aliyun;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.VoidResult;
import com.lixin.capability.oss.exception.OssCapabilityApiException;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class AliyunOssProvider {
    private final OSS ossClient;

    public AliyunOssProvider(OSS ossClient) {
        this.ossClient = ossClient;
    }

    public AliyunOssUploadResult upload(String bucketName, String objectKey, InputStream inputStream,
                                        long contentLength, String contentType, Map<String, String> metadata) {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(contentLength);
            if (hasText(contentType)) {
                objectMetadata.setContentType(contentType);
            }
            if (metadata != null) {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    objectMetadata.addUserMetadata(entry.getKey(), entry.getValue());
                }
            }

            PutObjectResult result = ossClient.putObject(new PutObjectRequest(bucketName, objectKey, inputStream, objectMetadata));
            if (result == null) {
                return null;
            }
            AliyunOssUploadResult uploadResult = new AliyunOssUploadResult();
            uploadResult.setEtag(result.getETag());
            uploadResult.setUrl(result.getResponse() == null ? null : result.getResponse().getUri());
            uploadResult.setRawResponse(toRawResponse(result.getResponse()));
            return uploadResult;
        } catch (OSSException | ClientException ex) {
            throw new OssCapabilityApiException("Aliyun OSS upload failed", ex);
        }
    }

    public void deleteObject(String bucketName, String objectKey) {
        try {
            VoidResult result = ossClient.deleteObject(bucketName, objectKey);
            if (result == null) {
                throw new OssCapabilityApiException("Aliyun OSS delete returned null result", null);
            }
        } catch (OSSException | ClientException ex) {
            throw new OssCapabilityApiException("Aliyun OSS delete failed", ex);
        }
    }

    public URL generateUrl(String bucketName, String objectKey, Date expireAt) {
        try {
            return ossClient.generatePresignedUrl(bucketName, objectKey, expireAt);
        } catch (OSSException | ClientException ex) {
            throw new OssCapabilityApiException("Aliyun OSS generate URL failed", ex);
        }
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String toRawResponse(ResponseMessage response) {
        if (response == null) {
            return null;
        }
        return "statusCode=" + response.getStatusCode()
                + ",requestId=" + response.getRequestId()
                + ",uri=" + response.getUri();
    }
}
