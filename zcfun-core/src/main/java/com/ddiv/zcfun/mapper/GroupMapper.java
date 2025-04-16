package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.im.GroupPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMapper {

    GroupPO getGroupById(@Param("groupId") long groupId);

    List<GroupPO> getGroupsByMemberId(@Param("userId") long userId);

    @Insert("insert into `group`(creator_id,group_name) values(#{creatorId},#{groupName})")
    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    int insertGroup(GroupPO group);

    int updateGroup(GroupPO group);

    @Insert("insert into group_user(user_id,group_id) values (#{userId},#{groupId})")
    int insertUserToGroup(@Param("groupId") long groupId, @Param("userId") long userId);

    @Update("update group_user set is_delete=1-is_delete where user_id=#{userId} and group_id=#{groupId}")
    void updateUserToGroup(@Param("userId") long userId, @Param("groupId") long groupId);

    void deleteGroupById(@Param("groupId") long groupId);

    void deleteGroupUserById(@Param("groupId") long groupId);

    void deleteGroupMessageById(@Param("groupId") long groupId);

    void deleteGroupMessageExtraById(@Param("groupId") long groupId);

    @Update("UPDATE group_user SET is_delete = 1 WHERE user_id = #{userId} AND group_id = #{groupId}")
    int removeUserFromGroup(@Param("userId") long userId, @Param("groupId") long groupId);
}
