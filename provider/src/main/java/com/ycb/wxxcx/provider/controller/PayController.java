package com.ycb.wxxcx.provider.controller;

import com.google.common.base.Charsets;
import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.*;
import com.ycb.wxxcx.provider.service.SocketService;
import com.ycb.wxxcx.provider.utils.HttpRequest;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.utils.WXPayUtil;
import com.ycb.wxxcx.provider.utils.XmlUtil;
import com.ycb.wxxcx.provider.vo.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhuhui on 17-8-7.
 */
@RestController
@RequestMapping("wxpay")
public class PayController {

    public static final Logger logger = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private SocketService socketService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopStationMapper shopStationMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @Value("${mch_id}")
    private String mchId;

    @Value("${key}")
    private String key;

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/payment", method = RequestMethod.POST)
    @ResponseBody
    public String payment(@RequestParam("session") String session,
                          @RequestParam("sid") String sid,//设备id
                          @RequestParam("cable_type") String cableType,  //线类型
                          @RequestParam("tid") String tid) {  //标签id
        Map<String, Object> bacMap = new HashMap<>();
        try {
            String openid = redisService.getKeyValue(session);
            User user = userMapper.findUserIdByOpenid(openid);
            if (user.getUsablemoney().compareTo(BigDecimal.valueOf(95)) > 0) {
                // 账户内月额大于95 时，直接使用余额支付

                //创建订单
                String orderid = WXPayUtil.createOrderId();
                createPreOrder(sid, cableType, user, orderid);
                //弹出电池
                String mac = stationMapper.getStationMac(Long.valueOf(sid));
                socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;MAC:" + mac + ";ORDERID:" + orderid + ";COLORID:7;CABLE:" + cableType + ";\r\n");
                //修改用户账户信息，余额，押金修正
                BigDecimal useMoney = user.getUsablemoney();
                if (useMoney.compareTo(BigDecimal.valueOf(100)) > 0) {
                    useMoney = BigDecimal.valueOf(100);
                }
                userMapper.updateUserDepositUsable(useMoney,user.getId());
                Map<String, Object> data = new HashMap<>();
                data.put("paytype", 1);//1账户余额支付
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("errcode", 0);
                bacMap.put("msg", "成功");
            } else {
                // 统一下单，生成预支付交易单
                Map<String, Object> paramMap = createPrepayParam(openid);
                String preOrderInfo = HttpRequest.sendPost(GlobalConfig.WX_UNIFIEDORDER_URL, WXPayUtil.map2Xml(paramMap, key));
                //创建订单
                createPreOrder(sid, cableType, user, paramMap.get("out_trade_no").toString());

                Map<String, Object> prePayMap = new LinkedHashMap<>();
                prePayMap.put("appId", WXPayUtil.getAppId(preOrderInfo));
                prePayMap.put("nonceStr", WXPayUtil.getNonceStr(preOrderInfo));
                prePayMap.put("package", "prepay_id=" + WXPayUtil.getPrepayId(preOrderInfo));
                prePayMap.put("signType", "MD5");
                prePayMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                String paySign = WXPayUtil.getSign(prePayMap, key);
                Map<String, Object> payData = new HashMap<>();
                payData.put("timeStamp", prePayMap.get("timeStamp"));
                payData.put("nonceStr", prePayMap.get("nonceStr"));
                payData.put("package", prePayMap.get("package"));
                payData.put("signType", prePayMap.get("signType"));
                payData.put("paySign", paySign);
                Map<String, Object> data = new HashMap<>();
                data.put("paytype", 0);//微信支付
                data.put("wxpay_params", payData);//微信支付
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("errcode", 0);
                bacMap.put("msg", "成功");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 0);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    /**
     * 创建订单
     *
     * @param sid
     * @param cableType
     * @param user
     * @param orderid
     */
    private void createPreOrder(@RequestParam("sid") String sid, @RequestParam("cable_type") String cableType, User user, String orderid) {
        Station station = stationMapper.getStationBySid(sid);
        Shop shop = shopMapper.getShopInfoBySid(sid);
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(sid);
        Order order = new Order();
        order.setCreatedBy("SYS:prepay");
        order.setCreatedDate(new Date());
        order.setBorrow_station_name(station.getTitle());
        order.setBorrow_time(order.getCreatedDate());
        order.setOrderid(orderid);//订单编号
        order.setPlatform(3);//平台(小程序)
        order.setPrice(BigDecimal.valueOf(100));//商品价格(元)
        order.setPaid(BigDecimal.ZERO);//已支付的费用
        order.setUsefee(BigDecimal.ZERO);//产生的费用
        order.setCable(Integer.valueOf(cableType));
        order.setStatus(0);//未支付状态
        order.setCustomer(user.getId());//用户id
        order.setBorrow_shop_id(shop.getId());
        order.setBorrow_shop_station_id(shopStation.getId());
        order.setBorrow_station_id(station.getId());
        orderMapper.saveOrder(order);
    }

    /**
     * 微信支付参数
     *
     * @param openid
     * @return
     */
    private Map<String, Object> createPrepayParam(String openid) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("appid", appID);
        paramMap.put("attach", "attach");
        paramMap.put("body", "云充吧驿站");
        paramMap.put("goods_tag", "notag");
        paramMap.put("mch_id", mchId);
        paramMap.put("nonce_str", WXPayUtil.getNonce_str());
        paramMap.put("notify_url", GlobalConfig.NOTIFY_URL);
        paramMap.put("openid", openid);
        String out_trade_no = WXPayUtil.createOrderId();
        paramMap.put("out_trade_no", out_trade_no);
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        paramMap.put("spbill_create_ip", remoteAddr);
        // paramMap.put("time_expire", "20170808160434");
        // paramMap.put("time_start", "20170808155434");
        paramMap.put("total_fee", 1);//费用 TODO
        paramMap.put("trade_type", "JSAPI");
        return paramMap;
    }


    @RequestMapping(value = "/payNotify", method = {RequestMethod.GET, RequestMethod.POST})
    public String payNotify(HttpServletRequest request) {
        try {
            String responseStr = parseWeixinCallback(request);
            Map<String, Object> map = XmlUtil.doXMLParse(responseStr);
            // 校验签名 防止数据泄漏导致出现“假通知”，造成资金损失
            if (!WXPayUtil.checkIsSignValidFromResponseString(responseStr, key)) {
                logger.error("微信回调失败,签名可能被篡改");
                return WXPayUtil.setXML("FAIL", "invalid sign");
            }
            if ("FAIL".equalsIgnoreCase(map.get("result_code").toString())) {
                logger.error("微信回调失败");
                return WXPayUtil.setXML("FAIL", "weixin pay fail");
            }
            if ("SUCCESS".equalsIgnoreCase(map.get("result_code").toString())) {
                //获取应用服务器需要的数据进行持久化操作
                String outTradeNo = (String) map.get("out_trade_no");
                String totlaFee = (String) map.get("total_fee");
                Long paid = Long.valueOf(totlaFee);

                //根据订单查询MAC和CABLE
                Station station = stationMapper.getMacCableByOrderid(outTradeNo);
                socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;MAC:" + station.getMac() + ";ORDERID:" + outTradeNo + ";COLORID:7;CABLE:" + station.getCable() + ";\r\n");
                //修改订单状态为1，已支付
                Order order = new Order();
                order.setLastModifiedBy("SYS:pay");
                order.setLastModifiedDate(new Date());
                order.setStatus(1);//订单状态改为1，已支付
                order.setPaid(BigDecimal.valueOf(paid).divide(BigDecimal.valueOf(100), 2)); //已支付的费用 分转换元
                order.setOrderid(outTradeNo);
                orderMapper.updateOrderStatus(order);

                //更新用户账户信息，押金
                userMapper.updateUserDeposit(BigDecimal.valueOf(paid).divide(BigDecimal.valueOf(100), 2), order.getCustomer());

                boolean isOk = true;
                // 告诉微信服务器，我收到信息了，不要在调用回调action了
                if (isOk) {
                    return WXPayUtil.setXML("SUCCESS", "OK");
                } else {
                    return WXPayUtil.setXML("FAIL", "pay fail");
                }
            }
        } catch (Exception e) {
            logger.debug("支付失败" + e.getMessage());
            return WXPayUtil.setXML("FAIL", "weixin pay server exception");
        }
        return WXPayUtil.setXML("FAIL", "weixin pay fail");
    }

    /**
     * 解析微信回调参数
     *
     * @param request
     * @return
     * @throws IOException
     */
    private String parseWeixinCallback(HttpServletRequest request) throws IOException {
        // 获取微信调用我们notify_url的返回信息
        String result = "";
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            result = new String(outSteam.toByteArray(), Charsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outSteam != null) {
                    outSteam.close();
                    outSteam = null; // help GC
                }
                if (inStream != null) {
                    inStream.close();
                    inStream = null;// help GC
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
