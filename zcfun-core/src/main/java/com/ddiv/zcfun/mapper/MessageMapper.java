package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.im.message.MessagePO;
import com.ddiv.zcfun.domain.po.im.message.OfflineMessagePO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper {
    void save(MessagePO messagePO);

    void update(MessagePO messagePO);

    void delete(long msgId);

    MessagePO findById(long msgId);

    List<MessagePO> findAll();

    @Insert("INSERT INTO offline_message (recipient_id, content) values(#{recipientId},#{content})")
    int saveOfflineMessage(OfflineMessagePO offlineMessagePO);

    @Select("SELECT id,recipient_id,content from offline_message where is_delete=0 AND recipient_id=#{l}")
    List<OfflineMessagePO> selectOfflineMessage(long l);

    @Update("UPDATE offline_message SET is_delete=1 WHERE id=#{id}")
    void markAsDelivered(long id);
}