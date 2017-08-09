package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.User;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by zhuhui on 17-6-16.
 */
@Mapper
public interface UserMapper {
    @Select("Select optlock from ycb_mcs_user WHERE openid = #{openid}")
    Integer findByOpenid(@Param("openid") String openid);

    @Insert("Insert INTO ycb_mcs_user(createdBy,createdDate,optlock,openid,platform,usablemoney,deposit,refund,unsubscribe) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{openid},#{platform},#{usablemoney},#{deposit},#{refund},0)")
    void insert(User user);

    @Select("Select * from ycb_mcs_user WHERE openid = #{openid}")
    User findUserinfoByOpenid(@Param("openid") String openid);

    @Update("Update ycb_mcs_user " +
            "SET lastModifiedBy='system', lastModifiedDate=#{date}, optlock=#{version} " +
            "WHERE openid=#{openid} ")
    void update(@Param("version") Integer version, @Param("date") Date date, @Param("openid") String openid);
}
