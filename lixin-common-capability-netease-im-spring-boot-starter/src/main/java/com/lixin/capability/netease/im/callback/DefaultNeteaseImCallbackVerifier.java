package com.lixin.capability.netease.im.callback;

import com.lixin.capability.netease.im.dto.VerifyImCallbackRequest;
import com.lixin.capability.netease.im.dto.VerifyImCallbackResponse;
import com.lixin.capability.netease.im.exception.NeteaseImInvalidRequestException;
import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;
import com.lixin.capability.netease.im.provider.netease.NeteaseImSignatureSupport;

public class DefaultNeteaseImCallbackVerifier implements NeteaseImCallbackVerifier {
    private static final String PROVIDER_CODE_SUCCESS = "200";
    private static final String PROVIDER_CODE_VERIFY_FAILED = "VERIFY_FAILED";

    private final NeteaseImCapabilityProperties properties;
    private final NeteaseImSignatureSupport signatureSupport;

    public DefaultNeteaseImCallbackVerifier(NeteaseImCapabilityProperties properties,
                                            NeteaseImSignatureSupport signatureSupport) {
        this.properties = properties;
        this.signatureSupport = signatureSupport;
    }

    @Override
    public VerifyImCallbackResponse verify(VerifyImCallbackRequest request) {
        validateRequest(request);

        if (!properties.getAppKey().equals(request.getAppKey())) {
            return failed("appKey mismatch");
        }
        String expectedBodyMd5 = signatureSupport.md5(request.getBody());
        if (!expectedBodyMd5.equalsIgnoreCase(request.getBodyMd5())) {
            return failed("bodyMd5 mismatch");
        }
        String expectedCheckSum = signatureSupport.callbackCheckSum(
                properties.getAppSecret(), request.getBodyMd5(), request.getCurTime());
        if (!expectedCheckSum.equalsIgnoreCase(request.getCheckSum())) {
            return failed("checkSum mismatch");
        }

        VerifyImCallbackResponse response = new VerifyImCallbackResponse();
        response.setVerified(true);
        response.setReason("verified");
        response.setProviderCode(PROVIDER_CODE_SUCCESS);
        return response;
    }

    private void validateRequest(VerifyImCallbackRequest request) {
        if (request == null) {
            throw new NeteaseImInvalidRequestException("callback verify request must not be null");
        }
        if (!hasText(request.getAppKey())) {
            throw new NeteaseImInvalidRequestException("callback appKey must not be empty");
        }
        if (!hasText(request.getCurTime())) {
            throw new NeteaseImInvalidRequestException("callback curTime must not be empty");
        }
        if (!hasText(request.getBodyMd5())) {
            throw new NeteaseImInvalidRequestException("callback bodyMd5 must not be empty");
        }
        if (!hasText(request.getCheckSum())) {
            throw new NeteaseImInvalidRequestException("callback checkSum must not be empty");
        }
        if (request.getBody() == null) {
            throw new NeteaseImInvalidRequestException("callback body must not be null");
        }
    }

    private VerifyImCallbackResponse failed(String reason) {
        VerifyImCallbackResponse response = new VerifyImCallbackResponse();
        response.setVerified(false);
        response.setReason(reason);
        response.setProviderCode(PROVIDER_CODE_VERIFY_FAILED);
        return response;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
