package com.ycb.wxxcx.provider.vo;

import java.util.List;

/**
 * Created by zhuhui on 17-7-28.
 */
public class ShopStation extends BaseEntity{

    // 商铺站点名
    private String tile;

    // 地址
    private String address;

    // 经度
    private String longitude;

    // 纬度
    private String latitude;

    // 设备
    private List<Station> stationList;

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
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

    public List<Station> getStationList() {
        return stationList;
    }

    public void setStationList(List<Station> stationList) {
        this.stationList = stationList;
    }
}
