package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.UserPO;
import com.ddiv.zcfun.domain.po.UserRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
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
    UserPO findByUserId(long id);

    @Select("select role from user_role where user_id=#{id} and is_delete=0")
    List<UserRole> findUserRoleByUserId(long id);

    @Insert("insert into video_star(user_id,video_id) values(#{userId},#{videoId})")
    void addVideoStar(long userId, long videoId);

    @Update("update video_star set is_delete=1-is_delete where user_id=#{userId} and video_id=#{videoId}")
    void updateVideoStar(long userId, long videoId);

    void addVideo();
}
