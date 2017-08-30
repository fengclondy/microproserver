package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Order;
import com.ycb.wxxcx.provider.vo.TradeLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by duxinyuan on 2017/8/5.
 */

@Mapper
public interface OrderMapper {

    //通過用戶id查詢交易记录
    @Select("SELECT *,(UNIX_TIMESTAMP(t.return_time) - UNIX_TIMESTAMP(t.borrow_time)) duration " +
            "FROM ycb_mcs_tradelog t " +
            "LEFT JOIN ycb_mcs_shop_station ss " +
            "ON t.borrow_shop_station_id = ss.id " +
            "LEFT JOIN ycb_mcs_fee_strategy f " +
            "ON ss.fee_settings = f.id " +
            "WHERE t.customer = #{customer} " +
            "AND t.status <> 0 " +
            "ORDER BY t.id DESC LIMIT 0,20")
    @Results(id = "station", value = {
            @Result(property = "orderid", column = "orderid"),
            @Result(property = "status", column = "status"),
            @Result(property = "borrowName", column = "borrow_station_name"),
            @Result(property = "borrowTime", column = "borrow_time"),
            @Result(property = "returnName", column = "return_station_name"),
            @Result(property = "returnTime", column = "return_time"),
            @Result(property = "usefee", column = "usefee"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "feeStrategy", column = "fee_settings", one = @One(select = "com.ycb.wxxcx.provider.mapper.FeeStrategyMapper.findFeeStrategy"))
    })
    List<TradeLog> findTradeLogs(Long customer);

    @Select("SELECT t.orderid,t.usefee,t.paid FROM ycb_mcs_tradelog t WHERE t.status=3 AND t.customer = #{customer}")
    List<Order> findOrderListIdByUid(Long customer);

    @Insert("INSERT INTO ycb_mcs_tradelog(createdBy,createdDate,optlock," +
            "borrow_city,borrow_station_name,borrow_time,orderid,paid," +
            "platform,price,status,usefee,customer,borrow_shop_id,borrow_shop_station_id," +
            "borrow_station_id,cable) VALUES(#{createdBy},#{createdDate},0,#{borrowCity}," +
            "#{borrowStationName},#{borrowTime},#{orderid},#{paid},#{platform},#{price},#{status}," +
            "#{usefee},#{customer},#{borrowShopId},#{borrowShopStationId},#{borrowStationId},#{cable})")
    void saveOrder(Order order);

    @Update("UPDATE ycb_mcs_tradelog SET " +
            "lastModifiedBy=#{lastModifiedBy}, " +
            "lastModifiedDate=#{lastModifiedDate}, " +
            "status=#{status}, " +
            "paid=#{paid} " +
            "WHERE orderid=#{orderid}")
    void updateOrderStatus(Order order);

    @Select("SELECT customer " +
            "FROM ycb_mcs_tradelog where orderid = #{orderid}")
    Long getCustomer(String orderid);

    @Update("UPDATE ycb_mcs_tradelog SET " +
            "lastModifiedBy=#{lastModifiedBy}, " +
            "lastModifiedDate=NOW(), " +
            "refunded=#{refunded}, " +
            "status=#{status} " +
            "WHERE orderid=#{orderid}")
    void updateOrderStatusToFour(Order order);

    @Select("SELECT status " +
            "FROM ycb_mcs_tradelog where orderid = #{orderid}")
    Integer getOrderStatus(String outTradeNo);

    @Select("SELECT case when count(*) > 0 then 1 else 0 end result from ycb_mcs_tradelog " +
            "WHERE borrow_station_id = #{stationid} " +
            "AND customer = #{customer} " +
            "AND status >1 " +
            "AND status <5 " +
            "AND borrow_time > DATE_FORMAT(CURDATE(),'%Y-%m-%d %H:%i:%s') ")
    Boolean findTodayOrder(@Param("stationid") Long stationid, @Param("customer") Long customer);
}
