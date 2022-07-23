package com.xplago.xmessmessagingservice.controller;

import com.xplago.xmessmessagingservice.dto.message.LoadMessagesRequest;
import com.xplago.xmessmessagingservice.dto.message.MessageRequest;
import com.xplago.xmessmessagingservice.dto.message.MessagesListResponse;
import com.xplago.xmessmessagingservice.dto.message.ReadMessage;
import com.xplago.xmessmessagingservice.service.XMessMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@CrossOrigin(origins = {"http://localhost:3000", "https://xmess.jelastic.regruhosting.ru"})
@RequestMapping("/messages")
public class MessagesController {

    @Autowired
    private XMessMessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @SubscribeMapping("/update/messages/{chatId}")
    public void getMessages(@DestinationVariable String chatId, Principal user) {
        messagingTemplate.convertAndSendToUser(
                user.getName(),
                "/update/messages/" + chatId,
                MessagesListResponse.builder()
                        .messages(messageService.getMessagesOfChat(chatId, user.getName(), PageRequest.of(0, 20)))
                        .page(0)
                        .build()
        );
    }

    @MessageMapping("/readMessage")
    public void readMessage(Principal user, ReadMessage req) {
        messageService.readMessage(user.getName(), req.getId());
    }

    @MessageMapping("/loadMessages")
    public void loadMessages(Principal user, LoadMessagesRequest request) {
        messagingTemplate.convertAndSendToUser(
                user.getName(),
                "/update/messages/" + request.getChatId(),
                MessagesListResponse.builder()
                        .messages(messageService.getMessagesOfChat(request.getChatId(), user.getName(), PageRequest.of(request.getPage(), 20)))
                        .page(request.getPage())
                        .build()
        );
    }

    @MessageMapping("/send")
    public void sendMessage(Principal user, MessageRequest request) {
        messageService.createAndSendUserMessage(request, user.getName(), "/update/messages/" + request.getChatId());
    }
}