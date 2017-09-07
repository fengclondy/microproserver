package com.ycb.wxxcx.provider.vo;

import java.util.List;

/**
 * Created by zhuhui on 17-7-27.
 */
public class Shop extends BaseEntity{

    // 店铺名
    private String name;

    //所在城市
    private String city;

    private List<Station> stationList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
