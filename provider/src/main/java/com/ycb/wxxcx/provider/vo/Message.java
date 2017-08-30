package com.ycb.wxxcx.provider.vo;

/**
 * Created by duxinyuan on 2017/8/28.
 */
public class Message extends BaseEntity{

    //凭证有效期，单位：秒
    private Integer expiresIn;
    //接口访问凭证
    private String accessToken;
    //formid
    private String formId;
    //openid
    private String openid;
    //prepay_id
    private String prepayId;
    //orderid
    private String orderid;
    //type为1是form_id, 为2是prepay_id
    private Integer type;
    //用来记录prepay_id的使用次数
    private Integer number = 0;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public void setPrepayId(String prepayId) {
        this.prepayId = prepayId;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }
}
