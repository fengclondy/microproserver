package com.ycb.wxxcx.provider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayOpenPublicMessageSingleSendRequest;
import com.alipay.api.request.ZhimaMerchantOrderRentCompleteRequest;
import com.alipay.api.response.AlipayOpenPublicMessageSingleSendResponse;
import com.alipay.api.response.ZhimaMerchantOrderRentCompleteResponse;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.mapper.ShopMapper;
import com.ycb.wxxcx.provider.service.FeeStrategyService;
import com.ycb.wxxcx.provider.vo.FeeStrategy;
import com.ycb.wxxcx.provider.vo.Order;
import com.ycb.wxxcx.provider.vo.Shop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private ShopMapper shopMapper;

    @RequestMapping(value = "/restore", method = RequestMethod.POST)
    @ResponseBody
    //orderid   订单编号，是在创建信用借还订单的时候商家创建的订单编号
    public void CompleteOrder(@RequestParam("orderid") String orderid) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
                format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
        //根据orderID获得信用借还订单的支付宝的编号
        Order order = orderMapper.findOrderByOrderId(orderid);
        String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = "w1010100000000002858";
        //物品归还时间	2016-10-01 12:00:00
        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(order.getReturnTime());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "RENT";
        //支付金额	100.00
        //payAmount 需要支付的金额
        String payAmount = order.getUsefee().toString();
        //restoreShopName 物品归还门店名称,例如肯德基文三路门店
        Long returnShopId = order.getReturnShopId();
        Shop shopInfo = shopMapper.findShopById(returnShopId);
        String restoreShopName = shopInfo.getName();

        request.setBizContent("{" +
                "\"order_no\":\"" + orderNo + "\"," +
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
            updateOrder(response);
            //通知用户归还成功
            sendMassage(response.getUserId(), order, restoreShopName);
        } else {
            System.out.println("调用失败");
        }
    }

    //用户借用成功后，调用发送消息的接口给用户发送借用成功的通知
    //responseUserId        用户ID
    //订单信息
    //restoreShopName 归还地点
    private void sendMassage(String responseUserId, Order order, String restoreShopName) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        AlipayOpenPublicMessageSingleSendRequest request = new AlipayOpenPublicMessageSingleSendRequest();

        //消息模板ID
        String templateId = "9bc0cc74b6fc4949a7fcd9736f812469".trim();

        //顶部色条的色值
        String headColor = "#000000";

        //点击消息后承接页的地址
        String url = "";
        //底部链接描述文字，如“查看详情”
        String actionName = "查看详情";

        //当前文字颜色
        String firstColor = "#000000";
        //模板中占位符的值
        String firstValue = "";
        //归还地点
        //keyword1
        String keyword1Color = "#000000";
        String keyword1Value = restoreShopName;
        //归还时间
        //keyword2
        String keyword2Color = "#000000";
        String keyword2Value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getReturnTime());
        //租用时长
        //keyword3
        String keyword3Color = "#000000";
        //租用的时间，单位  秒
        long l = (order.getReturnTime().getTime() - order.getBorrowTime().getTime()) / 1000;
        //天
        long days = l / 86400;
        //小时
        long hours = l % 86400 / 3600;
        //分钟
        long minutes = l % 86400 % 3600 / 60;
        //秒
        long seconds = l % 86400 % 3600 % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days + "天");
        }
        if (hours > 0) {
            sb.append(hours + "小时");
        }
        if (minutes > 0) {
            sb.append(minutes + "分钟");
        }
        if (seconds > 0) {
            sb.append(seconds + "秒");
        }
        String keyword3Value = sb.toString();
        //订单号
        //keyword4
        String keyword4Color = "#000000";
        String keyword4Value = order.getOrderNo();

        //remark
        String remarkColor = "#32cd32";
        String remarkValue = "此次租借产生费用" + order.getPrice() + "元。如有疑问，请致电4006290808";

        request.setBizContent("{" +
                "\"to_user_id\":\"" + responseUserId + "\"," +
                "\"template\":{" +
                "\"template_id\":\"" + templateId + "\"," +
                "\"context\":{" +
                "\"head_color\":\"" + headColor + "\"," +
                "\"url\":\"" + url + "\"," +
                "\"action_name\":\"" + actionName + "\"," +
                "\"keyword1\":{" +
                "\"color\":\"" + keyword1Color + "\"," +
                "\"value\":\"" + keyword1Value + "\"" +
                "        }," +
                "\"keyword2\":{" +
                "\"color\":\"" + keyword2Color + "\"," +
                "\"value\":\"" + keyword2Value + "\"" +
                "        }," +
                "\"keyword3\":{" +
                "\"color\":\"" + keyword3Color + "\"," +
                "\"value\":\"" + keyword3Value + "\"" +
                "        }," +
                "\"keyword4\":{" +
                "\"color\":\"" + keyword3Color + "\"," +
                "\"value\":\"" + keyword3Value + "\"" +
                "        }," +
                "\"first\":{" +
                "\"color\":\"" + firstColor + "\"," +
                "\"value\":\"" + firstValue + "\"" +
                "        }," +
                "\"remark\":{" +
                "\"color\":\"" + remarkColor + "\"," +
                "\"value\":\"" + remarkValue + "\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }");
        AlipayOpenPublicMessageSingleSendResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            logger.error("通知用户借用成功失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
    }

    //用户归还后，更新订单表的信息
    private void updateOrder(ZhimaMerchantOrderRentCompleteResponse response) {
        //借用人支付宝userId.	例如2088202924240029
        String responseUserId = response.getUserId();
        //信用借还的订单号,例如100000
        String responseOrderNo = response.getOrderNo();
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String responseAlipayFundOrderNo = response.getAlipayFundOrderNo();

        Order order = new Order();
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());

        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
        order.setOrderNo(responseOrderNo);
        order.setAlipayFundOrderNo(responseAlipayFundOrderNo);
        orderMapper.updateOrderStatusByOrderNo(order);
    }


    //每隔fixedDelay（毫秒）执行一次
//    @Scheduled(fixedRate = 43200000)
    @Scheduled(fixedRate = 200000000)
    public void dealWithOverdueUsers() {

        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
                format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();
        //查询出还没有归还的订单
        List<Order> overdueOrders = orderMapper.findOverdueOrders();

        for (int i = 0; i < overdueOrders.size(); i++) {
            Order order = overdueOrders.get(i);
            //借出设备id
            Long sid = order.getBorrowStationId();
            Date borrowTime = order.getBorrowTime();
            //信用借还订单编号
            String orderNo = order.getOrderNo();

            FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));

            //获取最长时长
            Long maxFeeTime = feeStrategy.getMaxFeeTime();
            //获取最长时长的单位
            Long maxFeeUnit = feeStrategy.getMaxFeeUnit();
            //用开始租借的时间加上最长时长
            long l = borrowTime.getTime() + maxFeeTime * maxFeeUnit * 1000;
            //判断用户是否逾期未归还
            if (l < new Date().getTime()) {
                //向支付宝发送完结订单的请求
                sendCompleteOverdueRequest(order);
            }
        }
    }

    //对于逾期未归还的用户，向支付宝发送完结订单的请求
    private void sendCompleteOverdueRequest(Order order) {

        String orderNo = order.getOrderNo();
        //信用借还的产品码:w1010100000000002858
        String productCode = "w1010100000000002858";
        //物品归还时间	2016-10-01 12:00:00
        String restoreTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date());
        /*
        金额类型：
        RENT:租金
        DAMAGE:赔偿金
         */
        String payAmountType = "DAMAGE";
        //支付金额
        //需要支付的金额
        //借出设备id
        Long sid = order.getBorrowStationId();
        //根据设备的sid查询店铺信息
        Shop shopInfo = shopMapper.getShopInfoBySid(sid.toString());
        //查询出设备押金,也就是用户需要支付的赔偿金
        String payAmount = shopInfo.getDefaultPay().toString();

        //物品归还门店名称,可选
        //String restoreShopName = "";

        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey,
                format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCompleteRequest request = new ZhimaMerchantOrderRentCompleteRequest();

        request.setBizContent("{" +
                "\"order_no\":\"" + orderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"," +
                "\"restore_time\":\"" + restoreTime + "\"," +
                "\"pay_amount_type\":\"" + payAmountType + "\"," +
                "\"pay_amount\":\"" + payAmount + "\"," +
                //"\"restore_shop_name\":\"" + restoreShopName + "\"" +
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
            updateOverdueOrder(response);
        } else {
            System.out.println("调用失败");
        }
    }

    //调用信用借还完结接口完结订单后，更新逾期订单信息
    private void updateOverdueOrder(ZhimaMerchantOrderRentCompleteResponse response) {

        //借用人支付宝userId.	例如2088202924240029
        String responseUserId = response.getUserId();
        //信用借还的订单号,例如100000
        String responseOrderNo = response.getOrderNo();
        //资金流水号，用于商户与支付宝进行对账	2088000000000000
        String responseAlipayFundOrderNo = response.getAlipayFundOrderNo();

        Order order = new Order();
        order.setLastModifiedBy("SYS:completecreditpay");
        order.setLastModifiedDate(new Date());
        order.setStatus(92);//租金已扣完（未归还）

        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
        order.setOrderNo(responseOrderNo);
        order.setAlipayFundOrderNo(responseAlipayFundOrderNo);
        orderMapper.updateOrderStatusByOrderNo(order);

    }

}
