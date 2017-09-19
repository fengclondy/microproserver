package com.ycb.zprovider.vo;

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
    private String borrowCity;
    //租借站点名称
    private String borrowStationName;
    //租借时间
    private Date borrowTime;
    //平台
    private Integer platform;
    //已付款
    private BigDecimal paid = BigDecimal.ZERO;
    //使用费用
    private BigDecimal usefee = BigDecimal.ZERO;
    //订单价格
    private BigDecimal price = BigDecimal.ZERO;
    //已退款
    private BigDecimal refunded = BigDecimal.ZERO;
    //租借电池线类型
    private Integer cable;
    //退还城市
    private String returnCity;
    //退还站点名称
    private String returnStationName;
    //退还时间
    private Date returnTime;
    // 用户id
    private Long customer;
    //借出商铺id
    private Long borrowShopId;
    //借出商铺站点id
    private Long borrowShopStationId;
    //借出站点id;
    private Long borrowStationId;
    //归还商铺id
    private Long returnShopId;
    //归还商铺站点id
    private Long returnShopStationId;
    //归还站点id
    private Long returnStationId;

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
    //信用借还的订单号
    private String orderNo;
    //资金流水号，用于商户与支付宝进行对账
    private String alipayFundOrderNo;

    public String getOrderNo(){ return orderNo; }

    public void setOrderNo(String orderNo){ this.orderNo = orderNo; }

    public String getAlipayFundOrderNo(){ return alipayFundOrderNo; }

    public void setAlipayFundOrderNo(String alipayFundOrderNo){ this.alipayFundOrderNo = alipayFundOrderNo; }

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

    public String getBorrowCity() {
        return borrowCity;
    }

    public void setBorrowCity(String borrowCity) {
        this.borrowCity = borrowCity;
    }

    public String getBorrowStationName() {
        return borrowStationName;
    }

    public void setBorrowStationName(String borrowStationName) {
        this.borrowStationName = borrowStationName;
    }

    public Date getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(Date borrowTime) {
        this.borrowTime = borrowTime;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getUsefee() {
        return usefee;
    }

    public void setUsefee(BigDecimal usefee) {
        this.usefee = usefee;
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

    public Integer getCable() {
        return cable;
    }

    public void setCable(Integer cable) {
        this.cable = cable;
    }

    public String getReturnCity() {
        return returnCity;
    }

    public void setReturnCity(String returnCity) {
        this.returnCity = returnCity;
    }

    public String getReturnStationName() {
        return returnStationName;
    }

    public void setReturnStationName(String returnStationName) {
        this.returnStationName = returnStationName;
    }

    public Date getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(Date returnTime) {
        this.returnTime = returnTime;
    }

    public Long getCustomer() {
        return customer;
    }

    public void setCustomer(Long customer) {
        this.customer = customer;
    }

    public Long getBorrowShopId() {
        return borrowShopId;
    }

    public void setBorrowShopId(Long borrowShopId) {
        this.borrowShopId = borrowShopId;
    }

    public Long getBorrowShopStationId() {
        return borrowShopStationId;
    }

    public void setBorrowShopStationId(Long borrowShopStationId) {
        this.borrowShopStationId = borrowShopStationId;
    }

    public Long getBorrowStationId() {
        return borrowStationId;
    }

    public void setBorrowStationId(Long borrowStationId) {
        this.borrowStationId = borrowStationId;
    }

    public Long getReturnShopId() {
        return returnShopId;
    }

    public void setReturnShopId(Long returnShopId) {
        this.returnShopId = returnShopId;
    }

    public Long getReturnShopStationId() {
        return returnShopStationId;
    }

    public void setReturnShopStationId(Long returnShopStationId) {
        this.returnShopStationId = returnShopStationId;
    }

    public Long getReturnStationId() {
        return returnStationId;
    }

    public void setReturnStationId(Long returnStationId) {
        this.returnStationId = returnStationId;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderid='" + orderid + '\'' +
                ", status=" + status +
                ", borrowCity='" + borrowCity + '\'' +
                ", borrowStationName='" + borrowStationName + '\'' +
                ", borrowTime=" + borrowTime +
                ", platform=" + platform +
                ", paid=" + paid +
                ", usefee=" + usefee +
                ", price=" + price +
                ", refunded=" + refunded +
                ", cable=" + cable +
                ", returnCity='" + returnCity + '\'' +
                ", returnStationName='" + returnStationName + '\'' +
                ", returnTime=" + returnTime +
                ", customer=" + customer +
                ", borrowShopId=" + borrowShopId +
                ", borrowShopStationId=" + borrowShopStationId +
                ", borrowStationId=" + borrowStationId +
                ", returnShopId=" + returnShopId +
                ", returnShopStationId=" + returnShopStationId +
                ", returnStationId=" + returnStationId +
                ", orderNo='" + orderNo + '\'' +
                ", alipayFundOrderNo='" + alipayFundOrderNo + '\'' +
                '}';
    }
}
