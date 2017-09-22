package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.service.FeeStrategyService;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.TradeLog;
import com.ycb.wxxcx.provider.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    // 获取用户的订单记录
    @RequestMapping(value = "/getOrderList", method = RequestMethod.POST)
    @ResponseBody
    public String query(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String openid = redisService.getKeyValue(session);
            User user = this.userMapper.findUserinfoByOpenid(openid);
            List<TradeLog> tradeLogList = this.orderMapper.findTradeLogs(user.getId());
            for (int i = 0; i < tradeLogList.size(); i++) {
                tradeLogList.get(i).setFeeStrategy(feeStrategyService.transFeeStrategy(tradeLogList.get(i).getFeeStrategyEntity()));
                tradeLogList.get(i).setUseFee(feeStrategyService.calUseFee(tradeLogList.get(i).getFeeStrategyEntity(),
                        tradeLogList.get(i).getDuration(),
                        tradeLogList.get(i).getStatus(),
                        tradeLogList.get(i).getUseFee()));
            }
            if (null != tradeLogList) {
                Map<String, List> data = new HashMap<String, List>();
                data.put("orders", tradeLogList);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("msg", "成功");
            } else {
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

    /**
     * 获取订单状态
     *
     * @param session
     * @param orderid
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/getOrderStatus", method = RequestMethod.POST)
    public String getOrderStatus(@RequestParam("session") String session,
                                 @RequestParam("orderid") String orderid) throws IOException {
        Map<String, Object> bacMap = new HashMap<>();
        try {
            Integer orderStatus = this.orderMapper.getOrderStatus(orderid);
            Map<String, Object> data = new HashMap<String, Object>();
            if (orderStatus == 2) {
                data.put("status", "1");//借出成功
            } else {
                data.put("status", "0");//还未弹出
            }
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
