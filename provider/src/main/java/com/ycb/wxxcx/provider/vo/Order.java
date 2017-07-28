package com.ycb.wxxcx.provider.vo;

import java.sql.Date;

/**
 * Created by zhuhui on 17-6-19.
 */
public class Order {
    // 订单编号
    private String orderSeq;
    // 订单状态
    private String orderStatus;
    // 租借地点
    private Long locationId;
    // 租借时间
    private Date orderTime;
    // 收费标准
    private Long feeId;
    // 用户
    private User id;

    // 订单状态
    private enum OrderStatusEnum{
        // 使用中 Using
        U,
        // 已完成
        D,
        // 报失 Lost
        L
    }

}
