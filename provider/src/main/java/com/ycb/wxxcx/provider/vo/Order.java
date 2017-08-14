package com.ycb.wxxcx.provider.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhuhui on 17-6-19.
 */
public class Order extends BaseEntity{
    // 订单编号
    private String orderid;
    // 订单状态
    private Integer status;
    //租借城市
    private String borrow_city;
    //租借站点名称
    private String borrow_station_name;
    //租借时间
    private Date borrow_time;
    //付清的
    private BigDecimal paid = BigDecimal.ZERO;
    //平台
    private Integer platform;
    //价格
    private BigDecimal price = BigDecimal.ZERO;
    //已退还
    private BigDecimal refunded = BigDecimal.ZERO;
    //退还城市
    private String return_city;
    //退还站点名称
    private String return_station_name;
    //退还时间
    private Date return_time;
    //使用费用
    private BigDecimal usefee = BigDecimal.ZERO;
    // 用户id
    private Long customer;
    //借出商铺id
    private Long borrow_shop_id;
    //借出商铺站点id
    private Long borrow_shop_station_id;
    //借出站点id;
    private Long borrow_station_id;
    //归还商铺id
    private Long return_shop_id;
    //归还商铺站点id
    private Long return_shop_station_id;
    //归还站点id
    private Long return_station_id;

      // 租借地点
//    private Long locationId;
//    // 租借时间
//    private Date orderTime;
//    // 收费标准
//    private Long feeId;

    // 订单状态
//    private enum OrderStatusEnum{
//        // 使用中 Using
//        U,
//        // 已完成
//        D,
//        // 报失 Lost
//        L
//    }

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

    public String getBorrow_city() {
        return borrow_city;
    }

    public void setBorrow_city(String borrow_city) {
        this.borrow_city = borrow_city;
    }

    public String getBorrow_station_name() {
        return borrow_station_name;
    }

    public void setBorrow_station_name(String borrow_station_name) {
        this.borrow_station_name = borrow_station_name;
    }

    public Date getBorrow_time() {
        return borrow_time;
    }

    public void setBorrow_time(Date borrow_time) {
        this.borrow_time = borrow_time;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getRefunded() {
        return refunded;
    }

    public void setRefunded(BigDecimal refunded) {
        this.refunded = refunded;
    }

    public String getReturn_city() {
        return return_city;
    }

    public void setReturn_city(String return_city) {
        this.return_city = return_city;
    }

    public String getReturn_station_name() {
        return return_station_name;
    }

    public void setReturn_station_name(String return_station_name) {
        this.return_station_name = return_station_name;
    }

    public Date getReturn_time() {
        return return_time;
    }

    public void setReturn_time(Date return_time) {
        this.return_time = return_time;
    }

    public BigDecimal getUsefee() {
        return usefee;
    }

    public void setUsefee(BigDecimal usefee) {
        this.usefee = usefee;
    }

    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    public Long getBorrow_shop_id() {
        return borrow_shop_id;
    }

    public void setBorrow_shop_id(Long borrow_shop_id) {
        this.borrow_shop_id = borrow_shop_id;
    }

    public Long getBorrow_shop_station_id() {
        return borrow_shop_station_id;
    }

    public void setBorrow_shop_station_id(Long borrow_shop_station_id) {
        this.borrow_shop_station_id = borrow_shop_station_id;
    }

    public Long getBorrow_station_id() {
        return borrow_station_id;
    }

    public void setBorrow_station_id(Long borrow_station_id) {
        this.borrow_station_id = borrow_station_id;
    }

    public Long getReturn_shop_id() {
        return return_shop_id;
    }

    public void setReturn_shop_id(Long return_shop_id) {
        this.return_shop_id = return_shop_id;
    }

    public Long getReturn_shop_station_id() {
        return return_shop_station_id;
    }

    public void setReturn_shop_station_id(Long return_shop_station_id) {
        this.return_shop_station_id = return_shop_station_id;
    }

    public Long getReturn_station_id() {
        return return_station_id;
    }

    public void setReturn_station_id(Long return_station_id) {
        this.return_station_id = return_station_id;
    }
}
