package com.lixin.capability.autoconfigure;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.lixin.capability.wechat.exception.WechatCapabilityConfigException;
import com.lixin.capability.wechat.miniapp.client.DefaultWechatMiniappClient;
import com.lixin.capability.wechat.miniapp.client.WechatMiniappClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "lixin.capability.wechat.miniapp", name = "enabled", havingValue = "true")
public class LixinWechatMiniappAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public WxMaService wxMaService(WechatCapabilityProperties properties) {
        WechatCapabilityProperties.Miniapp miniapp = properties.getMiniapp();
        validateMiniapp(miniapp);
        validateStorage(miniapp.getStorage());

        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(miniapp.getAppId());
        config.setSecret(miniapp.getSecret());
        config.setToken(miniapp.getToken());
        config.setAesKey(miniapp.getAesKey());

        WxMaServiceImpl service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }

    @Bean
    @ConditionalOnMissingBean
    public WechatMiniappClient wechatMiniappClient(WxMaService wxMaService) {
        return new DefaultWechatMiniappClient(wxMaService);
    }

    private void validateMiniapp(WechatCapabilityProperties.Miniapp miniapp) {
        if (miniapp == null || isBlank(miniapp.getAppId()) || isBlank(miniapp.getSecret())) {
            throw new WechatCapabilityConfigException("Miniapp app-id and secret are required when lixin.capability.wechat.miniapp.enabled=true.");
        }
    }

    private void validateStorage(WechatCapabilityProperties.Storage storage) {
        String type = storage == null || isBlank(storage.getType()) ? "memory" : storage.getType();
        if (!"memory".equalsIgnoreCase(type)) {
            throw new WechatCapabilityConfigException("Only miniapp storage.type=memory is supported in V1.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
