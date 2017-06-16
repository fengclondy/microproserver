package com.ycb.wxxcx.provider.mapper;

import com.ycb.wxxcx.provider.vo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by zhuhui on 17-6-16.
 */
@Mapper
public interface UserMapper {
    @Select("Select * from USER WHERE id = #{id}")
    User findById(@Param("id") Long id);

    @Insert("Insert INTO USER VALUES(#{name},#{age})")
    int insert(@Param("name") String name, @Param("age") Integer age);
}
