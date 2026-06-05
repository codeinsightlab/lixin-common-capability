package com.lixin.capability.wxpusher.integration;

import com.lixin.capability.wxpusher.autoconfigure.LixinWxPusherAutoConfiguration;
import com.lixin.capability.wxpusher.dto.WxPusherSendResult;
import com.lixin.capability.wxpusher.service.WxPusherService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WxPusherRealSendIT {
    private static final String REAL_TEST_FLAG = "WXPUSHER_REAL_TEST";
    private static final String APP_TOKEN_ENV = "WXPUSHER_APP_TOKEN";
    private static final String TARGET_UID_ENV = "WXPUSHER_REAL_TEST_UID";
    /**
     * Local verification default only. Override with WXPUSHER_REAL_TEST_UID when validating another user.
     */
    private static final String DEFAULT_TARGET_UID = "UID_oN3AFEmfsR2jCGIlQVsZEOKreXjT";

    @Test
    void sendRealMessageToUidThroughWxPusherService() {
        String realTestFlag = System.getenv(REAL_TEST_FLAG);
        String appToken = System.getenv(APP_TOKEN_ENV);
        String targetUid = getTargetUid();
        assumeTrue("true".equalsIgnoreCase(realTestFlag),
                "Skip real WxPusher send: WXPUSHER_REAL_TEST is not true");
        assumeTrue(hasText(appToken),
                "Skip real WxPusher send: WXPUSHER_APP_TOKEN is blank");
        assumeTrue(hasText(targetUid),
                "Skip real WxPusher send: target UID is blank");

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LixinWxPusherAutoConfiguration.class))
                .withPropertyValues(
                        "lixin.capability.wxpusher.enabled=true",
                        "lixin.capability.wxpusher.app-token=" + appToken)
                .run(context -> {
                    WxPusherService wxPusherService = context.getBean(WxPusherService.class);
                    WxPusherSendResult result = wxPusherService.sendToUid(
                            targetUid,
                            "WxPusher真实发送测试",
                            "这是一条来自 lixin-common-capability 的真实发送测试消息。",
                            1,
                            null);

                    printResultSummary(result);
                    assertThat(result).isNotNull();
                    assertThat(result.isSuccess()).isTrue();
                });
    }

    private void printResultSummary(WxPusherSendResult result) {
        if (result == null) {
            System.out.println("WxPusher real send result: null");
            return;
        }
        System.out.println("WxPusher real send result summary: "
                + "success=" + result.isSuccess()
                + ", message=" + result.getMessage()
                + ", providerCode=" + result.getProviderCode()
                + ", providerMessage=" + result.getProviderMessage()
                + ", messageContentId=" + result.getMessageContentId()
                + ", sendRecordIds=" + result.getSendRecordIds());
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private String getTargetUid() {
        String uid = System.getenv(TARGET_UID_ENV);
        if (hasText(uid)) {
            return uid.trim();
        }
        return DEFAULT_TARGET_UID;
    }
}
