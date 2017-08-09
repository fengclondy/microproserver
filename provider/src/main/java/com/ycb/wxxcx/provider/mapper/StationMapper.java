package com.ycb.wxxcx.provider.mapper;

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

}
