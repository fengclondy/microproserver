package com.ycb.wxxcx.consumer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * Created by zhuhui on 17-6-16.
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RestTemplate restTemplate;


//    @RequestMapping(method = RequestMethod.GET)
//    public String userInfo() {
//        String userInfo = restTemplate.getForObject("http://provider/user", String.class);
//        return userInfo;
//    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam("code") String code,
                        @RequestParam("encryptedData") String encryptedData,
                        @RequestParam("iv") String iv) throws IOException {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("code", code);
        param.add("encryptedData", encryptedData);
        param.add("iv", iv);
        String result = restTemplate.postForObject("http://provider:18080/user/login", param, String.class);
        return result;
    }

    // 获取用户基本信息 用户头像，用户昵称，用户账户余额 (用户中心)
    @RequestMapping(value = "/userInfo", method = RequestMethod.POST)
    public String query(@RequestParam("session") String session) {
        RestTemplate rest = new RestTemplate();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        String result = restTemplate.postForObject("http://provider:18080/user/userInfo", param, String.class);
        return result;
    }

}
