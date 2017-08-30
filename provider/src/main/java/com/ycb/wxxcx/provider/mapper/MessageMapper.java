package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by duxinyuan on 2017/8/28.
 */

@Mapper
public interface MessageMapper {

    @Insert("INSERT INTO ycb_mcs_message(createdBy,createdDate,optlock,form_prepay_id,openid,type) " +
            "VALUES(#{createdBy},NOW(),#{version},#{formId},#{openid},#{type})")
    void insertMessage(Message message);

//    @Select("SELECT MAX(id) id FROM ycb_mcs_message m WHERE m.openid=#{openid}")
//    Long findMessageByOpenid(Message message);

//    @Update("UPDATE ycb_mcs_message m SET m.prepay_id=#{prepayId},m.orderid=#{orderid} WHERE m.id=#{id}")
//    void updateMessagePrepayIdByMid(Message message);

    @Select("SELECT m.id,m.openid,m.createdDate,m.form_prepay_id prepayId,m.number FROM ycb_mcs_message m WHERE m.orderid=#{outTradeNo} order by m.createdDate limit 1")
    Message findPrepayIdByOrderid(String outTradeNo);

    @Insert("INSERT INTO ycb_mcs_message(createdBy,createdDate,optlock,form_prepay_id,openid,orderid,number,type) " +
            "VALUES(#{createdBy},NOW(),#{version},#{prepayId},#{openid},#{orderid},#{number},#{type})")
    void insertPrepayIdMessage(Message message);

    @Delete("DELETE FROM ycb_mcs_message WHERE id=#{id}")
    void deleteMessageById(Long id);

    @Select("SELECT m.id,m.openid,m.createdDate,m.form_prepay_id formId FROM ycb_mcs_message m WHERE m.openid=#{openid} order by m.createdDate limit 1")
    Message findPrepayIdByOpenid(String openid);

}
