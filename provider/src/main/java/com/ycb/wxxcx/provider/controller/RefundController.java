package com.ycb.wxxcx.provider.controller;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.mapper.RefundMapper;
import com.ycb.wxxcx.provider.mapper.UserMapper;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.utils.RefundUtil;
import com.ycb.wxxcx.provider.utils.WXPayUtil;
import com.ycb.wxxcx.provider.vo.Refund;
import com.ycb.wxxcx.provider.vo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by 杜欣源:退款（提现）记录 on 2017/8/5.
 */

@RestController
@RequestMapping("refund")
public class RefundController {

    public static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    @Autowired(required = false)
    private RefundMapper refundMapper;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private UserMapper userMapper;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    @Value("${mch_id}")
    private String mchId;

    @Value("${key}")
    private String key;

    // 获取提现记录列表
    @RequestMapping(value = "/getRefundList", method = RequestMethod.POST)
    @ResponseBody
    public String query(@RequestParam("session") String session) {
        Map<String, Object> bacMap = new HashMap<>();
        if (StringUtils.isEmpty(session)) {
            bacMap.put("data", null);
            bacMap.put("code", 2);
            bacMap.put("msg", "失败(session不可为空)");

            return JsonUtils.writeValueAsString(bacMap);
        }
        try {
            String openid = redisService.getKeyValue(session);
            User user = this.userMapper.findUserinfoByOpenid(openid);

            List<Refund> refundList = this.refundMapper.findRefunds(user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("refunds", refundList);
            bacMap.put("data", data);
            bacMap.put("code", 0);
            bacMap.put("msg", "成功");

        } catch (Exception e) {
            logger.error(e.getMessage());
            bacMap.put("data", null);
            bacMap.put("code", 1);
            bacMap.put("msg", "获取数据失败");
        }
        return JsonUtils.writeValueAsString(bacMap);
    }

    // 申请提现(微信)
    @RequestMapping("/doRefund")
    @ResponseBody
    public String wechatRefund(HttpServletResponse response, HttpServletRequest request) throws UnsupportedEncodingException {

        //生成随机字符串
        String nonce_str = WXPayUtil.getNonce_str();

        String out_trade_no = "0001";//商户订单号
        String out_refund_no = "1000100";//商户退款单号
        String total_fee = "100";//退款金额
        String refund_fee = "100";//退款总金额

        SortedMap<String, Object> parameters = new TreeMap<String, Object>();
        parameters.put("appid", appID);//公众账号ID
        parameters.put("mch_id", mchId);//商户号
        parameters.put("nonce_str", nonce_str);//随机字符串
        // 在notify_url中解析微信返回的信息获取到 transaction_id，此项不是必填，详细请看上图文档
        // parameters.put("transaction_id", "微信支付订单中调用统一接口后微信返回的 transaction_id");
        parameters.put("out_trade_no", out_trade_no);//商户系统内部订单号
        parameters.put("out_refund_no", out_refund_no); //商户系统内部的退款单号，约束为UK唯一
        parameters.put("total_fee", total_fee); //订单总金额：单位为分
        parameters.put("refund_fee", refund_fee); //退款总金额：单位为分
        parameters.put("op_user_id", mchId);// 操作员帐号, 默认为商户号

        String xml = WXPayUtil.map2Xml(parameters, key);
        String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

        try {
            String mch_id = mchId;
            Map map = RefundUtil.forRefund(createOrderURL, xml, mch_id);
            if (map != null) {
                String return_code = (String) map.get("return_code");//返回状态码
                String result_code = (String) map.get("result_code");//业务结果
                if (return_code.equals("SUCCESS") && result_code.equals("SUCCESS")) {
                    System.out.println("退款成功");
                } else {
                    System.out.println("退款失败");
                }
            } else {
                System.out.println("退款失败");
            }
        } catch (Exception e) {
            System.out.print("退款失败");
            e.printStackTrace();
        }
        return null;
    }
}
