package com.ycb.wxxcx.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by huo on 2017/9/7.
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    // 获取用户的订单记录
    @RequestMapping(value = "/getOrderList", method = RequestMethod.POST)
    public String query(@RequestParam("session") String session) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        String result = restTemplate.postForObject("http://provider:18080/order/getOrderList", param, String.class);
        return result;
    }

    /**
     * 获取订单状态
     */
    @RequestMapping(value = "/getOrderStatus", method = RequestMethod.POST)
    public String getOrderStatus(@RequestParam("session") String session,
                                 @RequestParam("orderid") String orderid) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("orderid", orderid);
        String result = restTemplate.postForObject("http://provider:18080/order/getOrderStatus", param, String.class);
        return result;
    }
}
