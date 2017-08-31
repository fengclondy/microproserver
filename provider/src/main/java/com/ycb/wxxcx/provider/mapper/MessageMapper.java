package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Message;
import org.apache.ibatis.annotations.*;

/**
 * Created by duxinyuan on 2017/8/28.
 */

@Mapper
public interface MessageMapper {

    @Insert("INSERT INTO ycb_mcs_message(createdBy,createdDate,optlock,form_prepay_id,openid,number,type) " +
            "VALUES(#{createdBy},NOW(),#{version},#{formId},#{openid},#{number},#{type})")
    void insertFormId(Message message);

    @Select("SELECT m.id,m.openid,m.createdDate,m.form_prepay_id formId,m.number FROM ycb_mcs_message m WHERE m.orderid=#{outTradeNo} order by m.createdDate limit 1")
    Message findPrepayIdByOrderid(String outTradeNo);

    @Insert("INSERT INTO ycb_mcs_message(createdBy,createdDate,optlock,form_prepay_id,openid,orderid,number,type) " +
            "VALUES(#{createdBy},NOW(),#{version},#{prepayId},#{openid},#{orderid},#{number},#{type})")
    void insertPrepayIdMessage(Message message);

    @Delete("DELETE FROM ycb_mcs_message WHERE id=#{id}")
    void deleteMessageById(Long id);

    @Select("SELECT m.id,m.openid,m.createdDate,m.form_prepay_id formId,m.type,m.number,m.orderid FROM ycb_mcs_message m " +
            "WHERE m.openid=#{openid} AND m.form_prepay_id<>'the formId is a mock one' AND m.createdDate > DATE_SUB(CURDATE(), INTERVAL 1 WEEK) " +
            "order by m.createdDate limit 1")
    Message findFormIdByOpenid(String openid);

    @Update("UPDATE ycb_mcs_message SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),number=number-1 WHERE id = #{id}")
    void updateMessageNumberById(Message message);

    @Delete("DELETE FROM ycb_mcs_message WHERE openid=#{openid} AND createdDate <= DATE_SUB(CURDATE(), INTERVAL 1 WEEK)")
    void deleteMessageByOpenid(String openid);
}
