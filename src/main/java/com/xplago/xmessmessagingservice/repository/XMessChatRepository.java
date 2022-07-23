package com.xplago.xmessmessagingservice.repository;

import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface XMessChatRepository extends MongoRepository<XMessChat, String> {
    List<XMessChat> findAllByUsersContains(XMessUser user);
    Optional<XMessChat> findByIdAndUsersContaining(Long id, XMessUser user);
}
