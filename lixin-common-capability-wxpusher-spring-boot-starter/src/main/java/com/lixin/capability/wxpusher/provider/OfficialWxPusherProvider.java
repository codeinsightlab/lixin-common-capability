package com.lixin.capability.wxpusher.provider;

import com.lixin.capability.wxpusher.dto.WxPusherSendRequest;
import com.lixin.capability.wxpusher.dto.WxPusherResponse;
import com.lixin.capability.wxpusher.exception.WxPusherApiException;
import com.lixin.capability.wxpusher.exception.WxPusherParseException;
import com.smjcco.wxpusher.client.sdk.bean.Message;
import com.smjcco.wxpusher.client.sdk.bean.MessageResult;
import com.smjcco.wxpusher.client.sdk.bean.Result;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OfficialWxPusherProvider implements WxPusherProvider {
    private static final int SUCCESS_CODE = 1000;

    private final WxPusherSdkGateway sdkGateway;

    public OfficialWxPusherProvider(WxPusherSdkGateway sdkGateway) {
        this.sdkGateway = sdkGateway;
    }

    @Override
    public WxPusherResponse send(WxPusherSendRequest request) {
        try {
            Result<List<MessageResult>> result = sdkGateway.send(toSdkMessage(request));
            if (result == null) {
                throw new WxPusherParseException("WxPusher SDK returned null result");
            }
            WxPusherResponse response = toResponse(result);
            if (!Integer.valueOf(SUCCESS_CODE).equals(response.getCode())) {
                throw new WxPusherApiException("WxPusher SDK returned failure code " + response.getCode(),
                        response.getCode(), response.getRawResponse());
            }
            return response;
        } catch (WxPusherApiException | WxPusherParseException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new WxPusherApiException("WxPusher SDK send failed", e);
        }
    }

    private Message toSdkMessage(WxPusherSendRequest request) {
        Message message = new Message();
        message.setContent(request.getContent());
        message.setContentType(request.getContentType());
        message.setSummary(request.getTitle());
        message.setUrl(request.getUrl());
        if (request.getUids() != null && !request.getUids().isEmpty()) {
            message.setUids(new LinkedHashSet<>(request.getUids()));
        }
        if (request.getTopicIds() != null && !request.getTopicIds().isEmpty()) {
            Set<Long> topicIds = new LinkedHashSet<>();
            for (Integer topicId : request.getTopicIds()) {
                if (topicId != null) {
                    topicIds.add(topicId.longValue());
                }
            }
            message.setTopicIds(topicIds);
        }
        return message;
    }

    private WxPusherResponse toResponse(Result<List<MessageResult>> result) {
        WxPusherResponse response = new WxPusherResponse();
        response.setCode(result.getCode());
        response.setMsg(result.getMsg());
        response.setSuccess(result.isSuccess());
        response.setData(toItems(result.getData()));
        return response;
    }

    private List<WxPusherResponse.Item> toItems(List<MessageResult> messageResults) {
        List<WxPusherResponse.Item> items = new ArrayList<>();
        if (messageResults == null) {
            return items;
        }
        for (MessageResult messageResult : messageResults) {
            if (messageResult == null) {
                continue;
            }
            WxPusherResponse.Item item = new WxPusherResponse.Item();
            item.setUid(messageResult.getUid());
            item.setCode(messageResult.getCode());
            item.setStatus(messageResult.getStatus());
            Long messageId = messageResult.getMessageId();
            if (messageId != null && messageId <= Integer.MAX_VALUE) {
                item.setMessageId(messageId.intValue());
            }
            items.add(item);
        }
        return items;
    }
}
