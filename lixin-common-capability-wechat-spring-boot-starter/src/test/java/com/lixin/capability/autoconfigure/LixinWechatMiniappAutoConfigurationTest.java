package com.lixin.capability.autoconfigure;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.miniapp.client.WechatMiniappClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LixinWechatMiniappAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWechatAutoConfiguration.class));

    @Test
    void registersMiniappClientWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.wechat.miniapp.enabled=true",
                        "lixin.capability.wechat.miniapp.app-id=test-app-id",
                        "lixin.capability.wechat.miniapp.secret=test-secret",
                        "lixin.capability.wechat.miniapp.storage.type=memory")
                .run(context -> {
                    assertThat(context).hasSingleBean(WechatMiniappClient.class);
                    assertThat(context).hasSingleBean(WxMaService.class);
                });
    }

    @Test
    void doesNotRegisterMiniappClientWhenDisabled() {
        contextRunner
                .withPropertyValues("lixin.capability.wechat.miniapp.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WechatMiniappClient.class));
    }

    @Test
    void failsClearlyWhenRequiredConfigMissing() {
        contextRunner
                .withPropertyValues("lixin.capability.wechat.miniapp.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(WechatCapabilityConfigException.class)
                            .hasMessageContaining("app-id and secret");
                });
    }
}
