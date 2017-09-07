package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.Order;
import com.ycb.wxxcx.provider.vo.TradeLog;
import com.ycb.wxxcx.provider.vo.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by duxinyuan on 2017/8/5.
 */

@Mapper
public interface OrderMapper {

    //通過用戶id查詢交易记录
    @Select("SELECT *, " +
            "ssout.title borrowName , " +
            "t.borrow_time borrowTime , " +
            "ssin.title returnName , " +
            "t.return_time returnTime , " +
            "t.usefee usefee , " +
            "CASE " +
            "WHEN t.return_time is null THEN " +
            "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(t.borrow_time)) " +
            "WHEN t.return_time is not null THEN " +
            "(UNIX_TIMESTAMP(t.return_time) - UNIX_TIMESTAMP(t.borrow_time)) " +
            "END duration " +
            "FROM ycb_mcs_tradelog t " +
            "LEFT JOIN ycb_mcs_shop_station ssout " +
            "ON t.borrow_shop_station_id = ssout.id " +
            "LEFT JOIN ycb_mcs_fee_strategy f " +
            "ON ssout.fee_settings = f.id " +
            "LEFT JOIN ycb_mcs_shop_station ssin " +
            "ON t.return_shop_station_id = ssin.id " +
            "WHERE t.customer = #{customer} " +
            "AND t.status <> 0 " +
            "ORDER BY t.id DESC LIMIT 0,20")
    @Results(id = "station", value = {
            @Result(property = "orderid", column = "t.orderid"),
            @Result(property = "status", column = "t.status"),
            @Result(property = "borrowName", column = "borrowName"),
            @Result(property = "borrowTime", column = "borrowTime"),
            @Result(property = "returnName", column = "returnName"),
            @Result(property = "returnTime", column = "returnTime"),
            @Result(property = "usefee", column = "usefee"),
            @Result(property = "paid", column = "paid"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "feeStrategy", column = "fee_settings", one = @One(select = "com.ycb.wxxcx.provider.mapper.FeeStrategyMapper.findFeeStrategy"))
    })
    List<TradeLog> findTradeLogs(Long customer);

    @Select("SELECT t.orderid,t.usefee,t.paid FROM ycb_mcs_tradelog t WHERE t.status=3 AND (t.paid-t.usefee)>0 AND t.customer = #{customer}")
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

    @Select("SELECT COUNT(*) FROM ycb_mcs_tradelog WHERE status = 2 AND customer = #{id}")
    Integer findUserUseBatteryNum(User user);

    @Select("SELECT COUNT(*) FROM ycb_mcs_tradelog " +
            "WHERE status NOT in(0,1,96) " +
            "AND DATE(borrow_time)=(SELECT CURDATE()) " +
            "AND customer = #{id}")
    Integer findUserOrderNum(User user);
}
