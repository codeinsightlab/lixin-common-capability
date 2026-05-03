package com.lixin.capability.netease.im.autoconfigure;

import com.lixin.capability.netease.im.callback.NeteaseImCallbackVerifier;
import com.lixin.capability.netease.im.client.NeteaseImAccountClient;
import com.lixin.capability.netease.im.exception.NeteaseImConfigException;
import com.lixin.capability.netease.im.provider.netease.NeteaseImHttpResponse;
import com.lixin.capability.netease.im.provider.netease.NeteaseImHttpTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LixinNeteaseImAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinNeteaseImAutoConfiguration.class))
            .withBean(NeteaseImHttpTransport.class, () -> new StubTransport());

    @Test
    void registersClientAndVerifierWhenEnabled() {
        contextRunner
                .withPropertyValues(validProperties())
                .run(context -> {
                    assertThat(context).hasSingleBean(NeteaseImAccountClient.class);
                    assertThat(context).hasSingleBean(NeteaseImCallbackVerifier.class);
                });
    }

    @Test
    void doesNotRegisterBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("lixin.capability.netease.im.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(NeteaseImAccountClient.class);
                    assertThat(context).doesNotHaveBean(NeteaseImCallbackVerifier.class);
                });
    }

    @Test
    void failsWhenAppKeyMissing() {
        assertConfigFailure("lixin.capability.netease.im.app-key");
    }

    @Test
    void failsWhenAppSecretMissing() {
        assertConfigFailure("lixin.capability.netease.im.app-secret");
    }

    @Test
    void failsWhenBaseUrlBlank() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.netease.im.enabled=true",
                        "lixin.capability.netease.im.app-key=test-ak",
                        "lixin.capability.netease.im.app-secret=test-sk",
                        "lixin.capability.netease.im.base-url= ")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(NeteaseImConfigException.class)
                            .hasMessageContaining("base-url");
                });
    }

    @Test
    void failsWhenTimeoutInvalid() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.netease.im.enabled=true",
                        "lixin.capability.netease.im.app-key=test-ak",
                        "lixin.capability.netease.im.app-secret=test-sk",
                        "lixin.capability.netease.im.timeout-millis=0")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(NeteaseImConfigException.class)
                            .hasMessageContaining("timeout-millis");
                });
    }

    private void assertConfigFailure(String missingProperty) {
        contextRunner
                .withPropertyValues(validPropertiesExcept(missingProperty))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(NeteaseImConfigException.class)
                            .hasMessageContaining(missingProperty);
                });
    }

    private String[] validProperties() {
        return new String[] {
                "lixin.capability.netease.im.enabled=true",
                "lixin.capability.netease.im.app-key=test-ak",
                "lixin.capability.netease.im.app-secret=test-sk",
                "lixin.capability.netease.im.base-url=https://api.yunxinapi.com/nimserver",
                "lixin.capability.netease.im.timeout-millis=3000"
        };
    }

    private String[] validPropertiesExcept(String missingProperty) {
        String[] properties = validProperties();
        int kept = 0;
        for (String property : properties) {
            if (!property.startsWith(missingProperty + "=")) {
                kept++;
            }
        }
        String[] result = new String[kept];
        int index = 0;
        for (String property : properties) {
            if (!property.startsWith(missingProperty + "=")) {
                result[index++] = property;
            }
        }
        return result;
    }

    private static class StubTransport implements NeteaseImHttpTransport {
        @Override
        public NeteaseImHttpResponse postForm(String url, Map<String, String> headers, Map<String, String> form, int timeoutMillis) {
            return new NeteaseImHttpResponse(200, "{\"code\":200,\"info\":{\"accid\":\"a\",\"token\":\"t\"}}", Collections.emptyMap());
        }
    }
}
