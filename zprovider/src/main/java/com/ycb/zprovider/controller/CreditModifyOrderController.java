package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentModifyRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentModifyResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Huo on 2017/9/11.
 */
@RestController
@RequestMapping("modifyOrder")
public class CreditModifyOrderController {

    public static final Logger logger = LoggerFactory.getLogger(CreditModifyOrderController.class);
    //初始化alipayClient用到的参数:支付宝网关
    @Value("${serverUrl}")
    private String serverUrl;
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

    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    @ResponseBody
    //orderNo 信用借还订单号,该订单号在订单创建时由信用借还产品产生,并通过订单创建接口的返回结果返回给调用者
    public String query(@RequestParam("orderNo") String orderNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentModifyRequest request = new ZhimaMerchantOrderRentModifyRequest();
        //信用借还的产品码,是固定值:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        //芝麻借还订单的开始借用时间，格式：yyyy-mm-dd hh:MM:ss,可选
        //如果同时传入另一参数:应归还时间expiry_time，则传入的开始借用时间不能晚于传入的应归还时间，如果没有传入应归还时间，则传入的开始借用时间不能晚于原有应归还时间。
        //borrow_time 与 expiry_time 须至少传入一个，可同时传入。
        String borrowTime = "";
        //芝麻借还订单的应归还时间(到期时间)，格式：yyyy-mm-dd hh:MM:ss,可选
        //传入的应归还时间不能早于原有应归还时间。
        //borrow_time 与 expiry_time 须至少传入一个，可同时传入
        String expiryTime = "";

        request.setBizContent("{" +
                "\"order_no\":\"" + orderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"," +
                "\"borrow_time\":\"" + borrowTime + "\"," +
                "\"expiry_time\":\"" + expiryTime + "\"" +
                "  }");
        ZhimaMerchantOrderRentModifyResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println("修改订单" + response.getMsg());
        } else {
            System.out.println("调用失败");
        }

        return null;
    }
}
