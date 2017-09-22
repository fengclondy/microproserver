package com.ycb.wxxcx.provider.vo;

import com.ycb.wxxcx.provider.utils.TimeUtil;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Created by duxinyuan on 17-6-19.交易記錄   問題：收費策略，時間date
 */
public class TradeLog {

    // 订单编号
    private String orderid;
    // 订单状态
    private Integer status;
    // 租借地点
    private String borrowName = "";
    // 租借时间
    private String borrowTime = "";   //原来是date
    // 收费策略
    private String feeStrategy = "";
    // 归还地点
    private String returnName = "";
    //归还时间
    private String returnTime = "";   //原来是date
    //租借时长
    private String lastTime = "";  //原来是long
    // 支付金额
    private BigDecimal paid;
    //产生费用
    private BigDecimal usefee;
    //用来接从数据库查出来的时长
    private Long duration;
    //收费策略
    private FeeStrategy feeStrategyEntity;

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBorrowName() {
        return borrowName;
    }

    public void setBorrowName(String borrowName) {
        this.borrowName = borrowName;
    }

    public String getBorrowTime() {
        return StringUtils.isEmpty(borrowTime) ? "" : borrowTime.substring(0, borrowTime.length() - 2);
    }

    public void setBorrowTime(String borrowTime) {
        this.borrowTime = borrowTime;
    }

    public String getFeeStrategy() {
        return feeStrategy;
    }

    public void setFeeStrategy(String feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    public String getReturnName() {
        return returnName;
    }

    public void setReturnName(String returnName) {
        this.returnName = returnName;
    }

    public String getReturnTime() {
        return StringUtils.isEmpty(returnTime) ? "" : returnTime.substring(0, returnTime.length() - 2);
    }

    public void setReturnTime(String returnTime) {
        this.returnTime = returnTime;
    }

    public String getLastTime() {
        String returnTime = this.getReturnTime();
        String borrowTime = this.getBorrowTime();
        Long duration = this.getDuration();
        return TimeUtil.calLastTime(returnTime, borrowTime, duration);
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getUseFee() {
        return usefee;
    }

    public void setUseFee(BigDecimal usefee) {
        this.usefee = usefee;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public FeeStrategy getFeeStrategyEntity() {
        return feeStrategyEntity;
    }

    public void setFeeStrategyEntity(FeeStrategy feeStrategyEntity) {
        this.feeStrategyEntity = feeStrategyEntity;
    }
}
