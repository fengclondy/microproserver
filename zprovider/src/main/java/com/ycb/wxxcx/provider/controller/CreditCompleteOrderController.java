package com.ycb.wxxcx.provider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCompleteRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCompleteResponse;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.vo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Huo on 2017/9/11.
 * 用户在最长租借时间范围内归还电池后，由通信系统调用该controller
 * 在该controller中调用支付宝的信用订单完结接口，通知支付宝处理该订单
 */

/**
 * 间隔一段时间查询一次数据库，查询出哪些用户超时未还充电宝，调用信用借还订单完结接口，扣取物品赔偿金
 */
@RestController
@RequestMapping("restoreBattery")
public class CreditCompleteOrderController {
    public static final Logger logger = LoggerFactory.getLogger(CreditCompleteOrderController.class);

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

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    @ResponseBody
    //orderid   订单编号，是在创建信用借还订单的时候商家创建的订单编号
    //returnTime  物品归还时间
    //payAmount 需要支付的金额,需要将传入的金额进行转换
    //restoreShopName 物品归还门店名称,例如肯德基文三路门店
    public void query(@RequestParam("orderid") String orderid,
                      @RequestParam("returnTime") String returnTime,
                      @RequestParam("payAmount") String payAmount,
                      @RequestParam("restoreShopName") String restoreShopName) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
                format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
        //根据orderID获得信用借还订单的支付宝的编号
        //Order order = orderMapper.findOrderByOrderId(orderid);
        //String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = "w1010100000000002858";
        //物品归还时间	2016-10-01 12:00:00
        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "RENT";
        //支付金额	100.00


        //需要支付的金额,需要将传入的金额进行转换
//        String payAmount = "";

        request.setBizContent("{" +
                //"\"order_no\":\"" + orderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"," +
                "\"restore_time\":\"" + restoreTime + "\"," +
                "\"pay_amount_type\":\"" + payAmountType + "\"," +
                "\"pay_amount\":\"" + payAmount + "\"," +
                "\"restore_shop_name\":\"" + restoreShopName + "\"" +
                "  }");
        ZhimaMerchantOrderRentCompleteResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功,信用借还订单完结");
            //更新订单信息
            updateOrder(response, payAmount);
        } else {
            System.out.println("调用失败");
        }
    }

    //调用信用借还完结接口完结订单后，更新订单信息
    private void updateOrder(ZhimaMerchantOrderRentCompleteResponse response, String payAmount) {

        //借用人支付宝userId.	例如2088202924240029
        String responseUserId = response.getUserId();
        //信用借还的订单号,例如100000
        String responseOrderNo = response.getOrderNo();
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String responseAlipayFundOrderNo = response.getAlipayFundOrderNo();

        Order order = new Order();
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());
        order.setStatus(1);//订单状态改为1，已支付

        //设置费用
//        order.setPaid(BigDecimal.valueOf(payAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)); //已支付的费用 分转换元

        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
        order.setOrderNo(responseOrderNo);
        order.setAlipayFundOrderNo(responseAlipayFundOrderNo);
        orderMapper.updateOrderStatusByOrderNo(order);

    }

//    //第一次延迟0秒执行，当执行完后，每隔fixedDelay（毫秒）执行一次
//    @Scheduled(initialDelay = 0, fixedDelay = 4000)
//    public void dealWithOverdueUsers() {
//
//        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
//                format, charset, alipayPublicKey, signType);
//        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
//        //根据orderID获得信用借还订单的支付宝的编号
//        Order order = orderMapper.findOrderByOrderId(orderid);
//        String orderNo = order.getOrderNo();
//        //信用借还的产品码:w1010100000000002858
//        String productCode = "w1010100000000002858";
//        //物品归还时间	2016-10-01 12:00:00
//        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date());
//        /*
//        金额类型：
//        RENT:租金
//        DAMAGE:赔偿金
//         */
//        String payAmountType = "RENT";
//        //支付金额	100.00
//
//
//        //需要支付的金额,需要将传入的金额进行转换
////        String payAmount = "";
//
//        request.setBizContent("{" +
//                //"\"order_no\":\"" + orderNo + "\"," +
//                "\"product_code\":\"" + productCode + "\"," +
//                "\"restore_time\":\"" + restoreTime + "\"," +
//                "\"pay_amount_type\":\"" + payAmountType + "\"," +
//                "\"pay_amount\":\"" + payAmount + "\"," +
//                "\"restore_shop_name\":\"" + restoreShopName + "\"" +
//                "  }");
//        ZhimaMerchantOrderRentCompleteResponse response = null;
//        try {
//            response = alipayClient.execute(request);
//        } catch (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        if (response.isSuccess()) {
//            System.out.println("调用成功,信用借还订单完结");
//            //更新订单信息
//            updateOrder(response, payAmount);
//        } else {
//            System.out.println("调用失败");
//        }
//    }

}
