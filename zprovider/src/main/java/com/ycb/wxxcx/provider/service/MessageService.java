package com.ycb.wxxcx.provider.service;

import com.ycb.wxxcx.provider.cache.RedisService;
import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.MessageMapper;
import com.ycb.wxxcx.provider.mapper.RefundMapper;
import com.ycb.wxxcx.provider.utils.HttpRequest;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.Message;
import com.ycb.wxxcx.provider.vo.Refund;
import com.ycb.wxxcx.provider.vo.WechatTemplateMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by duxinyuan on 17-8-29.
 */
@Service
public class MessageService {

    public static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RefundMapper refundMapper;

    @Autowired
    private RedisService redisService;

    @Value("${appID}")
    private String appID;

    @Value("${appSecret}")
    private String appSecret;

    public String getAccessToken() throws Exception {
        String ACCESS_TOKEN = redisService.getKeyValue("ACCESS_TOKEN");
        if (StringUtils.isEmpty(ACCESS_TOKEN)) {
            String param = "grant_type=client_credential&appid=" + appID + "&secret=" + appSecret;
            try {
                String tokenInfo = HttpRequest.sendGet(GlobalConfig.WX_ACCESS_TOKEN_URL, param);
                Map<String, Object> tokenInfoMap = JsonUtils.readValue(tokenInfo);
                String accessToken = (String) tokenInfoMap.get("access_token");
                Integer expiresIn = (Integer) tokenInfoMap.get("expires_in");
                // 将accessToken存入Redis,存放时间为7200秒
                redisService.setKeyValueTimeout("ACCESS_TOKEN", accessToken, expiresIn);
                return accessToken;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return ACCESS_TOKEN;
        }
    }

    //获取form_id
    public Message getFormIdByOpenid(String openid) {
        //根据openid检索form_id
        Message message = this.messageMapper.findFormIdByOpenid(openid);
        return message;
    }

    //提现成功 推送消息
    public void refundSendTemplate(String openid, String templateid, Message message, Long refundId) {
        //查询提现记录
        Refund refund = this.refundMapper.findRefundByRefundId(refundId);
        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id(templateid);
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setPage("/pages/user/user"); //跳转页面
        wechatTemplateMsg.setForm_id(message.getFormId());
        String requestTime = refund.getRequestTime();
        TreeMap<String, TreeMap<String, String>> params = new TreeMap<String, TreeMap<String, String>>();
        params.put("keyword1", WechatTemplateMsg.item(refund.getRefund().toString() + "元", "#000000")); //提现金额
        params.put("keyword2", WechatTemplateMsg.item(requestTime.substring(0, requestTime.length() - 2), "#000000")); //提现时间
        params.put("keyword3", WechatTemplateMsg.item("提现1～3个工作日到账金额", "#000000")); //温馨提示
        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);
        //发送请求
        try {
            String token = this.getAccessToken();
            String msgUrl = GlobalConfig.WX_SEND_TEMPLATE_MESSAGE + "?access_token=" + token;
            String msgResult = HttpRequest.sendPost(msgUrl, data);  //发送post请求
            Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
            Integer errcode = (Integer) msgResultMap.get("errcode");
            String errmsg = (String) msgResultMap.get("errmsg");
            if (0 == errcode) {
                logger.info("模板消息发送成功errorCode:{" + errcode + "},errmsg:{" + errmsg + "}");
            } else {
                logger.info("模板消息发送失败errorCode:{" + errcode + "},errmsg:{" + errmsg + "}");
            }
            //如果此时的剩余使用次数为1 直接删除
            if (message.getNumber() <= 1) {
                //清除本条数据
                this.messageMapper.deleteMessageById(message.getId());
                //清除该用户过期数据
                this.messageMapper.deleteMessageByOpenid(openid);
            } else {
                //更新prepay_id的使用次数
                message.setLastModifiedBy("SYS:message");
                this.messageMapper.updateMessageNumberById(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}