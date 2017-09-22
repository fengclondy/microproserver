package com.ycb.zprovider.mapper;

import com.ycb.zprovider.vo.FeeStrategy;
import com.ycb.zprovider.vo.ShopStation;
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

    //根据商铺的id查询商铺信息为了上传商铺信息使用
    @Select("select ss.longitude,ss.latitude,ss.address,s.stime,s.etime,s.phone,s.id from ycb_mcs_shop s,ycb_mcs_shop_station ss WHERE s.id = ss.shopid AND s.id = #{id}")
    @Results(value = {
            @Result(property = "longitude",column = "longitude"),
            @Result(property = "latitude",column = "latitude"),
            @Result(property = "address",column = "address"),
            @Result(property = "stime",column = "stime"),
            @Result(property = "etime",column = "etime"),
            @Result(property = "phone",column = "phone"),
            @Result(property = "id",column = "id")
    })
    ShopStation findShopStationById(@Param("id") Long id);
}
