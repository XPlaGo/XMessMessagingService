package com.xplago.xmessmessagingservice.repository;

import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.message.XMessMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface XMessMessageRepository extends MongoRepository<XMessMessage, String> {
    Optional<XMessMessage> findFirstByChatOrderByTimeDesc(XMessChat chat);
    List<XMessMessage> findXMessMessageByChatOrderByTimeDesc(XMessChat chat, Pageable pageable);
    void deleteAllByChat(XMessChat chat);
}
