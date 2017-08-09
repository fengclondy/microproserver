package com.ycb.wxxcx.provider.controller;

import com.google.common.base.Charsets;
import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.utils.HttpRequest;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.utils.WXPayUtil;
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
    private RedisService redisService;

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
                          @RequestParam("sid") String sid,
                          @RequestParam("cable_type") String cableType,
                          @RequestParam("tid") String tid) {
        // 统一下单，生成预支付交易单
        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("appid", appID);
        paramMap.put("attach", "attach");
        paramMap.put("body", "云充吧驿站");
        paramMap.put("goods_tag", "notag");
        paramMap.put("mch_id", mchId);
        paramMap.put("nonce_str", WXPayUtil.getNonce_str());
        paramMap.put("notify_url", "https://m.pzzhuhui.top/wxpay/payNotify");
        paramMap.put("openid", redisService.getKeyValue(session));
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        String hhmmss = DateFormatUtils.format(new Date(), "hhmmss");
        int randomNum = RandomUtils.nextInt(99999);
        paramMap.put("out_trade_no", "MCS-" + yyyyMMdd + "-" + hhmmss + "-" + randomNum);
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
        paramMap.put("total_fee", 1);
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
            Map<String, Object> map = WXPayUtil.doXMLParse(responseStr);
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
                // if (PayApp.theApp.isDebug()) {// 测试时候支付一分钱，买入价值6块的20分钟语音
                //    totalPrice = 6;
                // }
                // boolean isOk = updateDB(outTradeNo, transactionId, totalPrice, 2);
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
