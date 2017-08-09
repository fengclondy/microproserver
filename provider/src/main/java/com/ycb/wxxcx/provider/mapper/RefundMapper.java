package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by 杜欣源 on 2017/8/5.
 */

@Mapper
public interface RefundMapper {

    @Select("SELECT id,refund,status,request_time AS requestTime ,refund_time AS refundTime FROM ycb_mcs_refund_log WHERE uid = #{uid}")
    List<Refund> findRefunds(Long uid);
}
