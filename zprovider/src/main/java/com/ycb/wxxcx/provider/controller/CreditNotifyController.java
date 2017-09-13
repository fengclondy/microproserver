package com.ycb.wxxcx.provider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Huo on 2017/9/13.
 */
@RestController
@RequestMapping("/notify")
public class CreditNotifyController {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory
            .getLogger(CreditNotifyController.class);

    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${alipayPublicKey}")
    private String alipayPublicKey;

    /**
     * 异步通知请求入口.
     *
     * @param modelMap
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/notifyTest.json", method = {RequestMethod.POST})
    public String index(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) throws IOException {

        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        //1、签名验证
        //验证

        /**
         @param params 参数列表(包括待验签参数和签名值sign) key-参数名称 value-参数值
         @param publicKey 验签公钥
         @param charset 验签字符集
         **/
        //验证签名是否成功
        boolean flag = false;
        try {
            flag = AlipaySignature.rsaCheckV2(params, alipayPublicKey, "ISO-8859-1");
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }


        if (flag) {
            //——请根据您的业务逻辑来编写程序（以下代码仅作参考）——
            //notify_type	取值范围：
            //ORDER_CREATE_NOTIFY (订单创建异步事件)
            //ORDER_COMPLETE_NOTIFY (订单完结异步事件)
            String notifyType = params.get("notify_type");

            if ("ORDER_CREATE_NOTIFY".equals(notifyType)) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //该种交易状态只在两种情况下出现
                //1、开通了普通即时到账，买家付款成功后。
                //2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。

                printResponse(response, "success");
                // out.println("success");    //请不要修改或删除
            } else if ("ORDER_COMPLETE_NOTIFY".equals("TRADE_SUCCESS")) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序

                //注意：
                //该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。

                printResponse(response, "success");
                // out.println("success");    //请不要修改或删除
            }

            //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
        } else {//
            return "fail";
        }
        return null;
    }


    //2、通知参数解析
//    Map<String, String[]> notifyParams = request.getParameterMap();
//    String notifyParamStr = JSONObject.toJSONString(notifyParams);
//        LoggerUtil.warn(logger,"异步通知:"+notifyParamStr);

    //3、执行业务逻辑
    //……

    //4、向芝麻反馈处理是否成功
    // printResponse(response, "success");


    protected void printResponse(HttpServletResponse response, String content) throws IOException {
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(content);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}