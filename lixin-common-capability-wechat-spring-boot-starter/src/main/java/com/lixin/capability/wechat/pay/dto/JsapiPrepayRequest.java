package com.lixin.capability.wechat.pay.dto;

public class JsapiPrepayRequest {
    private String description;
    private String outTradeNo;
    private Integer amountTotal;
    private String currency;
    private String payerOpenId;
    private String notifyUrl;
    private String attach;
    private String timeExpire;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public Integer getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(Integer amountTotal) {
        this.amountTotal = amountTotal;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayerOpenId() {
        return payerOpenId;
    }

    public void setPayerOpenId(String payerOpenId) {
        this.payerOpenId = payerOpenId;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getTimeExpire() {
        return timeExpire;
    }

    public void setTimeExpire(String timeExpire) {
        this.timeExpire = timeExpire;
    }

}
