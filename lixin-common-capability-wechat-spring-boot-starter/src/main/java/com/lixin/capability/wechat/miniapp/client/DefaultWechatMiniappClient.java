package com.lixin.capability.wechat.miniapp.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaQrcodeServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import com.lixin.capability.wechat.exception.WechatCapabilityApiException;
import com.lixin.capability.wechat.exception.WechatCapabilityException;
import com.lixin.capability.wechat.exception.WechatCapabilityInvalidRequestException;
import com.lixin.capability.wechat.exception.WechatCapabilityParseException;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionRequest;
import com.lixin.capability.wechat.miniapp.dto.Code2SessionResponse;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberRequest;
import com.lixin.capability.wechat.miniapp.dto.PhoneNumberResponse;
import com.lixin.capability.wechat.miniapp.dto.WxaCodeUnlimitRequest;
import com.lixin.capability.wechat.miniapp.dto.WxaCodeUnlimitResponse;
import me.chanjar.weixin.common.error.WxErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultWechatMiniappClient implements WechatMiniappClient {
    private static final int MAX_SCENE_LENGTH = 32;
    private static final int DEFAULT_WIDTH = 430;
    private static final String DEFAULT_ENV_VERSION = "release";
    private static final String IMAGE_PNG = "image/png";
    private static final Set<String> SUPPORTED_ENV_VERSIONS =
            new HashSet<>(Arrays.asList("release", "trial", "develop"));

    private final WxMaService wxMaService;
    private final WxaCodeFileCreator wxaCodeFileCreator;

    public DefaultWechatMiniappClient(WxMaService wxMaService) {
        if (wxMaService == null) {
            throw new WechatCapabilityInvalidRequestException("WxMaService must not be null.");
        }
        this.wxMaService = wxMaService;
        this.wxaCodeFileCreator = request -> {
            WxMaQrcodeServiceImpl qrcodeService = new WxMaQrcodeServiceImpl(wxMaService);
            return qrcodeService.createWxaCodeUnlimit(request.getScene(), request.getPage(),
                    resolveCheckPath(request), resolveEnvVersion(request), resolveWidth(request), true, null, false);
        };
    }

    DefaultWechatMiniappClient(WxMaService wxMaService, WxaCodeFileCreator wxaCodeFileCreator) {
        if (wxMaService == null) {
            throw new WechatCapabilityInvalidRequestException("WxMaService must not be null.");
        }
        if (wxaCodeFileCreator == null) {
            throw new WechatCapabilityInvalidRequestException("WxaCodeFileCreator must not be null.");
        }
        this.wxMaService = wxMaService;
        this.wxaCodeFileCreator = wxaCodeFileCreator;
    }

    @Override
    public Code2SessionResponse code2Session(Code2SessionRequest request) {
        if (request == null || isBlank(request.getCode())) {
            throw new WechatCapabilityInvalidRequestException("Miniapp code2Session code must not be blank.");
        }
        try {
            WxMaJscode2SessionResult result = wxMaService.jsCode2SessionInfo(request.getCode());
            if (result == null) {
                throw new WechatCapabilityApiException("WeChat miniapp code2Session returned empty response.");
            }
            if (isBlank(result.getOpenid())) {
                throw new WechatCapabilityParseException("WeChat miniapp code2Session response openId must not be blank.");
            }
            if (isBlank(result.getSessionKey())) {
                throw new WechatCapabilityParseException("WeChat miniapp code2Session response sessionKey must not be blank.");
            }
            Code2SessionResponse response = new Code2SessionResponse();
            response.setOpenId(result.getOpenid());
            response.setUnionId(result.getUnionid());
            response.setSessionKey(result.getSessionKey());
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
            if (phoneInfo == null) {
                throw new WechatCapabilityApiException("WeChat miniapp phone number request returned empty response.");
            }
            if (isBlank(phoneInfo.getPhoneNumber())) {
                throw new WechatCapabilityParseException("WeChat miniapp phone number response phoneNumber must not be blank.");
            }
            if (isBlank(phoneInfo.getPurePhoneNumber())) {
                throw new WechatCapabilityParseException("WeChat miniapp phone number response purePhoneNumber must not be blank.");
            }
            PhoneNumberResponse response = new PhoneNumberResponse();
            response.setPhoneNumber(phoneInfo.getPhoneNumber());
            response.setPurePhoneNumber(phoneInfo.getPurePhoneNumber());
            response.setCountryCode(phoneInfo.getCountryCode());
            return response;
        } catch (WxErrorException e) {
            throw toApiException("WeChat miniapp phone number request failed.", e);
        }
    }

    @Override
    public String getAccessToken() {
        try {
            String accessToken = wxMaService.getAccessToken();
            if (isBlank(accessToken)) {
                throw new WechatCapabilityApiException("WeChat miniapp access_token response must not be blank.");
            }
            return accessToken;
        } catch (WxErrorException e) {
            throw toApiException("WeChat miniapp access_token request failed.", e);
        }
    }

    @Override
    public WxaCodeUnlimitResponse createWxaCodeUnlimit(WxaCodeUnlimitRequest request) {
        validateWxaCodeUnlimitRequest(request);
        WxaCodeUnlimitRequest normalizedRequest = normalizeWxaCodeUnlimitRequest(request);
        try {
            File codeFile = wxaCodeFileCreator.create(normalizedRequest);
            if (codeFile == null || !codeFile.isFile()) {
                throw new WechatCapabilityApiException(buildWxaCodeContextMessage(
                        "WeChat miniapp createWxaCodeUnlimit returned empty file.", normalizedRequest));
            }
            byte[] bytes = Files.readAllBytes(codeFile.toPath());
            if (bytes.length == 0) {
                throw new WechatCapabilityApiException(buildWxaCodeContextMessage(
                        "WeChat miniapp createWxaCodeUnlimit returned empty image.", normalizedRequest));
            }
            WxaCodeUnlimitResponse response = new WxaCodeUnlimitResponse();
            response.setBytes(bytes);
            response.setContentType(IMAGE_PNG);
            return response;
        } catch (WxErrorException e) {
            throw toApiException(buildWxaCodeContextMessage(
                    "WeChat miniapp createWxaCodeUnlimit failed.", normalizedRequest), e);
        } catch (IOException e) {
            throw new WechatCapabilityApiException(null, buildWxaCodeContextMessage(
                    "WeChat miniapp createWxaCodeUnlimit image read failed.", normalizedRequest), null, e);
        } catch (RuntimeException e) {
            if (e instanceof WechatCapabilityException) {
                throw e;
            }
            throw new WechatCapabilityApiException(null, buildWxaCodeContextMessage(
                    "WeChat miniapp createWxaCodeUnlimit SDK call failed.", normalizedRequest), null, e);
        }
    }

    private WechatCapabilityApiException toApiException(String message, WxErrorException e) {
        String code = e.getError() == null ? null : String.valueOf(e.getError().getErrorCode());
        String raw = e.getError() == null ? null : e.getError().toString();
        return new WechatCapabilityApiException(code, message + " " + e.getMessage(), raw, e);
    }

    private void validateWxaCodeUnlimitRequest(WxaCodeUnlimitRequest request) {
        if (request == null) {
            throw new WechatCapabilityInvalidRequestException("Miniapp createWxaCodeUnlimit request must not be null.");
        }
        if (isBlank(request.getScene())) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit scene must not be blank.");
        }
        if (request.getScene().trim().length() > MAX_SCENE_LENGTH) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit scene length must not exceed 32.");
        }
        if (isBlank(request.getPage())) {
            throw new WechatCapabilityInvalidRequestException("Miniapp createWxaCodeUnlimit page must not be blank.");
        }
        String page = request.getPage().trim();
        if (page.startsWith("/")) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit page must not start with '/'.");
        }
        if (page.contains("?") || page.contains("#")) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit page must not contain query or fragment.");
        }
        String envVersion = resolveEnvVersion(request);
        if (!SUPPORTED_ENV_VERSIONS.contains(envVersion)) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit envVersion must be release, trial, or develop.");
        }
        int width = resolveWidth(request);
        if (width <= 0) {
            throw new WechatCapabilityInvalidRequestException(
                    "Miniapp createWxaCodeUnlimit width must be positive.");
        }
    }

    private WxaCodeUnlimitRequest normalizeWxaCodeUnlimitRequest(WxaCodeUnlimitRequest request) {
        WxaCodeUnlimitRequest normalizedRequest = new WxaCodeUnlimitRequest();
        normalizedRequest.setScene(request.getScene().trim());
        normalizedRequest.setPage(request.getPage().trim());
        normalizedRequest.setCheckPath(resolveCheckPath(request));
        normalizedRequest.setEnvVersion(resolveEnvVersion(request));
        normalizedRequest.setWidth(resolveWidth(request));
        return normalizedRequest;
    }

    private String resolveEnvVersion(WxaCodeUnlimitRequest request) {
        return isBlank(request.getEnvVersion()) ? DEFAULT_ENV_VERSION : request.getEnvVersion().trim();
    }

    private boolean resolveCheckPath(WxaCodeUnlimitRequest request) {
        return request.getCheckPath() == null || request.getCheckPath();
    }

    private int resolveWidth(WxaCodeUnlimitRequest request) {
        return request.getWidth() == null ? DEFAULT_WIDTH : request.getWidth();
    }

    private String buildWxaCodeContextMessage(String message, WxaCodeUnlimitRequest request) {
        return message + " method=createWxaCodeUnlimit"
                + ", page=" + safeValue(request.getPage())
                + ", sceneLength=" + (request.getScene() == null ? 0 : request.getScene().trim().length())
                + ", envVersion=" + resolveEnvVersion(request)
                + ", checkPath=" + resolveCheckPath(request);
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    interface WxaCodeFileCreator {
        File create(WxaCodeUnlimitRequest request) throws WxErrorException;
    }
}
