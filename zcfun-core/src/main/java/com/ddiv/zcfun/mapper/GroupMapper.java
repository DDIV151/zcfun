package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.im.GroupPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GroupMapper {

    public GroupPO getGroupById(@Param("groupId") long groupId);

    public List<GroupPO> getGroupsByMemberId(@Param("userId") long userId);

    @Insert("insert into group(creator_id,group_name,avatar_url) values(#{creatorId},#{groupName},#{avatarUrl})")
    public int insertGroup(GroupPO group);

    public int updateGroup(GroupPO group);

    @Insert("insert into group_user(user_id,group_id) values (#{userId},#{groupId})")
    public int insertUserToGroup(@Param("groupId") long groupId, @Param("userId") long userId);

    @Update("update group_user set is_delete=1-is_delete where user_id=#{userId} and group_id=#{groupId}")
    public int updateUserToGroup(@Param("userId") long userId, @Param("groupId") long groupId);

    public void deleteGroupById(@Param("groupId") long groupId);
}
