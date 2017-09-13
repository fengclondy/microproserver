package com.tcb.wxxcx.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
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

    // 获取商铺列表
    @RequestMapping(value = "/getShopInfo", method = RequestMethod.POST)
    @ResponseBody
    // @Action --修改 为拦截器方式实现
    public String query(@RequestParam("session") String session,
                        @RequestParam("shop_id") Long shopid) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("shop_id", shopid);
        String result = restTemplate.postForObject("http://provider:18080/shop/getShopInfo", param, String.class);
        return result;
    }
}
