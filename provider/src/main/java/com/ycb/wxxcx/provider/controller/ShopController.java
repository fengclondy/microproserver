package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.mapper.ShopMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuhui on 17-7-26.
 */
@RestController
@RequestMapping("shop")
public class ShopController {

    public static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopMapper shopMapper;

    // 获取商铺列表
    @RequestMapping(value = "/getShopList", method = RequestMethod.POST)
    @ResponseBody
    // @Action --修改 为拦截器方式实现
    public String query(@RequestParam("session") String session,
                        @RequestParam("latitude") String latitude,
                        @RequestParam("longitude") String longitude) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            List<Shop> shopList =  this.shopMapper.findShops(latitude, longitude);
            Map<String, Object> data = new HashMap<>();
            data.put("shops", shopList);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");
            // 根据openid检索数据库，不存在新建用户
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 0);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

}