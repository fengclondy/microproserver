package com.tcb.wxxcx.consumer.controller;

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
@RequestMapping("shop")
public class ShopController {

    @Autowired
    private RestTemplate restTemplate;

    // 获取商铺列表
    @RequestMapping(value = "/getShopList", method = RequestMethod.POST)
    public String query(@RequestParam("session") String session,
                        @RequestParam("latitude") String latitude,
                        @RequestParam("longitude") String longitude) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("latitude", latitude);
        param.add("longitude", longitude);
        String result = restTemplate.postForObject("http://provider:18080/shop/getShopList", param, String.class);
        return result;
    }
}
