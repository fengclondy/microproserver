package com.ycb.zprovider.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayOpenPublicMessageSingleSendRequest;
import com.alipay.api.request.ZhimaMerchantOrderRentCancelRequest;
import com.alipay.api.request.ZhimaMerchantOrderRentCreateRequest;
import com.alipay.api.request.ZhimaMerchantOrderRentQueryRequest;
import com.alipay.api.response.AlipayOpenPublicMessageSingleSendResponse;
import com.alipay.api.response.ZhimaMerchantOrderRentCancelResponse;
import com.alipay.api.response.ZhimaMerchantOrderRentCreateResponse;
import com.alipay.api.response.ZhimaMerchantOrderRentQueryResponse;
import com.ycb.zprovider.cache.RedisService;
import com.ycb.zprovider.constant.GlobalConfig;
import com.ycb.zprovider.mapper.*;
import com.ycb.zprovider.service.FeeStrategyService;
import com.ycb.zprovider.service.SocketService;
import com.ycb.zprovider.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/createOrder", method = RequestMethod.POST)
    @ResponseBody
    //sid   设备id
    //cableType 数据线类型
    //session   用户的session，去redis中进行比对查询
    public void createOrder(@RequestParam("sid") String sid, @RequestParam("cable_type") String cableType, @RequestParam("session") String session) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL,
                appId, privateKey, format, charset, alipayPublicKey,
                signType);
        ZhimaMerchantOrderRentCreateRequest request = new ZhimaMerchantOrderRentCreateRequest();


        //回调到商户的url地址
        //商户在组装订单创建https请求时，会附带invoke_return_url参数，当用户完成借用准入及资金处理后，
        // 在借用完成页面会自动回调到商户提供的invoke_return_url地址链接，目前商户链接跳转是通过自动跳转的方式实现。
        String invokeReturnUrl = "http://www.duxinyuan.top/return_url.html";

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
        String productCode = GlobalConfig.Z_PRODUCT_CODE;
        /*
        物品名称,最长不能超过14个汉字
         */
        String goodsName = "充电宝";
        /*
        租金信息描述 ,长度不超过14个汉字，只用于页面展示给C端用户，除此之外无其他意义。
         */
        String rentInfo = "1小时免费，1元/天";
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
//        String rentAmount = feeStrategy.getFixed().toString();
        String rentAmount = "0";
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
        long l = borrowDate.getTime() + maxFeeTime * maxFeeUnit * 1000;
        //示例：2017-04-30 12:06:31
        //     2017-09-11 13:09:23
        //到期时间，是指最晚归还时间，表示借用用户如果超过此时间还未完结订单（未归还物品或者未支付租金）将会进入逾期状态，
        // 芝麻会给借用用户发送催收提醒。如果此时间不传入或传空，将视为无限期借用
        String expiryTime = new SimpleDateFormat("YYYY-MM-dd HH:MM:ss").format(new Date(l));

//        //根据session从redis中查询用户的open_id
//        String openid = redisService.getKeyValue(session);
//        User user = this.userMapper.findUserinfoByOpenid(openid);

        //借用用户的手机号码，可选字段。推荐商户传入此值，会将此手机号码与用户身份信息进行匹配验证，防范欺诈风险。
        String mobileNo = "";

        //借用用户的收货地址，可选字段。推荐商户传入此值，会将此手机号码与用户身份信息进行匹配验证，防范欺诈风险。
        String address = "";

        //创建一个未支付订单
        createPreOrder(outOrderNo, sid, cableType, session);

        request.setBizContent("{" +
                "\"invoke_type\":\"WINDOWS\"," +
                "\"invoke_return_url\":\"" + invokeReturnUrl + "\"," +
                "\"out_order_no\":\"" + outOrderNo + "\"," +
                "\"product_code\":\"" + productCode + "\"," +
                "\"goods_name\":\"" + goodsName + "\"," +
                "\"rent_info\":\"" + rentInfo + "\"," +
                "\"rent_unit\":\"" + rentUnit + "\"," +
                "\"rent_amount\":\"" + rentAmount + "\"," +
                "\"deposit_amount\":\"" + depositAmount + "\"," +
                "\"deposit_state\":\"" + depositState + "\"," +
                "\"borrow_shop_name\":\"" + borrowShopName + "\"," +
                "\"name\":\"" + name + "\"," +
                "\"cert_no\":\"" + certNo + "\"," +
                "\"rent_settle_type\":\"" + rentSettleType + "\"," +
                "\"borrow_time\":\"" + borrowTime + "\"," +
                "\"expiry_time\":\"" + expiryTime +
                "  }");
        ZhimaMerchantOrderRentCreateResponse response = null;
        try {
            response = alipayClient.pageExecute(request, "GET"); // 这里一定要用GET模式
            String url = response.getBody(); // 从body中获取url
            System.out.println("generateRentUrl url:" + url);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功，信用借还订单创建成功");
        } else {
            System.out.println("调用失败");

            logger.error("调用创建信用借还订单失败，错误代码：" + response.getCode() + "错误信息：" + response.getMsg() +
                    "错误子代码" + response.getSubCode() + "错误子信息：" + response.getSubMsg());
        }
    }

    /*
    当用户发起借用的请求的时候，生成订单存储到数据库中，
     */
    private void createPreOrder(String outOrderNo, String sid, String cableType, String session) {
        //根据session从redis中查询用户的open_id
        String openid = redisService.getKeyValue(session);
        User user = this.userMapper.findUserinfoByOpenid(openid);

        //向ycb_mcs_tradelog中存入数据
        Order order = new Order();
        //设置用户编号
        order.setCustomer(user.getId());
        order.setCreatedBy("SYS:createcreditpay");
        order.setCreatedDate(new Date());
        //设置租借时间
        order.setBorrowTime(new Date());
        //设置订单编号，为商户自己的订单编号，格式为yyyyMMddHHmmss+4位随机数
        order.setOrderid(outOrderNo);//订单编号
        order.setPlatform(2);//平台(信用借还)
        //根据sid查询设备所在店铺的信息
        Shop responseShop = shopMapper.getShopInfoBySid(sid);
        order.setPrice(responseShop.getDefaultPay());//商品价格(元)，从数据库中查询押金defaultPay
        order.setPaid(BigDecimal.ZERO);//已支付的费用
        order.setUsefee(BigDecimal.ZERO);//产生的费用

        order.setCable(Integer.valueOf(cableType));
        order.setStatus(0);//支付状态,0为未支付，1为已经支付
        order.setBorrowShopId(responseShop.getId());
        //根据返回来的sid，查询到设备所在商铺的信息，和设备的信息
        ShopStation shopStation = shopStationMapper.findShopStationIdBySid(sid);
        Station station = stationMapper.getStationBySid(sid);
        order.setBorrowShopStationId(shopStation.getId());
        order.setBorrowStationId(station.getId());
        //设置租借地点
        order.setBorrowStationName(responseShop.getName());
        order.setBorrowCity(responseShop.getCity());
        orderMapper.saveOrder(order);
    }

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
                    String productCode = GlobalConfig.Z_PRODUCT_CODE;
                    zhimaMerchantOrderRentQueryRequest.setBizContent("{" +
                            "\"out_order_no\":\"" + outOrderNo + "\"," +
                            "\"product_code\":\"" + productCode + "\"" +
                            "  }");
                    ZhimaMerchantOrderRentQueryResponse zhimaResponse = null;
                    try {
                        zhimaResponse = alipayClient.execute(zhimaMerchantOrderRentQueryRequest);
                    } catch (AlipayApiException e) {
                        e.printStackTrace();
                    }
                    if (zhimaResponse.isSuccess()) {
                        System.out.println("调用成功");

                        updateOrder(zhimaResponse);

//                        //向ycb_mcs_tradelog中存入数据
//                        Order updateorder = new Order();
//
//                        //借用人支付宝userId.	例如2088202924240029
//                        String responseUserId = ZhimaResponse.getUserId();
//                        //信用借还的订单号,例如100000
//                        String responseOrderNo = ZhimaResponse.getOrderNo();
//                        //资金流水号，用于商户与支付宝进行对账	2088000000000000
//                        String responseAlipayFundOrderNo = ZhimaResponse.getAlipayFundOrderNo();
//
//                        updateorder.setLastModifiedBy("SYS:completecreditpay");
//                        updateorder.setLastModifiedDate(new Date());
//
//                        //因为这里只返回了信用借还的订单号，所以需要根据信用借还的订单号进行更新订单
//                        updateorder.setOrderNo(responseOrderNo);
//                        updateorder.setAlipayFundOrderNo(responseAlipayFundOrderNo);
//
//                        orderMapper.updateOrderStatusByOrderNo(updateorder);
                    } else {
                        System.out.println("调用失败");
                    }
                }

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
                    String productCode = GlobalConfig.Z_PRODUCT_CODE;
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

    /*
    当调用支付宝的创建信用借还订单接口成功时，更新订单的状态，弹出电池，弹出成功，
     */
//    private void updateOrder(ZhimaMerchantOrderRentCreateResponse response, String responseSid, String responseCableType) {
    private void updateOrder(ZhimaMerchantOrderRentQueryResponse response) {
        //芝麻信用借还订单号
        String responseOrderNo = response.getOrderNo();
        //根据芝麻信用借还订单号查询订单详情
        Order order = orderMapper.findOrderByOrderNo(responseOrderNo);
        //从订单中获取设备的sid和cabletype
        String responseSid = order.getBorrowStationId().toString();
        String responseCableType = order.getCable().toString();
        //获取设备的mac，在弹出电池时会使用
        String mac = stationMapper.getStationMac(Long.valueOf(responseSid));

        //借用者的userId
        String responseUserId = response.getUserId();

        //向ycb_mcs_tradelog中存入数据
//        Order order = new Order();
        order.setCreatedBy("SYS:updatecreditpay");
        order.setCreatedDate(new Date());
        order.setStatus(1);//支付状态,0为未支付，1为已经支付
        order.setOrderNo(responseOrderNo);
        //弹出电池
        try {
            socketService.SendCmd("ACT:borrow_battery;EVENT_CODE:1;STATIONID:" + responseSid + ";MAC:" + mac + ";ORDERID:" + responseOrderNo + ";COLORID:7;CABLE:" + responseCableType + ";\r\n");
            orderMapper.updateOrderStatusByOrderId(order);
            //电池弹出成功，调用发送消息的接口给用户发送消息
            sendMassage(responseUserId, order);
        } catch (IOException e) {
            logger.error("信用借还订单弹出电池失败" + e.toString());
            //取消支付宝创建的信用借还订单
            cancelOrder(responseOrderNo);
            //发送租借超时通知给用户
            sendErrorMessage(responseOrderNo);
        }
    }

    //发送租借超时通知给用户
    //orderNo   芝麻信用借还订单号
    private void sendErrorMessage(String orderNo) {
        Order order = orderMapper.findOrderByOrderNo(orderNo);
        //获取order订单的商户的编号
        String orderid = order.getOrderid();
        //返回给哪个用户
        String responseUserId = order.getCustomer().toString();
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        AlipayOpenPublicMessageSingleSendRequest request = new AlipayOpenPublicMessageSingleSendRequest();

        //消息模板ID
        String templateId = "fe6e474648a34949a053cd374cf0cfb5".trim();

        //顶部色条的色值
        String headColor = "#000000";

        //点击消息后承接页的地址
        String url = "";
        //底部链接描述文字，如“查看详情”
        String actionName = "查看详情";

        //当前文字颜色
        String firstColor = "#000000";
        //模板中占位符的值
        String firstValue = "本次租借超时，点击详情重新租借";

        //keyword1
        String keyword1Color = "#000000";
        String keyword1Value = orderid;

        //remark
        String remarkColor = "#32cd32";
        String remarkValue = "非常抱歉，您此次租借未成功，您可点击详情重新租借。如有疑问，请致电：4006290808";

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

    //当电池弹出失败时，取消订单
    private void cancelOrder(String responseOrderNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        ZhimaMerchantOrderRentCancelRequest request = new ZhimaMerchantOrderRentCancelRequest();
        //信用借还的产品码:w1010100000000002858
        String productCode = GlobalConfig.Z_PRODUCT_CODE;

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

    //用户借用成功后，调用发送消息的接口给用户发送借用成功的通知
    //responseUserId        用户ID
    private void sendMassage(String responseUserId, Order order) {
        AlipayClient alipayClient = new DefaultAlipayClient(GlobalConfig.Z_CREDIT_SERVER_URL, appId, privateKey, format, charset, alipayPublicKey, signType);
        AlipayOpenPublicMessageSingleSendRequest request = new AlipayOpenPublicMessageSingleSendRequest();

        //消息模板ID
        String templateId = "24b75e2d6fb442798d0d8565f02f9a1b".trim();

        //顶部色条的色值
        String headColor = "#000000";

        //点击消息后承接页的地址
        String url = "";
        //底部链接描述文字，如“查看详情”
        String actionName = "查看详情";

        //当前文字颜色
        String firstColor = "#000000";
        //模板中占位符的值
        String firstValue = "信用借还，免押金借用充电宝";

        //keyword1
        String keyword1Color = "#000000";
        String keyword1Value = order.getBorrowStationName();

        //keyword2
        String keyword2Color = "#000000";
        String keyword2Value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getBorrowTime());

        //keyword3
        String keyword3Color = "#000000";
        String keyword3Value = order.getOrderNo();

        //remark
        String remarkColor = "#32cd32";
        String remarkValue = "充电宝自带一根多功能充电线，插头的AB面分别支持苹果/安卓，如插入后没响应，更换另一面即可。";

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