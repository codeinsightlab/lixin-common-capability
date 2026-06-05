package com.lixin.capability.wxpusher.service;

import com.lixin.capability.wxpusher.dto.WxPusherSendRequest;
import com.lixin.capability.wxpusher.dto.WxPusherSendResult;
import com.lixin.capability.wxpusher.dto.WxPusherResponse;
import com.lixin.capability.wxpusher.exception.WxPusherApiException;
import com.lixin.capability.wxpusher.exception.WxPusherException;
import com.lixin.capability.wxpusher.properties.WxPusherProperties;
import com.lixin.capability.wxpusher.provider.WxPusherProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultWxPusherService implements WxPusherService {
    private static final Logger log = LoggerFactory.getLogger(DefaultWxPusherService.class);
    private static final int SUCCESS_CODE = 1000;
    private static final int DEFAULT_CONTENT_TYPE = 1;
    private static final int MAX_CONTENT_LENGTH = 40000;
    private static final int MAX_SUMMARY_LENGTH = 100;
    private static final int MAX_URL_LENGTH = 1000;
    private static final int MAX_UIDS = 2000;
    private static final int MAX_TOPIC_IDS = 5;

    private final WxPusherProperties properties;
    private final WxPusherProvider provider;
    private final AtomicBoolean disabledLogged = new AtomicBoolean(false);

    public DefaultWxPusherService(WxPusherProperties properties, WxPusherProvider provider) {
        this.properties = properties;
        this.provider = provider;
    }

    @Override
    public WxPusherSendResult sendToUid(String uid, String title, String content, Integer contentType, String url) {
        return sendToUids(hasText(uid) ? Collections.singletonList(uid) : Collections.<String>emptyList(),
                title, content, contentType, url);
    }

    @Override
    public WxPusherSendResult sendToUids(List<String> uids, String title, String content, Integer contentType, String url) {
        WxPusherSendRequest request = new WxPusherSendRequest();
        request.setUids(cleanUids(uids));
        request.setTitle(title);
        request.setContent(content);
        request.setContentType(contentType);
        request.setUrl(url);
        return send(request, "uid", request.getUids().size());
    }

    @Override
    public WxPusherSendResult sendToTopic(Integer topicId, String title, String content, Integer contentType, String url) {
        WxPusherSendRequest request = new WxPusherSendRequest();
        if (topicId != null) {
            request.setTopicIds(Collections.singletonList(topicId));
        }
        request.setTitle(title);
        request.setContent(content);
        request.setContentType(contentType);
        request.setUrl(url);
        return send(request, "topic", request.getTopicIds() == null ? 0 : request.getTopicIds().size());
    }

    private WxPusherSendResult send(WxPusherSendRequest request, String targetType, int targetCount) {
        WxPusherSendResult validation = validate(request);
        if (validation != null) {
            return validation;
        }
        try {
            WxPusherResponse response = provider.send(request);
            if (log.isDebugEnabled()) {
                log.debug("WxPusher raw response: {}", response.getRawResponse());
            }
            WxPusherSendResult result = toResult(response);
            if (result.isSuccess()) {
                log.info("WxPusher send succeeded, targetType={}, targetCount={}", targetType, targetCount);
            } else {
                log.warn("WxPusher send failed, targetType={}, targetCount={}, code={}, message={}",
                        targetType, targetCount, result.getProviderCode(), result.getProviderMessage());
            }
            return result;
        } catch (WxPusherApiException e) {
            if (log.isDebugEnabled()) {
                log.debug("WxPusher raw response: {}", e.getRawResponse());
            }
            log.warn("WxPusher send failed: {}", e.getMessage());
            WxPusherSendResult result = WxPusherSendResult.failure(e.getMessage());
            result.setProviderCode(e.getProviderCode());
            result.setRawResponse(e.getRawResponse());
            return result;
        } catch (WxPusherException e) {
            log.warn("WxPusher send failed: {}", e.getMessage());
            return WxPusherSendResult.failure(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("WxPusher send failed by unexpected runtime error: {}", e.getMessage());
            return WxPusherSendResult.failure("WxPusher send failed");
        }
    }

    private WxPusherSendResult validate(WxPusherSendRequest request) {
        if (properties == null || !properties.isEnabled()) {
            if (disabledLogged.compareAndSet(false, true)) {
                log.info("WxPusher capability is disabled, skip sending");
            }
            return WxPusherSendResult.failure("WxPusher capability is disabled");
        }
        if (!hasText(properties.getAppToken())) {
            return WxPusherSendResult.failure("lixin.capability.wxpusher.app-token is required");
        }
        if (request == null) {
            return WxPusherSendResult.failure("WxPusher send request must not be null");
        }
        if (!hasText(request.getContent())) {
            return WxPusherSendResult.failure("WxPusher content must not be empty");
        }
        if (request.getContent().length() > MAX_CONTENT_LENGTH) {
            return WxPusherSendResult.failure("WxPusher content length must not exceed 40000");
        }
        if (hasText(request.getTitle()) && request.getTitle().length() > MAX_SUMMARY_LENGTH) {
            return WxPusherSendResult.failure("WxPusher title/summary length must not exceed 100");
        }
        if (hasText(request.getUrl()) && request.getUrl().length() > MAX_URL_LENGTH) {
            return WxPusherSendResult.failure("WxPusher url length must not exceed 1000");
        }
        if (request.getContentType() == null) {
            request.setContentType(DEFAULT_CONTENT_TYPE);
        }
        if (request.getContentType() < 1 || request.getContentType() > 3) {
            return WxPusherSendResult.failure("WxPusher contentType only supports 1(text), 2(html), or 3(markdown)");
        }
        int uidCount = request.getUids() == null ? 0 : request.getUids().size();
        int topicCount = request.getTopicIds() == null ? 0 : request.getTopicIds().size();
        if (uidCount == 0 && topicCount == 0) {
            return WxPusherSendResult.failure("WxPusher target uid/uids/topicId must not be empty");
        }
        if (uidCount > MAX_UIDS) {
            return WxPusherSendResult.failure("WxPusher uids size must not exceed 2000");
        }
        if (topicCount > MAX_TOPIC_IDS) {
            return WxPusherSendResult.failure("WxPusher topicIds size must not exceed 5");
        }
        return null;
    }

    private WxPusherSendResult toResult(WxPusherResponse response) {
        if (response == null) {
            return WxPusherSendResult.failure("WxPusher provider returned null result");
        }
        WxPusherSendResult result = new WxPusherSendResult();
        result.setProviderCode(response.getCode());
        result.setProviderMessage(response.getMsg());
        result.setRawResponse(response.getRawResponse());
        result.setSuccess(Integer.valueOf(SUCCESS_CODE).equals(response.getCode()) && Boolean.TRUE.equals(response.getSuccess()));
        result.setMessage(result.isSuccess() ? "WxPusher send succeeded" : response.getMsg());
        List<Long> sendRecordIds = new ArrayList<>();
        if (response.getData() != null) {
            for (WxPusherResponse.Item item : response.getData()) {
                if (item.getMessageContentId() != null && result.getMessageContentId() == null) {
                    result.setMessageContentId(item.getMessageContentId());
                }
                if (item.getSendRecordId() != null) {
                    sendRecordIds.add(item.getSendRecordId());
                }
                if (item.getCode() != null && !Integer.valueOf(SUCCESS_CODE).equals(item.getCode())) {
                    result.setSuccess(false);
                    result.setMessage(item.getStatus());
                }
            }
        }
        result.setSendRecordIds(sendRecordIds);
        return result;
    }

    private List<String> cleanUids(List<String> uids) {
        List<String> cleaned = new ArrayList<>();
        if (uids == null) {
            return cleaned;
        }
        for (String uid : uids) {
            if (hasText(uid)) {
                cleaned.add(uid.trim());
            }
        }
        return cleaned;
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }
}
