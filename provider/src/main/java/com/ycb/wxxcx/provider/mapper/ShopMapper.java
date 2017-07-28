package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Shop;
import com.ycb.wxxcx.provider.vo.ShopStation;
import com.ycb.wxxcx.provider.vo.Station;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by zhuhui on 17-7-26.
 */
@Mapper
public interface ShopMapper {

    //    @Select("Select shop.name, ss.address, ss.latitude, ss.longitude From ycb_mcs_shop shop, ycb_mcs_shop_station ss, ycb_mcs_station s " +
    //            "Where ss.shopid = shop.id And ss.station_id = s.id " +
    //            "And abs(ss.latitude - #{latitude}) < 50 " +
    //            "And abs(ss.longitude - #{longitude}) < 50 ")
    @Select("Select * From ycb_mcs_shop shop, ycb_mcs_shop_station ss, ycb_mcs_station s " +
            "Where ss.shopid = shop.id And ss.station_id = s.id " +
            "And abs(ss.latitude - #{latitude}) < 50 " +
            "And abs(ss.longitude - #{longitude}) < 50 ")
    //    @Result(property = "name", column = "shop.name")
    //    @Result(property = "address", column = "shop.address")
    //    @Result(property = "latitude", column = "shop.latitude")
    //    @Result(property = "longitude", column = "shop.longitude")
    List<Shop> findShops(@Param("latitude") String latitude, @Param("longitude") String longitude);

    @Select("Select * From ycb_mcs_station s")
    @Results(id = "station", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "usable", column = "usable"),
            @Result(property = "empty", column = "empty")
    })
    List<Station> findStations();

    @Select("Select * From ycb_mcs_shop_station ss")
    @Results(id = "shopStation", value = {
            @Result(property = "address", column = "address"),
            @Result(property = "latitude", column = "latitude"),
            @Result(property = "longitude", column = "longitude")
    })
    List<ShopStation> findShopStations();
}
