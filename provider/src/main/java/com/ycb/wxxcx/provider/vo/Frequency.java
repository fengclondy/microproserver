package com.ycb.wxxcx.provider.vo;

/**
 * Created by duxinyuan on 17-9-1. 租借限制
 */
public class Frequency {

    // 每日最高同时使用块数
    private Integer batteryNum;
    // 每日最高订单数量
    private Integer orderNum;

    public Integer getBatteryNum() {
        return batteryNum;
    }

    public void setBatteryNum(Integer batteryNum) {
        this.batteryNum = batteryNum;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
