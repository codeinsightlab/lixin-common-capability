package com.lixin.capability.wechat.subscribe.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaSubscribeService;
import cn.binarywang.wx.miniapp.bean.WxMaSubscribeMessage;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageData;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendRequest;
import com.lixin.capability.wechat.subscribe.dto.SubscribeMessageSendResponse;
import me.chanjar.weixin.common.error.WxErrorException;

import java.util.ArrayList;
import java.util.List;

public class DefaultWechatSubscribeClient implements WechatSubscribeClient {
    private final WxMaService wxMaService;
    private final String defaultMiniProgramState;
    private final String defaultLang;

    public DefaultWechatSubscribeClient(WxMaService wxMaService, String defaultMiniProgramState, String defaultLang) {
        if (wxMaService == null) {
            throw new WechatCapabilityInvalidRequestException("WxMaService must not be null.");
        }
        this.wxMaService = wxMaService;
        this.defaultMiniProgramState = defaultString(defaultMiniProgramState, "formal");
        this.defaultLang = defaultString(defaultLang, "zh_CN");
    }

    @Override
    public SubscribeMessageSendResponse send(SubscribeMessageSendRequest request) {
        validateRequest(request);
        WxMaSubscribeMessage message = buildMessage(request);
        try {
            WxMaSubscribeService subscribeService = wxMaService.getSubscribeService();
            subscribeService.sendSubscribeMsg(message);
            SubscribeMessageSendResponse response = new SubscribeMessageSendResponse();
            response.setSuccess(true);
            return response;
        } catch (WxErrorException e) {
            throw toApiException(e);
        }
    }

    private WxMaSubscribeMessage buildMessage(SubscribeMessageSendRequest request) {
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(request.getToUser());
        message.setTemplateId(request.getTemplateId());
        message.setPage(request.getPage());
        message.setMiniprogramState(defaultString(request.getMiniProgramState(), defaultMiniProgramState));
        message.setLang(defaultString(request.getLang(), defaultLang));
        if (request.getData() != null && !request.getData().isEmpty()) {
            List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
            for (SubscribeMessageData item : request.getData()) {
                if (item == null) {
                    throw new WechatCapabilityInvalidRequestException("Subscribe message data item must not be null.");
                }
                if (isBlank(item.getName())) {
                    throw new WechatCapabilityInvalidRequestException("Subscribe message data item name must not be blank.");
                }
                WxMaSubscribeMessage.MsgData msgData = new WxMaSubscribeMessage.MsgData();
                msgData.setName(item.getName());
                msgData.setValue(item.getValue());
                data.add(msgData);
            }
            message.setData(data);
        }
        return message;
    }

    private void validateRequest(SubscribeMessageSendRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("Subscribe message request must not be null.");
        }
        if (isBlank(request.getToUser())) {
            throw new WechatCapabilityInvalidRequestException("Subscribe message toUser must not be blank.");
        }
        if (isBlank(request.getTemplateId())) {
            throw new WechatCapabilityInvalidRequestException("Subscribe message templateId must not be blank.");
        }
    }

    private WechatCapabilityApiException toApiException(WxErrorException e) {
        String code = e.getError() == null ? null : String.valueOf(e.getError().getErrorCode());
        String raw = e.getError() == null ? null : e.getError().toString();
        return new WechatCapabilityApiException(code, "WeChat subscribe message send failed. " + e.getMessage(), raw, e);
    }

    private String defaultString(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
