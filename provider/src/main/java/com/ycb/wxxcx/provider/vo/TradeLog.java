package com.ycb.wxxcx.provider.vo;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by duxinyuan on 17-6-19.交易記錄   問題：收費策略，時間date
 */
public class TradeLog {

    // 订单编号
    private String orderid;
    // 订单状态
    private Integer status;
    // 租借地点
    private String borrowName;
    // 租借时间
    private Date orderTime;
    // 收费策略
    private String feeStr;
    // 归还地点
    private String returnName;
    //归还时间
    private Date returnTime;
    //租借时长 （毫秒）
    private Long lastTime;
    //产生费用
    private BigDecimal usefee;

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

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getFeeStr() {
        return feeStr;
    }

    public void setFeeStr(String feeStr) {
        this.feeStr = feeStr;
    }

    public String getReturnName() {
        return returnName;
    }

    public void setReturnName(String returnName) {
        this.returnName = returnName;
    }

    public Date getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
    }

    public BigDecimal getUsefee() {
        return usefee;
    }

    public void setUsefee(BigDecimal usefee) {
        this.usefee = usefee;
    }

    public Long getLastTime() {
        //借出时间和归还时间的时间差 单位：毫秒
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long lastTime = this.getReturnTime().getTime()-this.getOrderTime().getTime();
        //Long lastTime = between/1000/60/60;
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public String toString() {
        return "TradeLog{" +
                "orderid='" + orderid + '\'' +
                ", status=" + status +
                ", borrowName='" + borrowName + '\'' +
                ", orderTime=" + orderTime +
                ", feeStr='" + feeStr + '\'' +
                ", returnName='" + returnName + '\'' +
                ", returnTime=" + returnTime +
                ", lastTime=" + lastTime +
                ", usefee=" + usefee +
                '}';
    }
}
