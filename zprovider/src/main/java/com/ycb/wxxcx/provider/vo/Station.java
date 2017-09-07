package com.ycb.wxxcx.provider.vo;

/**
 * Created by zhuhui on 17-7-27.
 */
public class Station {
    private Long id;

    private String title;

    private Integer usable;

    private Integer empty;

    private String mac;

    private Integer cable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getUsable() {
        return usable;
    }

    public void setUsable(Integer usable) {
        this.usable = usable;
    }

    public Integer getEmpty() {
        return empty;
    }

    public void setEmpty(Integer empty) {
        this.empty = empty;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Integer getCable() {
        return cable;
    }

    public void setCable(Integer cable) {
        this.cable = cable;
    }
}
