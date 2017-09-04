package com.ycb.wxxcx.provider.controller;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private MessageMapper messageMapper;

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

    @Value("${defaultPay}")
    private BigDecimal defaultPay;

    @Value("${notifyUrl}")
    public String notifyUrl;

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
            if (defaultPay.subtract(user.getUsablemoney()).compareTo(BigDecimal.ZERO) <= 0) {
                // need pay 为0时，直接使用余额支付
                //创建订单
                String orderid = WXPayUtil.createOrderId();
                createPreOrder(sid, cableType, user, orderid, 11);
                //修改用户账户信息，余额，押金修正
                userMapper.updateUserDepositUsable(defaultPay, user.getId());
                Map<String, Object> data = new HashMap<>();
                data.put("pay_type", 1);//1账户余额支付
                data.put("orderid", orderid);
                bacMap.put("data", data);
                bacMap.put("code", 0);
                bacMap.put("errcode", 0);
                bacMap.put("msg", "成功");
                //弹出电池
                String mac = stationMapper.getStationMac(Long.valueOf(sid));
                socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + sid + ";MAC:" + mac + ";ORDERID:" + orderid + ";COLORID:7;CABLE:" + cableType + ";\r\n");
            } else {
                // 统一下单，生成预支付交易单
                Map<String, Object> paramMap = createPrepayParam(openid, user.getUsablemoney());
                String preOrderInfo = HttpRequest.sendPost(GlobalConfig.WX_UNIFIEDORDER_URL, WXPayUtil.map2Xml(paramMap, key));
                //创建订单
                createPreOrder(sid, cableType, user, paramMap.get("out_trade_no").toString(), 0);
                Map<String, Object> prePayMap = new LinkedHashMap<>();
                prePayMap.put("appId", WXPayUtil.getAppId(preOrderInfo));
                prePayMap.put("nonceStr", WXPayUtil.getNonceStr(preOrderInfo));
                String prepayId = WXPayUtil.getPrepayId(preOrderInfo);
                prePayMap.put("package", "prepay_id=" + prepayId);
                prePayMap.put("signType", "MD5");
                prePayMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
                String paySign = WXPayUtil.getSign(prePayMap, key);
                Map<String, Object> payData = new HashMap<>();
                payData.put("timeStamp", prePayMap.get("timeStamp"));
                payData.put("nonceStr", prePayMap.get("nonceStr"));
                payData.put("package", prePayMap.get("package"));
                payData.put("signType", prePayMap.get("signType"));
                payData.put("paySign", paySign);
                bacMap.put("pay_type", 0);//微信支付
                bacMap.put("orderid", paramMap.get("out_trade_no").toString());
                bacMap.put("wxpay_params", payData);//微信支付
                bacMap.put("code", 0);
                bacMap.put("errcode", 0);
                bacMap.put("msg", "成功");

                //保存prepay_id
                Message message = new Message();
                message.setCreatedBy("SYS:message");
                message.setOpenid(openid);
                message.setPrepayId(prepayId);
                message.setOrderid(paramMap.get("out_trade_no").toString());
                message.setType(2);//prepay_id
                message.setNumber(3);//初始化使用次数
                this.messageMapper.insertPrepayIdMessage(message);
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
    private void createPreOrder(@RequestParam("sid") String sid, @RequestParam("cable_type") String cableType, User user, String orderid, Integer orderStatus) {
        Station station = stationMapper.getStationBySid(sid);
        Shop shop = shopMapper.getShopInfoBySid(sid);
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(sid);
        Order order = new Order();
        order.setCreatedBy("SYS:prepay");
        order.setCreatedDate(new Date());
        order.setBorrowStationName(station.getTitle());
        //order.setBorrow_time(order.getCreatedDate());预付订单并没有借出成功，不设置借出时间
        order.setOrderid(orderid);//订单编号
        order.setPlatform(3);//平台(小程序)
        order.setPrice(defaultPay);//商品价格(元)
        order.setPaid(BigDecimal.ZERO);//已支付的费用
        order.setUsefee(BigDecimal.ZERO);//产生的费用
        order.setCable(Integer.valueOf(cableType));
        order.setStatus(orderStatus);//支付状态
        order.setCustomer(user.getId());//用户id
        order.setBorrowShopId(shop.getId());
        order.setBorrowShopStationId(shopStation.getId());
        order.setBorrowStationId(station.getId());
        orderMapper.saveOrder(order);
    }

    /**
     * 微信支付参数
     *
     * @param openid
     * @param usablemoney
     * @return
     */
    private Map<String, Object> createPrepayParam(String openid, BigDecimal usablemoney) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("appid", appID);
        paramMap.put("attach", "attach");
        paramMap.put("body", "云充吧驿站");
        paramMap.put("goods_tag", "notag");
        paramMap.put("mch_id", mchId);
        paramMap.put("nonce_str", WXPayUtil.getNonce_str());
        paramMap.put("notify_url", notifyUrl);
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
        paramMap.put("total_fee", defaultPay.subtract(usablemoney).multiply(BigDecimal.valueOf(100)).longValue());//费用 TODO
        paramMap.put("trade_type", "JSAPI");
        return paramMap;
    }

    @RequestMapping(value = "/payNotify", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String payNotify(HttpServletRequest request) {
        try {
            String responseStr = HttpRequest.parseWeixinCallback(request);
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
                String openid = (String) map.get("openid");
                Long paid = Long.valueOf(totlaFee);
                // 幂等性设计，根据订单号，判断订单状态是否是未支付状态，是继续，不是，则说明已经更新成功
                Integer status = orderMapper.getOrderStatus(outTradeNo);
                if (status == 0) {
                    //修改订单状态为1，已支付
                    Order order = new Order();
                    order.setLastModifiedBy("SYS:pay");
                    order.setLastModifiedDate(new Date());
                    order.setStatus(1);//订单状态改为1，已支付
                    order.setPaid(BigDecimal.valueOf(paid).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)); //已支付的费用 分转换元
                    order.setOrderid(outTradeNo);
                    orderMapper.updateOrderStatus(order);
                    Long customer = orderMapper.getCustomer(outTradeNo);
                    //更新用户账户信息，押金
                    userMapper.updateUserDeposit(BigDecimal.valueOf(paid).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP), customer);
                    //根据订单查询MAC和CABLE
                    Station station = stationMapper.getMacCableByOrderid(outTradeNo);
                    socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + station.getId() + ";MAC:" + station.getMac() + ";ORDERID:" + outTradeNo + ";COLORID:7;CABLE:" + station.getCable() + ";\r\n");

                     logger.info("ORDERID:" + outTradeNo + "支付成功！");
                }
                // 告诉微信服务器，我收到信息了，不要在调用回调action了
                return WXPayUtil.setXML("SUCCESS", "OK");
            }
        } catch (Exception e) {
            logger.error("支付失败" + e.getMessage());
            return WXPayUtil.setXML("FAIL", "weixin pay server exception");
        }
        return WXPayUtil.setXML("FAIL", "weixin pay fail");
    }
}