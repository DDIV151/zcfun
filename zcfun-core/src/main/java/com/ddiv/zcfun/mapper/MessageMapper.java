package com.ddiv.zcfun.mapper;

import com.ddiv.zcfun.domain.po.message.MessagePO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    void save(MessagePO messagePO);

    void update(MessagePO messagePO);

    void delete(long msgId);

    MessagePO findById(long msgId);

    List<MessagePO> findAll();
}