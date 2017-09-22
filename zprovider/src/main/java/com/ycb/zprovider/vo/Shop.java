package com.ycb.zprovider.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhuhui on 17-7-27.
 */
public class Shop extends BaseEntity{

    // 店铺名
    private String name;

    //所在城市
    private String city;

    //押金
    private BigDecimal defaultPay;

    //可直接支付差额
    private BigDecimal atLeatValue;

    private List<Station> stationList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getDefaultPay(){return defaultPay; }

    public void setDefaultPay(){this.defaultPay = defaultPay; }

    public BigDecimal getAtLeatValue(){return atLeatValue; }

    public void setAtLeatValue(BigDecimal atLeatValue){this.atLeatValue = atLeatValue; }

    public List<Station> getStationList() {
        return stationList;
    }

    public void setStationList(List<Station> stationList) {
        this.stationList = stationList;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
