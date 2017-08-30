package com.ycb.wxxcx.provider.constant;

/**
 * duxinyaun
 */
public class GlobalConfig {

    public GlobalConfig() {
    }

    public static final String WX_CREATORDER_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

    public static final String WX_OPENID_URL = "https://api.weixin.qq.com/sns/jscode2session";

    public static final String WX_UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    public static final String APICLIENT_CERT_P12 = "apiclient_cert.p12";

    public static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    //支付成功通知模板ID
    public static final String PAY_TEMPLATE_ID = "ZkISvi8zgBJ1EX36s9XbgOukKX6KRdBi2YNBQDx8sW8";
    //发送模板消息接口
    public static final String SEND_TEMPLATE_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";
    //提现成功通知模板ID
    public static final String REFUND_TEMPLATE_ID = "P-tsKrAI_lU-R5DN2bDphds7F0jmeOTH8zrFYLFRGy0";
}
