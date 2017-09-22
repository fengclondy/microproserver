package com.ycb.wxxcx.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * Created by huo on 2017/9/7.
 */
@RestController
@RequestMapping("wxpay")
public class PayController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/payment", method = RequestMethod.POST)
    @ResponseBody
    public String payment(@RequestParam("session") String session,
                          @RequestParam("sid") String sid,//设备id
                          @RequestParam("cable_type") String cableType,  //线类型
                          @RequestParam("tid") String tid) {  //标签id
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("sid", sid);
        param.add("cable_type", cableType);
        param.add("tid", tid);
        String result = restTemplate.postForObject("http://provider:18080/wxpay/payment", param, String.class);
        return result;
    }
}
