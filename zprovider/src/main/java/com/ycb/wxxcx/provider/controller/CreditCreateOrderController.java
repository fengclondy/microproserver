package com.ycb.wxxcx.provider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.ZhimaMerchantOrderRentCancelRequest;
import com.alipay.api.request.ZhimaMerchantOrderRentCreateRequest;
import com.alipay.api.response.ZhimaMerchantOrderRentCancelResponse;
import com.alipay.api.response.ZhimaMerchantOrderRentCreateResponse;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.OrderMapper;
import com.ycb.wxxcx.provider.mapper.ShopMapper;
import com.ycb.wxxcx.provider.mapper.ShopStationMapper;
import com.ycb.wxxcx.provider.mapper.StationMapper;
import com.ycb.wxxcx.provider.service.FeeStrategyService;
import com.ycb.wxxcx.provider.service.SocketService;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Huo on 2017/9/8.
 */
@RestController
@RequestMapping("creditOrder")
public class CreditCreateOrderController {

    public static final Logger logger = LoggerFactory.getLogger(CreditCreateOrderController.class);

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
    private ShopMapper shopMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopStationMapper shopStationMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private FeeStrategyService feeStrategyService;

    @Autowired
    private SocketService socketService;

    @RequestMapping(value = "/createOrder", method = RequestMethod.POST)
    @ResponseBody
    //sid   设备id
    //cableType 数据线类型
    public void query(@RequestParam("sid") String sid, @RequestParam("cable_type") String cableType) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL,
                appId, privateKey, format, charset, alipayPublicKey,
                signType);
        ZhimaMerchantOrderRentCreateRequest request = new ZhimaMerchantOrderRentCreateRequest();
        //回调到商户的url地址
        //商户在组装订单创建https请求时，会附带invoke_return_url参数，当用户完成借用准入及资金处理后，
        // 在借用完成页面会自动回调到商户提供的invoke_return_url地址链接，目前商户链接跳转是通过自动跳转的方式实现。
        String invokeReturnUrl = "";
        /*
        商户请求状态上下文。商户发起借用服务时，需要在借用结束后返回给商户的参数，格式：json
        商户发起借用服务时，一笔订单调用请求的上下文参数信息，创建成功后原参数返回，如果没有可不传。
         */
        Map<String, Object> map = new HashMap<>();
        map.put("sid", sid);
        map.put("cableType", cableType);
        String invokeState = JsonUtils.writeValueAsString(map);

        //下面的代码用来生成外部订单号，就是商户自己的订单号
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Random random = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        /*
        外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+随机数
               示例："2016100100000xxxx\"
        转换后的时间为：20170911091758
         */
        String outOrderNo = date + sb.toString();
        /*
        信用借还的产品码，传入固定值：w1010100000000002858
         */
        String productCode = "w1010100000000002858";
        /*
        物品名称,最长不能超过14个汉字
         */
        String goodsName = "充电宝";
        /*
        租金信息描述 ,长度不超过14个汉字，只用于页面展示给C端用户，除此之外无其他意义。
         */
        String rentInfo = "1小时免费时长";
        /*
        租金单位，租金+租金单位组合才具备实际的租金意义。
        取值定义如下：
        DAY_YUAN:元/天
        HOUR_YUAN:元/小时
        YUAN:元
        YUAN_ONCE: 元/次
         */
        String rentUnit = "HOUR_YUAN";
        /*
        租金，租金+租金单位组合才具备实际的租金意义。
        >0.00元，代表有租金
        =0.00元，代表无租金，免费借用
        注：参数传值必须>=0，传入其他值会报错参数非法
         */
        //这里还需要商议
        FeeStrategy feeStrategy = feeStrategyService.findFeeStrategyByStation(Long.valueOf(sid));
        String rentAmount = feeStrategy.getFixed().toString();
        /*
        押金，金额单位：元。
        注：不允许免押金的用户按此金额支付押金；当物品丢失时，赔偿金额不得高于该金额。
         */
        //根据设备的sid查询店铺信息
        Shop shopInfo = shopMapper.getShopInfoBySid(sid);
        //查询出设备押金
        String depositAmount = shopInfo.getDefaultPay().toString();
        /*
        是否支持当借用用户信用不够（不准入）时，可让用户支付押金借用:
        Y:支持
        N:不支持
        注：支付押金的金额等同于deposit_amount
         */
        String depositState = "Y";

        //物品借用地点的描述，便于用户知道物品是在哪里借的。可为空
        String borrowShopName = shopInfo.getName();

        //借用用户的真实姓名，非必填字段。但name和cert_no必须同时非空，或者同时为空，一旦传入会对用户身份进行校验。
        String name = "";

        //借用用户的真实身份证号，非必填字段。但name和cert_no必须同时非空，或者同时为空，一旦传入会对用户身份进行校验。
        String certNo = "";

        /*
        租金的结算方式，非必填字段，默认是支付宝租金结算支付
        merchant：表示商户自行结算，信用借还不提供租金支付能力；
        alipay：表示使用支付宝支付功能，给用户提供租金代扣及赔偿金支付能力；
         */
        String rentSettleType = "alipay";

        //商户订单创建的起始借用时间，格式：YYYY-MM-DD HH:MM:SS。如果不传入或者为空，则认为订单创建起始时间为调用此接口时的时间。
        Date borrowDate = new Date();
        String borrowTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(borrowDate);
        //下面的代码用于处理到期时间
        //获取最长时长
        Long maxFeeTime = feeStrategy.getMaxFeeTime();
        //获取最长时长的单位
        Long maxFeeUnit = feeStrategy.getMaxFeeUnit();
        //用开始租借的时间加上最长时长
        long l = borrowDate.getTime() + maxFeeTime * maxFeeUnit * 60 * 1000;
        //示例：2017-04-30 12:06:31
        //     2017-09-11 13:09:23
        //到期时间，是指最晚归还时间，表示借用用户如果超过此时间还未完结订单（未归还物品或者未支付租金）将会进入逾期状态，
        // 芝麻会给借用用户发送催收提醒。如果此时间不传入或传空，将视为无限期借用
        String expiryTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date(l));

        //借用用户的手机号码，可选字段。推荐商户传入此值，会将此手机号码与用户身份信息进行匹配验证，防范欺诈风险。
        String mobileNo = "";

        //借用用户的收货地址，可选字段。推荐商户传入此值，会将此手机号码与用户身份信息进行匹配验证，防范欺诈风险。
        String address = "";

        request.setBizContent("{" +
                "\"invoke_type\":\"WINDOWS\"," +
                "\"invoke_return_url\":\"" + invokeReturnUrl + "\"," +
                //"\"notify_url\":\"废弃，使用蚂蚁开放平台应用中的网关地址\"," +
                "\"invoke_state\":\"" + invokeState + "\"," +
                "\"out_order_no\":\"" + outOrderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"," +
                "\"goods_name\":\"" + goodsName + "\"," +
                "\"rent_info\":\"" + rentInfo + "\"," +
                "\"rent_unit\":\"" + rentUnit + "\"," +
                "\"rent_amount\":\"" + rentAmount + "\"," +
                "\"deposit_amount\":\"" + depositAmount + "\"," +
                "\"deposit_state\":\"" + depositState + "\"," +
                //"\"borrow_cycle\":\"\"," +
                //"\"borrow_cycle_unit\":\"HOUR\"," +
                "\"borrow_shop_name\":\"" + borrowShopName + "\"," +
                "\"name\":\"" + name + "\"," +
                "\"cert_no\":\"" + certNo + "\"," +
                "\"rent_settle_type\":\"" + rentSettleType + "\"," +
                "\"borrow_time\":\"" + borrowTime + "\"," +
                "\"expiry_time\":\"" + expiryTime + "\"," +
                "\"mobile_no\":\"" + mobileNo + "\"," +
                "\"address\":\"" + address + "\"" +
                "  }");
        ZhimaMerchantOrderRentCreateResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功，信用借还订单创建成功");
            //创建订单
            createPreOrder(response);

        } else {
            System.out.println("调用失败");

            logger.error("调用创建信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
    }

    /*
    当调用支付宝接口成功创建信用借还订单后，生成订单存储到数据库中，
     */
    private void createPreOrder(ZhimaMerchantOrderRentCreateResponse response) {
        //商户发起借用服务时，需要在借用结束后返回给商户的参数
        String responseInvokeState = response.getInvokeState();
        Map<String, Object> responseMap = JsonUtils.readValue(responseInvokeState);
        //获取到设备的sid
        String responseSid = (String) responseMap.get("sid");
        //外部订单号，需要唯一，由商户传入，芝麻内部会做幂等控制，格式为：yyyyMMddHHmmss+4位随机数
        String responseOutOrderNo = response.getOutOrderNo();
        //芝麻信用借还订单号
        String responseOrderNo = response.getOrderNo();
        //获取之前用户选择的数据线类型
        String responseCableType = (String) responseMap.get("cableType");
        //获取设备的mac，在弹出电池时会使用
        String mac = stationMapper.getStationMac(Long.valueOf(responseSid));
        /*
        是否准入（信用）：
        Y-准入（信用够）
        N-不准入（信用不够）
         */
        String responseAdmitState = response.getAdmitState();
        //订单状态，存储是否用户是否可以使用信用借还
        /*Integer orderStatus = null;

        orderStatus的值
        1表示用户的信用值足够使用信用借还
        2表示用户的信用值不足，创建的是预授权支付订单

        if ("Y".equals(responseAdmitState)) {
            orderStatus = 1;
        } else {
            orderStatus = 2;
        }
        */

        //借用者的userId
        String responseUserId = response.getUserId();

        //向ycb_mcs_tradelog中存入数据
        Order order = new Order();
        order.setCreatedBy("SYS:createcreditpay");
        order.setCreatedDate(new Date());
        order.setBorrowTime(new Date());
        //设置订单编号，为商户自己的订单编号，格式为yyyyMMddHHmmss+4位随机数
        order.setOrderid(responseOutOrderNo);//订单编号
        order.setPlatform(2);//平台(信用借还)
        //根据返回来的responseSid查询设备所在店铺的信息
        Shop responseShop = shopMapper.getShopInfoBySid(responseSid);
        order.setPrice(responseShop.getDefaultPay());//商品价格(元)，从数据库中查询押金defaultPay
        order.setPaid(BigDecimal.ZERO);//已支付的费用
        order.setUsefee(BigDecimal.ZERO);//产生的费用

        order.setCable(Integer.valueOf(responseCableType));
        order.setStatus(0);//支付状态,0为未支付，1为已经支付
        //支付宝给出的示例："user_id":"2088202924240029"
        //数据库中该字段的属性：`customer` bigint(20) NOT NULL,
        try {
            order.setCustomer(Long.parseLong(responseUserId));//用户id
        } catch (NumberFormatException e) {
            System.out.println("支付宝返回的用户id转换为数字失败");
        }
        order.setBorrowShopId(responseShop.getId());
        //根据返回来的sid，查询到设备所在商铺的信息，和设备的信息
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(responseSid);
        Station station = stationMapper.getStationBySid(responseSid);
        order.setBorrowShopStationId(shopStation.getId());
        order.setBorrowStationId(station.getId());
        order.setBorrowStationName(responseShop.getName());
        order.setBorrowCity(responseShop.getCity());
        order.setOrderNo(responseOrderNo);
        //弹出电池
        try {
            socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + responseSid + ";MAC:" + mac + ";ORDERID:" + responseOrderNo + ";COLORID:7;CABLE:" + responseCableType + ";\r\n");
            orderMapper.saveOrder(order);
        } catch (IOException e) {
            logger.error("信用借还订单弹出电池失败" + e.toString());
            //取消支付宝创建的信用借还订单
            cancelOrder(responseOrderNo);
        }
    }
    //当电池弹出失败时，取消订单
    private void cancelOrder(String responseOrderNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCancelRequest request = new ZhimaMerchantOrderRentCancelRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = "w1010100000000002858";

        request.setBizContent("{" +
                "\"order_no\":\"" + responseOrderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"" +
                "  }");
        ZhimaMerchantOrderRentCancelResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            System.out.println("订单取消" + response.getMsg());
        } else {
            System.out.println("调用失败");
            logger.error("调用撤销信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
    }
}