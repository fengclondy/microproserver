package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.TradeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by duxinyuan on 2017/8/5.
 */

@Mapper
public interface OrderMapper {

    //通過用戶id查詢交易记录
    @Select("select t.orderid,t.status,t.borrow_station_name,t.borrow_time,1," +
            "t.return_station_name,t.return_time," +
            "t.usefee from ycb_mcs_tradelog t where t.customer = #{customer}")
    @Results(id = "station", value = {
            @Result(property = "orderid", column = "orderid"),
            @Result(property = "status", column = "status"),
            @Result(property = "borrowName", column = "borrow_station_name"),
            @Result(property = "orderTime", column = "borrow_time"),
            @Result(property = "feeStr", column = "1"), //目前没用，随便定义的
            @Result(property = "returnName", column = "return_station_name"),
            @Result(property = "returnTime", column = "return_time"),
            @Result(property = "lastTime", column = "last_time"),
            @Result(property = "usefee", column = "usefee")
    })
    List<TradeLog> findTradeLogs(Long customer);

    @Select("SELECT orderid FROM ycb_mcs_tradelog WHERE id = (SELECT MAX(id) FROM ycb_mcs_tradelog WHERE customer = #{customer})")
    TradeLog findOrderIdByUid(Long customer);
}
