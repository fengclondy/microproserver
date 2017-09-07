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
@RequestMapping("formid")
public class FormIdController {

    @Autowired
    private RestTemplate restTemplate;

    //接收form_id接口
    @RequestMapping(value = "/submitFormId", method = RequestMethod.POST)
    public String submitFormId(@RequestParam("session") String session,
                               @RequestParam("form_id") String formid) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap();
        param.add("session", session);
        param.add("form_id", formid);
        String result = restTemplate.postForObject("http://provider:18080/formid/submitFormId", param, String.class);
        return result;
    }
}
