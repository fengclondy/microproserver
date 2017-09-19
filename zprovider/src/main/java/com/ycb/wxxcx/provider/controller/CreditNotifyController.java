package com.ycb.wxxcx.provider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.ZhimaMerchantOrderRentQueryRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentQueryResponse;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.vo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
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

    //初始化alipayClient用到的参数:该appId必须设为开发者自己的生活号id
    @Value("${appId}")
    private String appId;
    //初始化alipayClient用到的参数:该私钥为测试账号私钥  开发者必须设置自己的私钥,否则会存在安全隐患
    @Value("${privateKey}")
    private String privateKey;
    //初始化alipayClient用到的参数:仅支持JSON
    @Value("${format}")
    private String format;
    //初始化alipayClient用到的参数:字符编码-传递给支付宝的数据编码
    @Value("${charset}")
    private String charset;
    //初始化alipayClient用到的参数:该公钥为测试账号公钥,开发者必须设置自己的公钥 ,否则会存在安全隐患
    @Value("${alipayPublicKey}")
    private String alipayPublicKey;
    //初始化alipayClient用到的参数:签名类型
    @Value("${signType}")
    private String signType;


    @Autowired
    private OrderMapper orderMapper;

    /**
     * 异步通知请求入口.
     *
     * @param modelMap
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/notify", method = {RequestMethod.POST})
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

            //因为是在用户点击借用按钮的时候创建的订单，所以不需要处理订单创建完结事件
            if ("ORDER_CREATE_NOTIFY".equals(notifyType)) {
                //返回 success
                printResponse(response, "success");
            }
            //处理订单完结异步通知
            if ("ORDER_COMPLETE_NOTIFY".equals(notifyType)) {
                //判断该笔订单是否在商户网站中已经做过处理
                //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                //如果有做过处理，不执行商户的业务程序
                //信用借还平台订单号 芝麻信用借还平台生成的订单号
                String orderNo = params.get("order_no");
                //外部商户订单号 外部商户生成的订单号，与芝麻信用借还平台生成的订单号存在关联关系
                String outOrderNo = params.get("out_order_no");

                Order order = orderMapper.findOrderByOrderId(outOrderNo);
                //因为在用户点击后就生成这个订单，所以这个订单一定存在
                //当订单的流水编号不存在时
                if (null == order.getAlipayFundOrderNo() || "".equals(order.getAlipayFundOrderNo())) {
                    AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL,
                            appId, privateKey, format, charset, alipayPublicKey,
                            signType);
                    ZhimaMerchantOrderRentQueryRequest zhimaMerchantOrderRentQueryRequest = new ZhimaMerchantOrderRentQueryRequest();
                    //外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数	2016100100000xxxx
                    //String outOrderNo="";
                    //信用借还的产品码:w1010100000000002858
                    String productCode = "w1010100000000002858";
                    zhimaMerchantOrderRentQueryRequest.setBizContent("{" +
                            "\"out_order_no\":\"" + outOrderNo + "\"," +
                            "\"product_code\":\"" + productCode + "\"" +
                            "  }");
                    ZhimaMerchantOrderRentQueryResponse ZhimaResponse = null;
                    try {
                        ZhimaResponse = alipayClient.execute(zhimaMerchantOrderRentQueryRequest);
                    } catch (AlipayApiException e) {
                        e.printStackTrace();
                    }
                    if (ZhimaResponse.isSuccess()) {
                        System.out.println("调用成功");

                        //向ycb_mcs_tradelog中存入数据
                        Order updateorder = new Order();

                        //借用人支付宝userId.	例如2088202924240029
                        String responseUserId = ZhimaResponse.getUserId();
                        //信用借还的订单号,例如100000
                        String responseOrderNo = ZhimaResponse.getOrderNo();
                        //资金流水号，用于商户与支付宝进行对账	2088000000000000
                        String responseAlipayFundOrderNo = ZhimaResponse.getAlipayFundOrderNo();

                        updateorder.setLastModifiedBy("SYS:completecreditpay");
                        updateorder.setLastModifiedDate(new Date());

                        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
                        updateorder.setOrderNo(responseOrderNo);
                        updateorder.setAlipayFundOrderNo(responseAlipayFundOrderNo);

                        orderMapper.updateOrderStatusByOrderNo(updateorder);
                    } else {
                        System.out.println("调用失败");
                    }
                }
                //返回 success
                printResponse(response, "success");
            }
        } else {//
            return "fail";
        }
        return null;
    }

    //返回 success
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