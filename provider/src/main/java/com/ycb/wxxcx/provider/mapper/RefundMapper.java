package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Refund;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by duxinyuan on 2017/8/5.
 */

@Mapper
public interface RefundMapper {

    @Select("SELECT id,refund,status,request_time AS requestTime ,refund_time AS refundTime FROM ycb_mcs_refund_log WHERE uid = #{uid}")
    List<Refund> findRefunds(Long uid);

    @Insert("INSERT INTO ycb_mcs_refund_log(createdBy,createdDate,optlock,refund,request_time,orderid,status,uid) " +
            "VALUES(#{createdBy},NOW(),#{version},#{refund},NOW(),#{orderid},#{status},#{uid})")
    Integer insertRefund(Refund refund);

    @Select("SELECT MAX(id) id FROM ycb_mcs_refund_log WHERE uid = #{uid}")
    Refund findRefundIdByUid(Long uid);

    @Update("UPDATE ycb_mcs_refund_log SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),refund_time=NOW(),refunded=#{refund},status=#{status} WHERE id=#{id}")
    void updateStatus(Refund refund);
}
