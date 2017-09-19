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
@RequestMapping("borrow")
public class BorrowController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/getMachineInfo", method = RequestMethod.POST)
    public String getMachineInfo(@RequestParam("session") String session,
                                 @RequestParam("qrcode") String qrcode) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("qrcode", qrcode);
        String result = restTemplate.postForObject("http://provider:18080/borrow/getMachineInfo", param, String.class);
        return result;
    }
}
