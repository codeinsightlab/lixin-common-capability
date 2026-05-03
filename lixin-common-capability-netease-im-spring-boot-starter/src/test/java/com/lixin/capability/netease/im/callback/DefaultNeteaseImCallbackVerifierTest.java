package com.lixin.capability.netease.im.callback;

import com.lixin.capability.netease.im.dto.VerifyImCallbackRequest;
import com.lixin.capability.netease.im.exception.NeteaseImInvalidRequestException;
import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;
import com.lixin.capability.netease.im.provider.netease.NeteaseImSignatureSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultNeteaseImCallbackVerifierTest {
    private NeteaseImSignatureSupport signatureSupport;
    private DefaultNeteaseImCallbackVerifier verifier;

    @BeforeEach
    void setUp() {
        NeteaseImCapabilityProperties properties = new NeteaseImCapabilityProperties();
        properties.setAppKey("app-key");
        properties.setAppSecret("app-secret");
        signatureSupport = new NeteaseImSignatureSupport();
        verifier = new DefaultNeteaseImCallbackVerifier(properties, signatureSupport);
    }

    @Test
    void rejectsMissingParameters() {
        assertThatThrownBy(() -> verifier.verify(null))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        VerifyImCallbackRequest missingBodyMd5 = validRequest();
        missingBodyMd5.setBodyMd5(" ");
        assertThatThrownBy(() -> verifier.verify(missingBodyMd5))
                .isInstanceOf(NeteaseImInvalidRequestException.class);

        VerifyImCallbackRequest missingBody = validRequest();
        missingBody.setBody(null);
        assertThatThrownBy(() -> verifier.verify(missingBody))
                .isInstanceOf(NeteaseImInvalidRequestException.class);
    }

    @Test
    void returnsFalseWhenAppKeyMismatch() {
        VerifyImCallbackRequest request = validRequest();
        request.setAppKey("other-key");

        assertThat(verifier.verify(request).isVerified()).isFalse();
        assertThat(verifier.verify(request).getReason()).isEqualTo("appKey mismatch");
    }

    @Test
    void returnsFalseWhenBodyMd5Mismatch() {
        VerifyImCallbackRequest request = validRequest();
        request.setBodyMd5("bad-md5");

        assertThat(verifier.verify(request).isVerified()).isFalse();
        assertThat(verifier.verify(request).getReason()).isEqualTo("bodyMd5 mismatch");
    }

    @Test
    void returnsFalseWhenCheckSumMismatch() {
        VerifyImCallbackRequest request = validRequest();
        request.setCheckSum("bad-checksum");

        assertThat(verifier.verify(request).isVerified()).isFalse();
        assertThat(verifier.verify(request).getReason()).isEqualTo("checkSum mismatch");
    }

    @Test
    void verifiesCallback() {
        assertThat(verifier.verify(validRequest()).isVerified()).isTrue();
        assertThat(verifier.verify(validRequest()).getProviderCode()).isEqualTo("200");
    }

    private VerifyImCallbackRequest validRequest() {
        String body = "{\"eventType\":1}";
        String bodyMd5 = signatureSupport.md5(body);
        String curTime = "1710000000";
        VerifyImCallbackRequest request = new VerifyImCallbackRequest();
        request.setAppKey("app-key");
        request.setCurTime(curTime);
        request.setBodyMd5(bodyMd5);
        request.setCheckSum(signatureSupport.callbackCheckSum("app-secret", bodyMd5, curTime));
        request.setBody(body);
        return request;
    }
}
