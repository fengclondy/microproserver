package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.utils.GetWxOrderno;
import com.ycb.wxxcx.provider.utils.RequestHandler;
import com.ycb.wxxcx.provider.utils.TenpayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * 微信退款
 */
@RestController
@RequestMapping("refund")
public class WechatRefundController {

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @Value("${mch_id}")
    private String mchId;

    @Value("${key}")
    private String key;

    @RequestMapping("/doRefund")
    @ResponseBody
    public String wechatRefund(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {

        //生成随机字符串
        String currTime = TenpayUtil.getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = TenpayUtil.buildRandom(4) + "";
        String nonce_str = strTime + strRandom;

        String out_trade_no = "0001";//商户订单号
        String out_refund_no = "1000100";//商户退款单号
        String total_fee = "100";//退款金额
        String refund_fee = "100";//退款总金额

        SortedMap<String, String> parameters = new TreeMap<String, String>();
        parameters.put("appid", appID);//公众账号ID
        parameters.put("mch_id", mchId);//商户号
        parameters.put("nonce_str", nonce_str);//随机字符串
        // 在notify_url中解析微信返回的信息获取到 transaction_id，此项不是必填，详细请看上图文档
        // parameters.put("transaction_id", "微信支付订单中调用统一接口后微信返回的 transaction_id");
        parameters.put("out_trade_no", out_trade_no);//商户系统内部订单号
        parameters.put("out_refund_no", out_refund_no); //商户系统内部的退款单号，约束为UK唯一
        parameters.put("total_fee", total_fee); //订单总金额：单位为分
        parameters.put("refund_fee", refund_fee); //退款总金额：单位为分
        parameters.put("op_user_id", mchId);// 操作员帐号, 默认为商户号

        RequestHandler requestHandler = new RequestHandler(request, response);
        requestHandler.init(appID, appSecret, key);
        String sign = requestHandler.createSign(parameters);//创建md5 签名摘要

        String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

        String xml = "<xml>"
                + "<appid><![CDATA[" + appID + "]]></appid>"
                + "<mch_id><![CDATA[" + mchId + "]]></mch_id>"
                + "<nonce_str><![CDATA[" + nonce_str + "]]></nonce_str>"
                + "<out_trade_no><![CDATA[" + out_trade_no + "]]></out_trade_no>"
                + "<out_refund_no><![CDATA[" + out_refund_no + "]]></out_refund_no>"
                + "<total_fee><![CDATA[" + total_fee + "]]></total_fee>"
                + "<refund_fee><![CDATA[" + refund_fee + "]]></refund_fee>"
                + "<op_user_id><![CDATA[" + mchId + "]]></op_user_id>"
                + "<sign>" + sign + "</sign>"   //签名
                + "</xml>";

        try {
            Map map = GetWxOrderno.forRefund(createOrderURL, xml);
            if (map != null) {
                String return_code = (String) map.get("return_code");//返回状态码
                String result_code = (String) map.get("result_code");//业务结果
                if (return_code.equals("SUCCESS") && result_code.equals("SUCCESS")) {
                    System.out.println("退款成功");
                } else {
                    System.out.println("退款失败");
                }
            } else {
                System.out.println("退款失败");
            }
        } catch (Exception e) {
            System.out.print("退款失败");
            e.printStackTrace();
        }
        return null;
    }
}
