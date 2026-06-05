package com.lixin.capability.wxpusher.autoconfigure;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationImportsTest {
    @Test
    void containsWxPusherAutoConfiguration() throws Exception {
        assertThat(autoConfigurationImports())
                .contains("com.lixin.capability.wxpusher.autoconfigure.LixinWxPusherAutoConfiguration");
    }

    private String autoConfigurationImports() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        assertThat(inputStream).isNotNull();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
