package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Frequency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by duxinyuan on 2017/8/28.
 */

@Mapper
public interface FrequencyMapper {

    @Select("SELECT f.battery_num batteryNum,f.order_num orderNum FROM ycb_mcs_frequency f ORDER BY createdDate DESC LIMIT 1")
    Frequency findFrequency();
}
