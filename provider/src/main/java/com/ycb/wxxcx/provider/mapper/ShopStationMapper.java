package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.ShopStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by duxinyuan on 17-7-26.
 */
@Mapper
public interface ShopStationMapper {

    @Select("Select id from ycb_mcs_shop_station ss WHERE ss.station_id =#{sid}")
    ShopStation findShopStationIdBySid(String sid);
}
