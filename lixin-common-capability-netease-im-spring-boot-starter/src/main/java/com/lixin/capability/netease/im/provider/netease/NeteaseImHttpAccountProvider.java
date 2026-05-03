package com.lixin.capability.netease.im.provider.netease;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lixin.capability.netease.im.dto.CreateImAccountRequest;
import com.lixin.capability.netease.im.dto.RefreshImTokenRequest;
import com.lixin.capability.netease.im.dto.UpdateImAccountRequest;
import com.lixin.capability.netease.im.exception.NeteaseImApiException;
import com.lixin.capability.netease.im.exception.NeteaseImParseException;
import com.lixin.capability.netease.im.properties.NeteaseImCapabilityProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NeteaseImHttpAccountProvider implements NeteaseImAccountProvider {
    private static final String SUCCESS_CODE = "200";
    private static final String CREATE_PATH = "/user/create.action";
    private static final String UPDATE_PROFILE_PATH = "/user/updateUinfo.action";
    private static final String REFRESH_TOKEN_PATH = "/user/refreshToken.action";

    private final NeteaseImCapabilityProperties properties;
    private final NeteaseImHttpTransport transport;
    private final NeteaseImSignatureSupport signatureSupport;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NeteaseImHttpAccountProvider(NeteaseImCapabilityProperties properties,
                                        NeteaseImHttpTransport transport,
                                        NeteaseImSignatureSupport signatureSupport) {
        this.properties = properties;
        this.transport = transport;
        this.signatureSupport = signatureSupport;
    }

    @Override
    public NeteaseImAccountResult createAccount(CreateImAccountRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("accid", request.getAccountId());
        putIfHasText(form, "name", request.getName());
        putIfHasText(form, "icon", request.getAvatar());
        putIfHasText(form, "ex", request.getExtensionJson());
        return postAndParse(CREATE_PATH, form, true);
    }

    @Override
    public NeteaseImAccountResult updateAccountProfile(UpdateImAccountRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("accid", request.getAccountId());
        putIfHasText(form, "name", request.getName());
        putIfHasText(form, "icon", request.getAvatar());
        putIfHasText(form, "ex", request.getExtensionJson());
        return postAndParse(UPDATE_PROFILE_PATH, form, false);
    }

    @Override
    public NeteaseImAccountResult refreshToken(RefreshImTokenRequest request) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("accid", request.getAccountId());
        return postAndParse(REFRESH_TOKEN_PATH, form, true);
    }

    private NeteaseImAccountResult postAndParse(String path, Map<String, String> form, boolean tokenRequired) {
        NeteaseImHttpResponse response = transport.postForm(resolveUrl(path), buildHeaders(), form, properties.getTimeoutMillis());
        if (response == null) {
            throw new NeteaseImParseException("Netease IM HTTP provider returned null response");
        }
        if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
            throw new NeteaseImApiException("Netease IM HTTP status " + response.getStatusCode(),
                    String.valueOf(response.getStatusCode()), response.getBody());
        }
        if (!hasText(response.getBody())) {
            throw new NeteaseImParseException("Netease IM response body is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String code = text(root, "code");
            if (!hasText(code)) {
                throw new NeteaseImParseException("Netease IM response missing code");
            }
            if (!SUCCESS_CODE.equals(code)) {
                throw new NeteaseImApiException("Netease IM API returned failure code " + code,
                        code, response.getBody());
            }

            JsonNode info = root.get("info");
            NeteaseImAccountResult result = new NeteaseImAccountResult();
            result.setProviderCode(code);
            result.setProviderMessage(firstText(root, "desc", "msg", "message"));
            result.setRequestId(firstText(root, "requestId", "reqId", "traceId"));
            if (!hasText(result.getRequestId())) {
                result.setRequestId(firstHeader(response.getHeaders(), "X-Netease-Request-Id", "X-Request-Id"));
            }
            result.setRawResponse(response.getBody());
            result.setAccountId(firstText(info, "accid", "accountId", "account"));
            result.setToken(firstText(info, "token"));

            if (!hasText(result.getAccountId())) {
                String fallbackAccountId = form.get("accid");
                if (!hasText(fallbackAccountId)) {
                    throw new NeteaseImParseException("Netease IM response missing accountId");
                }
                result.setAccountId(fallbackAccountId);
            }
            if (tokenRequired && !hasText(result.getToken())) {
                throw new NeteaseImParseException("Netease IM response missing token");
            }
            return result;
        } catch (NeteaseImApiException | NeteaseImParseException e) {
            throw e;
        } catch (Exception e) {
            throw new NeteaseImParseException("Netease IM response parse failed", e);
        }
    }

    private Map<String, String> buildHeaders() {
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String curTime = String.valueOf(System.currentTimeMillis() / 1000L);
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("AppKey", properties.getAppKey());
        headers.put("Nonce", nonce);
        headers.put("CurTime", curTime);
        headers.put("CheckSum", signatureSupport.requestCheckSum(properties.getAppSecret(), nonce, curTime));
        return headers;
    }

    private String resolveUrl(String path) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }

    private void putIfHasText(Map<String, String> form, String key, String value) {
        if (hasText(value)) {
            form.put(key, value);
        }
    }

    private String firstText(JsonNode node, String... names) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        for (String name : names) {
            String value = text(node, name);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String name) {
        if (node == null || node.get(name) == null || node.get(name).isNull()) {
            return null;
        }
        return node.get(name).asText();
    }

    private String firstHeader(Map<String, List<String>> headers, String... names) {
        if (headers == null) {
            return null;
        }
        for (String name : names) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)
                        && entry.getValue() != null && !entry.getValue().isEmpty()) {
                    return entry.getValue().get(0);
                }
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
