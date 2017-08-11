package com.ycb.wxxcx.provider.vo;

import java.math.BigDecimal;

/**
 * Created by duxinyuan on 11-8-16.
 */
public class UserInfo{
    //用户id
    private String id;
    //用户昵称
    private String nickname;
    // 账户余额
    private BigDecimal usablemoney = BigDecimal.ZERO;
    //头像地址
    private String headimgurl;
    // 用户押金
    private BigDecimal deposit = BigDecimal.ZERO;
    //提现金额
    private BigDecimal refund = BigDecimal.ZERO;

    public BigDecimal getUsablemoney() {
        return usablemoney;
    }

    public void setUsablemoney(BigDecimal usablemoney) {
        this.usablemoney = usablemoney;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
