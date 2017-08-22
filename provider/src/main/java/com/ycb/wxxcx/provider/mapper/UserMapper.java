package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.User;
import com.ycb.wxxcx.provider.vo.UserInfo;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.Date;

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

    @Select("SELECT u.id,ui.nickname,u.usablemoney,ui.headimgurl,u.deposit,u.refund FROM ycb_mcs_user u,ycb_mcs_userinfo ui WHERE u.openid=ui.openid AND u.openid = #{openid}")
    UserInfo findUserinfo(@Param("openid") String openid);

    @Select("SELECT * FROM ycb_mcs_user WHERE openid = #{openid}")
    User findUserinfoByOpenid(@Param("openid") String openid);

    @Update("Update ycb_mcs_user " +
            "SET lastModifiedBy='system', lastModifiedDate=#{date}, optlock=#{version} " +
            "WHERE openid=#{openid} ")
    void update(@Param("version") Integer version, @Param("date") Date date, @Param("openid") String openid);

    @Select("Select id,usablemoney from ycb_mcs_user WHERE openid = #{openid}")
    User findUserMoneyByOpenid(String openid);

    @Update("Update ycb_mcs_user SET lastModifiedBy=#{lastModifiedBy},lastModifiedDate=NOW(),deposit=0,usablemoney=0 WHERE id=#{id}")
    void updateUsablemoneyByUid(User user);

    @Select("Select id, usablemoney from ycb_mcs_user WHERE openid = #{openid}")
    User findUserIdByOpenid(String openid);

    @Update("Update ycb_mcs_user SET lastModifiedBy='SYS:pay',lastModifiedDate=NOW(),optlock =optlock+1,deposit=deposit+#{paid} WHERE id=#{customerid}")
    void updateUserDeposit(@Param("paid") BigDecimal paid, @Param("customerid") Long customerid);

    @Update("Update ycb_mcs_user SET lastModifiedBy='SYS:pay',lastModifiedDate=NOW(),deposit=deposit+#{deposit},usablemoney=usablemoney-#{usablemoney} WHERE id=#{id}")
    void updateUserDepositUsable(BigDecimal useMoney, Long id);

    @Insert("Insert INTO ycb_mcs_userinfo(createdBy,createdDate,optlock,openid,unionid,nickname,sex,language,city,province,country,headimgurl) " +
            "VALUES(#{createdBy},#{createdDate},#{version},#{openid},#{unionid},#{nickname},#{sex},#{language},#{city},#{province},#{country},#{headimgurl})")
    void insertUserInfo(UserInfo userInfo);
}
