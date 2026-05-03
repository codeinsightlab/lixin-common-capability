package com.lixin.capability.oss.autoconfigure;

import com.aliyun.oss.OSS;
import com.lixin.capability.oss.client.LixinOssClient;
import com.lixin.capability.oss.exception.OssCapabilityConfigException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LixinOssAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LixinOssAutoConfiguration.class))
            .withBean(OSS.class, () -> mock(OSS.class));

    @Test
    void registersLixinOssClientWhenEnabled() {
        contextRunner
                .withPropertyValues(validProperties())
                .run(context -> assertThat(context).hasSingleBean(LixinOssClient.class));
    }

    @Test
    void doesNotRegisterLixinOssClientWhenDisabled() {
        contextRunner
                .withPropertyValues("lixin.capability.oss.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(LixinOssClient.class));
    }

    @Test
    void failsWhenEndpointMissing() {
        assertConfigFailure("lixin.capability.oss.endpoint");
    }

    @Test
    void failsWhenBucketNameMissing() {
        assertConfigFailure("lixin.capability.oss.bucket-name");
    }

    @Test
    void failsWhenAccessKeyIdMissing() {
        assertConfigFailure("lixin.capability.oss.access-key-id");
    }

    @Test
    void failsWhenAccessKeySecretMissing() {
        assertConfigFailure("lixin.capability.oss.access-key-secret");
    }

    @Test
    void failsWhenProviderUnsupported() {
        contextRunner
                .withPropertyValues(
                        "lixin.capability.oss.enabled=true",
                        "lixin.capability.oss.provider=minio",
                        "lixin.capability.oss.endpoint=https://oss-cn-hangzhou.aliyuncs.com",
                        "lixin.capability.oss.bucket-name=test-bucket",
                        "lixin.capability.oss.access-key-id=test-ak",
                        "lixin.capability.oss.access-key-secret=test-sk")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(OssCapabilityConfigException.class)
                            .hasMessageContaining("only supports provider aliyun");
                });
    }

    private void assertConfigFailure(String missingProperty) {
        contextRunner
                .withPropertyValues(validPropertiesExcept(missingProperty))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(OssCapabilityConfigException.class)
                            .hasMessageContaining(missingProperty);
                });
    }

    private String[] validProperties() {
        return new String[] {
                "lixin.capability.oss.enabled=true",
                "lixin.capability.oss.provider=aliyun",
                "lixin.capability.oss.endpoint=https://oss-cn-hangzhou.aliyuncs.com",
                "lixin.capability.oss.bucket-name=test-bucket",
                "lixin.capability.oss.access-key-id=test-ak",
                "lixin.capability.oss.access-key-secret=test-sk"
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
}
