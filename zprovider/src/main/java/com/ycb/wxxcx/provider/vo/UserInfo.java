package com.ycb.wxxcx.provider.vo;

import java.util.Date;

/**
 * Created by duxinyuan on 11-8-16.
 */
public class UserInfo extends BaseEntity {

    //@MetaData("用户openid")
    private String openid;

    //@MetaData("昵称")
    private String nickname;

    //@MetaData("性别")
    private Integer sex;

    //@MetaData("城市")
    private String city;

    //@MetaData("省份")
    private String province;

    //@MetaData("国家")
    private String country;

    //@MetaData("头像")
    private String headimgurl;

    //@MetaData("语言")
    private String language;

    //@MetaData("关注时间")
    private Date subscribeTime;

    //@MetaData("unionid")
    private String unionid;

    //@MetaData("remark")
    private String remark;

    //@MetaData("组")
    private String groupid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getSubscribeTime() {
        return subscribeTime;
    }

    public void setSubscribeTime(Date subscribeTime) {
        this.subscribeTime = subscribeTime;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }
}
