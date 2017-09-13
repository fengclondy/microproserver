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

    public static final String WX_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    //发送模板消息接口
    public static final String WX_SEND_TEMPLATE_MESSAGE = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";

    //信用借还支付宝接口
    public static final String Z_CREDIT_SERVER_URL = "https://openapi.alipay.com/gateway.do";
}
