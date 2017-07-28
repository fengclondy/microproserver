package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.vo.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * Created by zhuhui on 17-6-19.
 */
public class OrderController {
    // 获取用户的订单记录 {user_id}
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public List<Order> query(@PathVariable Long id) {
        return null;
    }
    // 用户扫码下单
    // 用户充电宝报失 根据用户id 更新订单状态
}
