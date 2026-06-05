package com.lixin.capability.all.autoconfigure;

import com.lixin.capability.wxpusher.autoconfigure.LixinWxPusherAutoConfiguration;
import com.lixin.capability.wxpusher.service.WxPusherService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AllStarterWxPusherAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinWxPusherAutoConfiguration.class));

    @Test
    void allStarterCanCreateWxPusherServiceWhenEnabled() {
        contextRunner.withPropertyValues(
                        "lixin.capability.wxpusher.enabled=true",
                        "lixin.capability.wxpusher.app-token=AT_test")
                .run(context -> assertThat(context).hasSingleBean(WxPusherService.class));
    }
}
