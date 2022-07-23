package com.xplago.xmessmessagingservice.service;

import com.xplago.xmessmessagingservice.dto.document.DocumentResponse;
import com.xplago.xmessmessagingservice.dto.document.File;
import com.xplago.xmessmessagingservice.dto.message.MessageRequest;
import com.xplago.xmessmessagingservice.dto.message.MessageResponse;
import com.xplago.xmessmessagingservice.dto.message.MessageResponseType;
import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.message.XMessMessage;
import com.xplago.xmessmessagingservice.model.message.XMessMessageStatus;
import com.xplago.xmessmessagingservice.model.message.XMessMessageType;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.repository.XMessChatRepository;
import com.xplago.xmessmessagingservice.repository.XMessMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class XMessMessageService {

    @Autowired
    private XMessMessageRepository messageRepository;

    @Autowired
    private XMessChatRepository chatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private XMessChatService chatService;

    @Autowired
    private XMessUserService userService;

    @Autowired
    private DocumentStorageService documentStorageService;

    public void readMessage(String username, String messageId) {
        XMessUser user = userService.requestUserFromUsername(username);
        XMessMessage message = getMessageById(messageId);
        XMessChat chat = message.getChat();
        message.getStatus().put(username, XMessMessageStatus.RECEIVED);
        messageRepository.save(message);
        for (XMessUser member : chat.getUsers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/update/messages/" + chat.getId(),
                    convertToMessageResponse(message, chat.getId(), MessageResponseType.UPDATE_SINGLE)
            );
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/update/chats",
                    chatService.convertToChatResponse(chat, member.getUsername())
            );
        }
    }

    public void createAndSendUserMessage(MessageRequest request, String username, String destination) {

        XMessUser user = userService.requestUserFromUsername(username);
        XMessChat chat = chatService.requestChatOfUserById(request.getChatId(), user);

        HashMap<String, XMessMessageStatus> status = new HashMap<>();
        for (XMessUser member : chat.getUsers())
            status.put(
                    member.getUsername(),
                    Objects.equals(member.getUsername(), username) ? XMessMessageStatus.RECEIVED : XMessMessageStatus.SENT
            );

        XMessMessage message = XMessMessage.builder()
                .id(null)
                .chat(chat)
                .documents(request.getDocuments())
                .sender(user)
                .status(status)
                .textContent(request.getTextContent())
                .time(request.getTime())
                .type(XMessMessageType.USER_MESSAGE)
                .build();

        message = messageRepository.save(message);

        chat.getMessages().add(message);
        chatRepository.save(chat);

        for (XMessUser member : chat.getUsers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    destination,
                    convertToMessageResponse(message, chat.getId(), MessageResponseType.NEW_SINGLE)
            );
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/update/chats",
                    chatService.convertToChatResponse(chat, member.getUsername())
            );
        }
    }

    public void createAndSendInfoMessage(String chatId, String textContent, String destination) {

        XMessChat chat = chatService.requestChatById(chatId);

        HashMap<String, XMessMessageStatus> status = new HashMap<>();
        for (XMessUser member : chat.getUsers()) status.put(member.getUsername(), XMessMessageStatus.SENT);

        XMessMessage message = XMessMessage.builder()
                .id(null)
                .chat(chat)
                .documents(new ArrayList<>())
                .sender(null)
                .status(status)
                .textContent(textContent)
                .time(new Date())
                .type(XMessMessageType.INFO_MESSAGE)
                .build();

        message = messageRepository.save(message);

        for (XMessUser member : chat.getUsers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    destination,
                    convertToMessageResponse(message, chat.getId(), MessageResponseType.NEW_SINGLE)
            );
            messagingTemplate.convertAndSendToUser(
                    member.getUsername(),
                    "/update/chats",
                    chatService.convertToChatResponse(chat, member.getUsername())
            );
        }
    }

    @Transactional
    public List<MessageResponse> getMessagesOfChat(String chatId, String username, Pageable pageable) {
        Optional<XMessChat> chat = userService.requestUserFromUsername(username)
                .getChats().stream().filter(item -> Objects.equals(item.getId(), chatId)).findFirst();
        if (chat.isPresent()) {
            return messageRepository.findXMessMessageByChatOrderByTimeDesc(chat.get(), pageable)
                    .stream()
                    .map(message -> convertToMessageResponse(message, chatId, MessageResponseType.DEFAULT_SINGLE))
                    .toList();
        } else {
            throw new CustomException(
                    "The chat does not exist or you do not have access to this chat",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    private MessageResponse convertToMessageResponse(XMessMessage message, String chatId, MessageResponseType type) {
        return MessageResponse.builder()
                .id(message.getId())
                .chatId(chatId)
                .type(type)
                .documents(
                        message.getDocuments()
                                .stream()
                                .map(id -> convertToDocumentResponse(id, message.getChat().getId()))
                                .toList()
                )
                .sender(message.getSender() != null ? message.getSender().getUsername() : null)
                .status(message.getStatus())
                .textContent(message.getTextContent())
                .time(message.getTime())
                .build();
    }

    private DocumentResponse convertToDocumentResponse(String id, String chatId) {
        File file = documentStorageService.getFile(id, chatId);
        return DocumentResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .chatId(chatId)
                .build();
    }

    public XMessMessage getMessageById(String messageId) {
        Optional<XMessMessage> message = messageRepository.findById(messageId);
        if (message.isPresent()) return message.get();
        else throw new CustomException("Message is not found", HttpStatus.NOT_FOUND);
    }
}
