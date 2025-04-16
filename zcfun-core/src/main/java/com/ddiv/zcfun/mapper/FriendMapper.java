package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.BlockPO;
import com.ddiv.zcfun.domain.po.im.FriendPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FriendMapper {
    @Select("SELECT COUNT(*) FROM friend " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId} AND is_delete = 0")
    int existsFriendship(long userId, long friendId);

    @Insert("INSERT INTO friend (user_id, friend_id, is_delete) " +
            "VALUES (#{userId}, #{friendId}, #{isDelete})")
    int insertFriend(FriendPO friendPO);

    @Update("UPDATE friend SET is_delete = 1-is_delete " +
            "WHERE user_id = #{userId} AND friend_id = #{friendId}")
    int updateFriend(long userId, long friendId);

    @Select("SELECT user_id,friend_id,create_at,updated_at FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} AND is_delete = 0")
    FriendPO getFriend(long userId, long friendId);

    // 检查是否存在屏蔽关系
    @Select("SELECT COUNT(*) FROM block " +
            "WHERE user_id = #{userId} AND blocked_id = #{blockedId} AND is_delete = 0")
    int existsBlock(long userId, long blockedId);

    // 添加屏蔽关系
    @Insert("INSERT INTO block (user_id, blocked_id) " +
            "VALUES (#{userId}, #{blockedId})")
    int insertBlock(BlockPO blockPO);

    // 取消屏蔽关系
    @Update("UPDATE block SET is_delete = 1 - is_delete " +
            "WHERE user_id = #{userId} AND blocked_id = #{blockedId}")
    int updateBlock(long userId, long blockedId);

    // 获取屏蔽关系
    @Select("SELECT user_id, blocked_id, created_at, updated_at FROM block " +
            "WHERE user_id = #{userId} AND blocked_id = #{blockedId} AND is_delete = 0")
    BlockPO getBlock(long userId, long blockedId);

    @Select("SELECT user_id,friend_id,create_at,updated_at FROM friend WHERE is_delete=0 AND (user_id=#{userId} OR friend_id=#{userId})")
    List<FriendPO> getFriends(long userId);

    @Select("SELECT user_id,blocked_id,created_at,updated_at FROM block WHERE is_delete=0 AND user_id=#{userId})")
    List<FriendPO> getBlockedUsers(long userId);
}