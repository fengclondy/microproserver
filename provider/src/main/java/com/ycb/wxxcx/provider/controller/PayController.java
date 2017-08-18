package com.ycb.wxxcx.provider.controller;

import com.google.common.base.Charsets;
import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.*;
import com.ycb.wxxcx.provider.service.SocketService;
import com.ycb.wxxcx.provider.utils.HttpRequest;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.utils.WXPayUtil;
import com.ycb.wxxcx.provider.utils.XmlUtil;
import com.ycb.wxxcx.provider.vo.*;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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
        String openid = redisService.getKeyValue(session);
        User user = userMapper.findUserIdByOpenid(openid);
        Station station = stationMapper.getStationBySid(sid);
        Shop shop = shopMapper.getShopInfoBySid(sid);
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(sid);
        // 统一下单，生成预支付交易单
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("appid", appID);
        paramMap.put("attach", "attach");
        paramMap.put("body", "云充吧驿站");
        paramMap.put("goods_tag", "notag");
        paramMap.put("mch_id", mchId);
        paramMap.put("nonce_str", WXPayUtil.getNonce_str());
        paramMap.put("notify_url", "https://m.pzzhuhui.top/wxpay/payNotify");
        paramMap.put("openid", openid);
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        //MCS-20170815-113816-88701
        String hhmmss = DateFormatUtils.format(new Date(), "HHmmss");
        int randomNum = RandomUtils.nextInt(99999);
        String out_trade_no = "MCS-" + yyyyMMdd + "-" + hhmmss + "-" + String.format("%05d", randomNum);
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
        paramMap.put("total_fee", 1);//费用
        paramMap.put("trade_type", "JSAPI");

        Map<String, Object> bacMap = new HashMap<>();
        try {
            String preOrderInfo = HttpRequest.sendPost("", WXPayUtil.map2Xml(paramMap, key));
            Map<String, Object> prePayMap = new LinkedHashMap<>();
            prePayMap.put("appId", WXPayUtil.getAppId(preOrderInfo));
            prePayMap.put("nonceStr", WXPayUtil.getNonceStr(preOrderInfo));
            prePayMap.put("package", "prepay_id=" + WXPayUtil.getPrepayId(preOrderInfo));
            prePayMap.put("signType", "MD5");
            prePayMap.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            String paySign = WXPayUtil.getSign(prePayMap, key);
            Map<String, Object> data = new HashMap<>();
            data.put("timeStamp", prePayMap.get("timeStamp"));
            data.put("nonceStr", prePayMap.get("nonceStr"));
            data.put("package", prePayMap.get("package"));
            data.put("signType", prePayMap.get("signType"));
            data.put("paySign", paySign);
            bacMap.put("wxpay_params", data);
            bacMap.put("code", 0);
            bacMap.put("errcode", 0);
            bacMap.put("msg", "成功");

            //创建订单
            Order order = new Order();
            order.setCreatedBy("system");
            order.setCreatedDate(new Date());
            order.setBorrow_city(shop.getCity());
            order.setBorrow_station_name(station.getTitle());
            order.setBorrow_time(order.getCreatedDate());
            order.setOrderid(out_trade_no);//订单编号
            order.setPlatform(3);//平台(小程序)
            order.setPrice(BigDecimal.valueOf(100));//商品价格
            order.setPaid(BigDecimal.ZERO);//已支付的费用
            order.setStatus(0);//未支付状态
            order.setUsefee(BigDecimal.ZERO);//产生的费用
            order.setCustomer(user.getId());//用户id
            order.setBorrow_shop_id(shop.getId());
            order.setBorrow_shop_station_id(shopStation.getId());
            order.setBorrow_station_id(station.getId());

            orderMapper.saveOrder(order);

        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 0);
            bacMap.put("msg", "失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
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
                String transactionId = (String) map.get("transaction_id");
                String totlaFee = (String) map.get("total_fee");
                Integer totalPrice = Integer.valueOf(totlaFee);

                //根据订单查询MAC和CABLE
                Station station = stationMapper.getMacCableByOrderid(outTradeNo);

                socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;MAC:"+station.getMac()+";ORDERID:"+outTradeNo+";COLORID:7;CABLE:"+station.getCable()+";\r\n");
                //修改订单状态
                Order order = new Order();
                order.setLastModifiedBy("system");
                order.setLastModifiedDate(new Date());
                order.setStatus(1);//订单状态改为1，已支付
                order.setPaid(BigDecimal.valueOf(totalPrice)); //已支付的费用
                order.setOrderid(outTradeNo);
                orderMapper.updateOrderStatus(order);

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
