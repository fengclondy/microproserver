package com.ycb.zprovider.vo;

import java.math.BigDecimal;

/**
 * Created by duxinyuan on 17-8-22. 用户中心接口实体类
 */
public class UserInfoVo {

    //用户编号
    private Long id;

    //用户昵称
    private String nickname;

    //账户余额
    private BigDecimal usablemoney;

    //头像地址
    private String headimgurl;

    //用户押金
    private BigDecimal deposit;

    //提现金额
    private BigDecimal refund;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public BigDecimal getUsablemoney() {
        return usablemoney;
    }

    public void setUsablemoney(BigDecimal usablemoney) {
        this.usablemoney = usablemoney;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public void setRefund(BigDecimal refund) {
        this.refund = refund;
    }
}
