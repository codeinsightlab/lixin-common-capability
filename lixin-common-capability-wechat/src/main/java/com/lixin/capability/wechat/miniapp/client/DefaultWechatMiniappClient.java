package com.lixin.capability.wechat.miniapp.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;
import me.chanjar.weixin.common.error.WxErrorException;

public class DefaultWechatMiniappClient implements WechatMiniappClient {
    private final WxMaService wxMaService;

    public DefaultWechatMiniappClient(WxMaService wxMaService) {
        if (wxMaService == null) {
            throw new WechatCapabilityInvalidRequestException("WxMaService must not be null.");
        }
        this.wxMaService = wxMaService;
    }

    @Override
    public Code2SessionResponse code2Session(Code2SessionRequest request) {
        if (request == null || isBlank(request.getCode())) {
            throw new WechatCapabilityInvalidRequestException("Miniapp code2Session code must not be blank.");
        }
        try {
            WxMaJscode2SessionResult result = wxMaService.jsCode2SessionInfo(request.getCode());
            Code2SessionResponse response = new Code2SessionResponse();
            if (result != null) {
                response.setOpenId(result.getOpenid());
                response.setUnionId(result.getUnionid());
                response.setSessionKey(result.getSessionKey());
            }
            return response;
        } catch (WxErrorException e) {
            throw toApiException("WeChat miniapp code2Session failed.", e);
        }
    }

    @Override
    public PhoneNumberResponse getPhoneNumber(PhoneNumberRequest request) {
        if (request == null || isBlank(request.getCode())) {
            throw new WechatCapabilityInvalidRequestException("Miniapp phone code must not be blank.");
        }
        try {
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(request.getCode());
            PhoneNumberResponse response = new PhoneNumberResponse();
            if (phoneInfo != null) {
                response.setPhoneNumber(phoneInfo.getPhoneNumber());
                response.setPurePhoneNumber(phoneInfo.getPurePhoneNumber());
                response.setCountryCode(phoneInfo.getCountryCode());
            }
            return response;
        } catch (WxErrorException e) {
            throw toApiException("WeChat miniapp phone number request failed.", e);
        }
    }

    @Override
    public String getAccessToken() {
        try {
            return wxMaService.getAccessToken();
        } catch (WxErrorException e) {
            throw toApiException("WeChat miniapp access_token request failed.", e);
        }
    }

    private WechatCapabilityApiException toApiException(String message, WxErrorException e) {
        String code = e.getError() == null ? null : String.valueOf(e.getError().getErrorCode());
        String raw = e.getError() == null ? null : e.getError().toString();
        return new WechatCapabilityApiException(code, message + " " + e.getMessage(), raw, e);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
