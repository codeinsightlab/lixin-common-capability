package com.lixin.capability.wxpusher.autoconfigure;

import com.lixin.capability.wxpusher.service.WxPusherService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LixinWxPusherAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWxPusherAutoConfiguration.class));

    @Test
    void createsServiceWhenEnabled() {
        contextRunner.withPropertyValues(
                        "lixin.capability.wxpusher.enabled=true",
                        "lixin.capability.wxpusher.app-token=AT_test")
                .run(context -> assertThat(context).hasSingleBean(WxPusherService.class));
    }

    @Test
    void doesNotCreateServiceWhenDisabled() {
        contextRunner.withPropertyValues("lixin.capability.wxpusher.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(WxPusherService.class));
    }
}
