package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.MessageMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by duxinyuan on 17-8-28.
 */
@RestController
@RequestMapping("formid")
public class FormIdController {

    public static final Logger logger = LoggerFactory.getLogger(FormIdController.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageMapper messageMapper;

    //接收form_id接口
    @RequestMapping(value = "/submitFormId", method = RequestMethod.POST)
    @ResponseBody
    public String submitFormId(@RequestParam("session") String session,
                        @RequestParam("form_id") String formid) {

        Map<String, Object> bacMap = new HashMap<>();
        if (StringUtils.isEmpty(session) || StringUtils.isEmpty(formid)){
            bacMap.put("form_id", null);
            bacMap.put("code", 2);
            bacMap.put("msg", "参数有误");
            return JsonUtils.writeValueAsString(bacMap);
        }
        try {
            String openid = redisService.getKeyValue(session);
            if (!StringUtils.isEmpty(openid)){
                //持久化form_id
                Message message = new Message();
                message.setOpenid(openid);
                message.setFormId(formid);
                message.setType(1); //form_id
                message.setNumber(1);//使用次数初始化为1
                message.setCreatedBy("SYS:message");
                this.messageMapper.insertFormId(message);

                bacMap.put("form_id", formid);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            }else {
                bacMap.put("form_id", null);
                bacMap.put("code", 1);
                bacMap.put("msg", "openid有误");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("form_id", null);
            bacMap.put("code", 3);
            bacMap.put("msg", "系统异常");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
