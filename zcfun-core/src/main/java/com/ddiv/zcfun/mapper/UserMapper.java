package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.domain.po.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    void addUser(UserPO user);

    @Insert("insert into user_role(user_id,role) values(#{userId},#{role})")
    void addRole(long userId, UserRole role);

    @Select("select user_id,username,password,avatar_url,create_at,updated_at from user where username=#{username} and is_delete=0")
    UserPO findByUserName(String username);

    void cancelUserAdd(UserPO userPO);

    @Select("select user_id,username,password,avatar_url,create_at,updated_at from user where user_id=#{userId} and is_delete=0")
    UserPO findByUserId(long userId);

    @Select("select role from user_role where user_id=#{id} and is_delete=0")
    List<UserRole> findUserRoleByUserId(long id);

    @Update("update user set username=#{username} where user_id=#{id} and is_delete=0")
    int updateUsername(long id, String username);

    @Update("update user set password=#{encode} where user_id=#{id} and is_delete=0")
    int updatePassword(long id, String encode);

}
