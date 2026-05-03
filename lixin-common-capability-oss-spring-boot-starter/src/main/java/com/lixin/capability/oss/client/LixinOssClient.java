package com.lixin.capability.oss.client;

import com.lixin.capability.oss.dto.DeleteObjectRequest;
import com.lixin.capability.oss.dto.GenerateUrlRequest;
import com.lixin.capability.oss.dto.GenerateUrlResponse;
import com.lixin.capability.oss.dto.UploadBytesRequest;
import com.lixin.capability.oss.dto.UploadInputStreamRequest;
import com.lixin.capability.oss.dto.UploadObjectResponse;

public interface LixinOssClient {
    UploadObjectResponse uploadInputStream(UploadInputStreamRequest request);

    UploadObjectResponse uploadBytes(UploadBytesRequest request);

    void deleteObject(DeleteObjectRequest request);

    GenerateUrlResponse generateUrl(GenerateUrlRequest request);
}
