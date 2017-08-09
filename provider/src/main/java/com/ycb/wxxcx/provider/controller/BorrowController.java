package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.StationMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-8-7.
 */
@RestController
@RequestMapping("borrow")
public class BorrowController {

    public static final Logger logger = LoggerFactory.getLogger(BorrowController.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/getMachineInfo", method = RequestMethod.POST)
    @ResponseBody
    public String getMachineInfo(@RequestParam("session") String session,
                                 @RequestParam("qrcode") String qrcode) {
        // 解析qrcode，根据机器sid，获取机器状态属性值
        String[] urlArr = qrcode.trim().toLowerCase().split("/");
        String sid = urlArr[urlArr.length - 1];
        String cable_type = stationMapper.getUsableBatteries(Long.valueOf(sid));
        User user = this.userMapper.findUserinfoByOpenid(redisService.getKeyValue(session));

        Map<String, Object> bacMap = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("sid", sid);
            data.put("tid", session);
            data.put("deposite_need", 0.5);//TODO
            data.put("usable_money", user.getUsablemoney());
            data.put("fee_strategy", session);
            data.put("cable_type", cable_type);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 0);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }
}
