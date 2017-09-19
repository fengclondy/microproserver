package com.ycb.zprovider.vo;

import java.math.BigDecimal;

/**
 * Created by zhuhui on 17-6-16.
 */
public class User extends BaseEntity {

    private String openid;

    // 用户所属平台("0:微信-公众号 1:支付宝 2:芝麻信用 3:微信-小程序")
    private Integer platform = 0;

    // 账户余额
    private BigDecimal usablemoney = BigDecimal.ZERO;

    // 押金
    private BigDecimal deposit = BigDecimal.ZERO;

    // 待退款金额
    private BigDecimal refund = BigDecimal.ZERO;

    // 已退款金额
    private BigDecimal refunded = BigDecimal.ZERO;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

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

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }

    public BigDecimal getRefunded() {
        return refunded;
    }

    public void setRefunded(BigDecimal refunded) {
        this.refunded = refunded;
    }
}
