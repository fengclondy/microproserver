package com.ycb.zprovider.vo;

import java.util.List;

/**
 * Created by zhuhui on 17-7-28.
 */
public class ShopStation extends BaseEntity {

    // 商铺站点名
    private String name;

    // 地址
    private String address;

    // 经度
    private String longitude;

    // 纬度
    private String latitude;

    // 人均消费
    private String cost;

    // 电话号码
    private String phone;

    // 开始营业时间
    private String stime;

    // 结束营业时间
    private String etime;

    // logo图片地址
    private String logo;

    //收费策略
    private FeeStrategy feeStrategy;

    // 设备
    private List<Station> shopStation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public List<Station> getShopStation() {
        return shopStation;
    }

    public void setShopStation(List<Station> shopStation) {
        this.shopStation = shopStation;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public FeeStrategy getFeeStrategy() {
        return feeStrategy;
    }

    public void setFeeStrategy(FeeStrategy feeStrategy) {
        this.feeStrategy = feeStrategy;
    }
}
