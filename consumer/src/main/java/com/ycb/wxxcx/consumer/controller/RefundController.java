package com.ycb.wxxcx.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;

/**
 * Created by huo on 2017/9/7.
 */
@RestController
@RequestMapping("refund")
public class RefundController {

    @Autowired
    private RestTemplate restTemplate;

    // 获取提现记录列表
    @RequestMapping(value = "/getRefundList", method = RequestMethod.POST)
    public String query(@RequestParam("session") String session) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        String result = restTemplate.postForObject("http://provider:18080/refund/getRefundList", param, String.class);
        return result;
    }

    // 申请提现(微信)
    @RequestMapping(value = "/doRefund", method = RequestMethod.POST)
    public String wechatRefund(@RequestParam("session") String session) throws UnsupportedEncodingException {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        String result = restTemplate.postForObject("http://provider:18080/refund/doRefund", param, String.class);
        return result;
    }
}
