package com.ddiv.zcfun.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FriendMapper {
    @Select("SELECT COUNT(*) FROM friend " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId} AND is_delete = 0")
    int existsFriendship(long userId, long friendId);
}
