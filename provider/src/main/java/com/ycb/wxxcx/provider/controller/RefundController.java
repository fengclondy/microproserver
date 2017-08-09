package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.RefundMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.Refund;
import com.ycb.wxxcx.provider.vo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 杜欣源:退款记录 on 2017/8/5.
 */

@RestController
@RequestMapping("refund")
public class RefundController {

    public static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    @Autowired(required = false)
    private RefundMapper refundMapper;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private UserMapper userMapper;

    // 获取提现记录列表
    @RequestMapping(value = "/getRefundList", method = RequestMethod.POST)
    @ResponseBody
    public String query(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        if (StringUtils.isEmpty(session)){
            bacMap.put("data", null);
            bacMap.put("code", 2);
            bacMap.put("msg", "失败(session不可为空)");

            return JsonUtils.writeValueAsString(bacMap);
        }
        try {
            String openid = redisService.getKeyValue(session);
            User user = this.userMapper.findUserinfoByOpenid(openid);

            List<Refund> refundList =  this.refundMapper.findRefunds(user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("refunds", refundList);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");

        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取数据失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

}
