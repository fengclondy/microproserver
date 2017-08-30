package com.ycb.wxxcx.provider.service;

import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.mapper.MessageMapper;
import com.ycb.wxxcx.provider.mapper.RefundMapper;
import com.ycb.wxxcx.provider.utils.AccessToken;
import com.ycb.wxxcx.provider.utils.HttpRequest;
import com.ycb.wxxcx.provider.utils.JsonUtils;
import com.ycb.wxxcx.provider.vo.Message;
import com.ycb.wxxcx.provider.vo.Refund;
import com.ycb.wxxcx.provider.vo.WechatTemplateMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    //获取form_id
    public Message getFormIdByOpenid(String openid){

        //根据openid检索form_id
        Message message = this.messageMapper.findPrepayIdByOpenid(openid);
        if (null !=message){
            //判断时间是否过期
            Date createdDate = message.getCreatedDate();
            Date nowTime = new Date();
            Long diff = (nowTime.getTime() - createdDate.getTime()) / 1000; //秒
            if (diff<60*60*24*7){
                return message;
            }else {
                //过期 从数据库删除
                this.messageMapper.deleteMessageById(message.getId());
            }
        }else {
            //没有可用的form_id了
            return null;
        }
        return null;
    }

    //获取prepay_id
    public Message getPrepayId(String outTradeNo){

        //根据订单编号检索
        Message message = this.messageMapper.findPrepayIdByOrderid(outTradeNo);
        if (null !=message){
            //判断时间是否过期
            Date createdDate = message.getCreatedDate();
            Date nowTime = new Date();
            Long diff = (nowTime.getTime() - createdDate.getTime()) / 1000; //秒
            if (diff<60*60*24*7){
                //未过期 判断是否还可以使用
               Integer i = message.getNumber();
               if (0<i){
                   //可以使用
                    return message;
               }else {
                   //prepay_id已经使用够3次了 从数据库删除
                   this.messageMapper.deleteMessageById(message.getId());
               }
            }else {
                //过期 从数据库删除
                this.messageMapper.deleteMessageById(message.getId());
            }
        }else {
            //没有可用的prepay_id了
            return null;
        }
        return null;
    }

    //支付成功 推送消息
    public void paySendTemplate(String openid, String templateid,String formid,String orderid){

        TreeMap<String,TreeMap<String,String>> params = new TreeMap<String,TreeMap<String,String>>();
        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id(templateid);
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setPage("********");
        wechatTemplateMsg.setForm_id(formid);

        //todo 查询订单信息

        params.put("keyword1",WechatTemplateMsg.item("8.1发现尼泊尔—人文与自然的旅行圣地", "#000000"));
        params.put("keyword2",WechatTemplateMsg.item("2017.1.2", "#000000"));
        params.put("keyword3",WechatTemplateMsg.item("订单编号***", "#000000"));
        params.put("keyword4",WechatTemplateMsg.item("备注", "#000000"));

        wechatTemplateMsg.setData(params);
        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);

        //发送请求
        try {
            AccessToken accessToken = new AccessToken();
            String token = accessToken.getAccessToken();
            String msgUrl = GlobalConfig.SEND_TEMPLATE_MESSAGE+"send?access_token="+token;

            String msgResult = HttpRequest.sendPost(msgUrl,data);  //发送post请求
            Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
            Integer errcode = (Integer)msgResultMap.get("errcode");
            String errmsg = (String)msgResultMap.get("errmsg");
            if(0 == errcode){
                //result = true;
                logger.info("模板消息发送成功errorCode:{"+errcode+"},errmsg:{"+errmsg+"}");
            }else{
                logger.info("模板消息发送失败errorCode:{"+errcode+"},errmsg:{"+errmsg+"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //提现成功 推送消息
    public void refundSendTemplate(String openid, String templateid, Message message, Long refundId){

        //查询提现记录
        Refund refund = this.refundMapper.findRefundByRefundId(refundId);

        //根据具体模板参数组装
        WechatTemplateMsg wechatTemplateMsg = new WechatTemplateMsg();
        wechatTemplateMsg.setTemplate_id(templateid);
        wechatTemplateMsg.setTouser(openid);
        wechatTemplateMsg.setPage("index.jsp"); //跳转页面
        wechatTemplateMsg.setForm_id(message.getFormId());

        TreeMap<String,TreeMap<String,String>> params = new TreeMap<String,TreeMap<String,String>>();
        params.put("keyword1",WechatTemplateMsg.item(refund.getRefund().toString(), "#000000")); //提现金额
        params.put("keyword2",WechatTemplateMsg.item(refund.getRefundTime(), "#000000")); //提现时间
        params.put("keyword3",WechatTemplateMsg.item("提现1～3个工作日到账金额", "#000000")); //温馨提示
        wechatTemplateMsg.setData(params);

        String data = JsonUtils.writeValueAsString(wechatTemplateMsg);

        //发送请求
        try {
            AccessToken accessToken = new AccessToken();
            String token = accessToken.getAccessToken();
            String msgUrl = GlobalConfig.SEND_TEMPLATE_MESSAGE+"send?access_token="+token;

            String msgResult = HttpRequest.sendPost(msgUrl,data);  //发送post请求
            Map<String, Object> msgResultMap = JsonUtils.readValue(msgResult);
            Integer errcode = (Integer)msgResultMap.get("errcode");
            String errmsg = (String)msgResultMap.get("errmsg");
            if(0 == errcode){
                logger.info("模板消息发送成功errorCode:{"+errcode+"},errmsg:{"+errmsg+"}");
            }else{
                logger.info("模板消息发送失败errorCode:{"+errcode+"},errmsg:{"+errmsg+"}");
            }
            //删除form_id
            this.messageMapper.deleteMessageById(message.getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
