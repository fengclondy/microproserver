package com.ycb.wxxcx.provider.vo;

import java.math.BigDecimal;

import java.util.Date;

/**
 * Created by 杜欣源 on 2017/8/5.
 */
public class Refund extends BaseEntity{

    //用户编号
    private Long uid;

    //提现金额
    private BigDecimal refund;

    //提现状态:1为退款申请、 2为退款完成
    private Integer status;

    //发起时间
    private Date requestTime;

    //提现时间
    private Date refundTime;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }
}
