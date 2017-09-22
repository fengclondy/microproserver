package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentQueryRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentQueryResponse;
import com.ycb.zprovider.constant.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Huo on 2017/9/11.
 */
@RestController
@RequestMapping("queryOrder")
public class CreditQueryOrderController {

    public static final Logger logger = LoggerFactory.getLogger(CreditQueryOrderController.class);
    //初始化alipayClient用到的参数:支付宝网关
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

    @RequestMapping(value = "/completeOrder", method = RequestMethod.POST)
    @ResponseBody
    //outOrderNo 外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数	2016100100000xxxx
    public String query(@RequestParam("outOrderNo") String outOrderNo) {

        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentQueryRequest request = new ZhimaMerchantOrderRentQueryRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

        request.setBizContent("{" +
                "\"out_order_no\":\"" + outOrderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"" +
                "  }");
        ZhimaMerchantOrderRentQueryResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功,查询成功\n");
            System.out.println("芝麻信用借还订单号\t" + response.getOrderNo()
                    + "\n物品名称,最长不能超过14个汉字\t" + response.getGoodsName()
                    + "\n支付宝userId\t" + response.getUserId()
                    + "\n借用时间\t" + response.getBorrowTime()
                    + "\n归还时间\t" + response.getRestoreTime()
                    + "\n订单状态: \n" +
                    "borrow:借出 \n" +
                    "restore:归还 \n" +
                    "cancel:撤销\t" + response.getUseState()
                    + "\n支付状态 \n" +
                    "PAY_INIT:待支付 \n" +
                    "PAY_SUCCESS:支付成功 \n" +
                    "PAY_FAILED:支付失败 \n" +
                    "PAY_INPROGRESS:支付中\t" + response.getPayStatus()
                    + "\n支付金额类型 \n" +
                    "RENT:租金 \n" +
                    "DAMAGE:赔偿金\t" + response.getPayAmountType()
                    + "\n支付金额\t" + response.getPayAmount()
                    + "\n支付时间\t" + response.getPayTime()
                    + "\n是否准入: \n" +
                    "Y:准入 \n" +
                    "N:不准入\t" + response.getAdmitState()
                    + "\n资金流水号\t" + response.getAlipayFundOrderNo()
            );
        } else {
            System.out.println("调用失败");
        }
        return null;
    }

}
