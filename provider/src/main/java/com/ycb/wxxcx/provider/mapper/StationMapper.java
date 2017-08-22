package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Station;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhuhui on 17-8-7.
 */
@Mapper
public interface StationMapper {
    @Select("Select usable_battery From ycb_mcs_station s " +
            "Where s.id = #{sid} ")
    String getUsableBatteries(@Param("sid") Long sid);

    @Select("Select mac From ycb_mcs_station s " +
            "Where s.id = #{sid} ")
    String getStationMac(@Param("sid") Long sid);

    @Select("SELECT s.id,s.title FROM ycb_mcs_station s WHERE s.id=#{sid}")
    Station getStationBySid(String sid);

    @Select("SELECT t.cable, s.mac, t.customer FROM ycb_mcs_tradelog t,ycb_mcs_station s WHERE t.borrow_station_id=s.id AND t.orderid=#{outTradeNo}")
    Station getMacCableByOrderid(String outTradeNo);
}
