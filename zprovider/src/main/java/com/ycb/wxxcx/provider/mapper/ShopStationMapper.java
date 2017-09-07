package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.FeeStrategy;
import com.ycb.wxxcx.provider.vo.ShopStation;
import org.apache.ibatis.annotations.*;

/**
 * Created by duxinyuan on 17-7-26.
 */
@Mapper
public interface ShopStationMapper {

    @Select("Select id from ycb_mcs_shop_station ss WHERE ss.station_id =#{sid}")
    ShopStation findShopStationIdBySid(String sid);

    //通過用戶id查詢交易记录
    @Select("SELECT f.* " +
            "FROM ycb_mcs_shop_station ss " +
            "LEFT JOIN ycb_mcs_fee_strategy f " +
            "ON ss.fee_settings = f.id " +
            "WHERE ss.station_id = #{stationid} ")
    @Results(value = {
            @Result(property = "name", column = "name"),
            @Result(property = "freeTime", column = "free_time"),
            @Result(property = "freeUnit", column = "free_unit"),
            @Result(property = "fixedTime", column = "fixed_time"),
            @Result(property = "fixedUnit", column = "fixed_unit"),
            @Result(property = "fixed", column = "fixed"),
            @Result(property = "fee", column = "fee"),
            @Result(property = "feeUnit", column = "fee_unit"),
            @Result(property = "maxFeeTime", column = "max_fee_time"),
            @Result(property = "maxFeeUnit", column = "max_fee_unit"),
            @Result(property = "maxFee", column = "max_fee")
    })
    FeeStrategy findFeeStrategyByStation(Long stationid);
}
