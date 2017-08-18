package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.TradeLog;
import com.ycb.wxxcx.provider.vo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuhui on 17-6-19.
 */
@RestController
@RequestMapping("order")
public class OrderController {

    public static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private OrderMapper orderMapper;

    // 获取用户的订单记录
    @RequestMapping(value = "/getOrderList", method = RequestMethod.POST)
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
            List<TradeLog> tradeLogList =  this.orderMapper.findTradeLogs(user.getId());
            if (null != tradeLogList){
                Map<String, List> data = new HashMap<String, List>();
                data.put("orders", tradeLogList);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            }else {
                bacMap.put("data", null);
                bacMap.put("code", 1);
                bacMap.put("msg", "用户暂无租借记录");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取数据失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    // 用户扫码下单
    @RequestMapping(value = "/borrowBattery", method = RequestMethod.GET)
    public String borrowBattery() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8000);
            //向服务器端发送数据
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
            out.write("ACT:rent_confirm;STATUS:1;ORDERID:xxxx;ID:xxxx;STATIONID:xxxx;COLORID:xxxx;POWER:xxx;ISDAMAGE:xxxx;VOLTAGE:xxxx;ADAPTER:xxxx;CABLE:xxxx;SLOT:xxxx;TEMPERATURE:xxxx;BATT_TYPE:xxxx\r\n");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
        return null;
    }
    // 用户充电宝报失 根据用户id 更新订单状态
}
