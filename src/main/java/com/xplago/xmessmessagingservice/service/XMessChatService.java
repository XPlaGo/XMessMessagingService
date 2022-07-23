package com.xplago.xmessmessagingservice.service;

import com.xplago.xmessmessagingservice.dto.chat.ChatResponse;
import com.xplago.xmessmessagingservice.dto.chat.MemberInfo;
import com.xplago.xmessmessagingservice.dto.chat.MemberStatus;
import com.xplago.xmessmessagingservice.dto.chat.MessagingStatus;
import com.xplago.xmessmessagingservice.dto.document.DocumentResponse;
import com.xplago.xmessmessagingservice.dto.document.File;
import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.chat.XMessChatType;
import com.xplago.xmessmessagingservice.model.message.XMessMessage;
import com.xplago.xmessmessagingservice.model.message.XMessMessageStatus;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.model.user.XMessUserStatus;
import com.xplago.xmessmessagingservice.repository.XMessChatRepository;
import com.xplago.xmessmessagingservice.repository.XMessMessageRepository;
import com.xplago.xmessmessagingservice.repository.XMessUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class XMessChatService {
    @Autowired
    private XMessChatRepository chatRepository;

    @Autowired
    private XMessMessageRepository messageRepository;

    @Autowired
    private XMessUserRepository userRepository;

    @Autowired
    private XMessUserService userService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private DocumentStorageService storageService;

    @Transactional
    public List<ChatResponse> getChatsOfUser(String username) {

        XMessUser user = userService.requestUserFromUsername(username);

        List<XMessChat> chats = user.getChats();

        return chats.stream().map(chat -> convertToChatResponse(chat, username)).collect(Collectors.toList());
    }

    public XMessChat newDialogue(String creatorUsername, String interlocutorUsername) {

        XMessChat chat = new XMessChat();

        ArrayList<XMessUser> members = new ArrayList<>(2);
        XMessUser creator = userService.requestUserFromUsername(creatorUsername);
        XMessUser interlocutor = userService.requestUserFromUsername(interlocutorUsername);
        members.add(creator);
        members.add(interlocutor);

        chat.setType(XMessChatType.DIALOGUE);
        chat.setUsers(members);
        chat.setCreationDate(new Date());

        for (XMessChat oldChat : creator.getChats())
            if (oldChat != null && oldChat.getType() == XMessChatType.DIALOGUE && oldChat.getUsers().contains(interlocutor))
                throw new CustomException("Dialogue already exist", HttpStatus.BAD_REQUEST);

        chat =  chatRepository.save(chat);

        if (creator.getChats() == null) creator.setChats(new ArrayList<>());
        if (interlocutor.getChats() == null) interlocutor.setChats(new ArrayList<>());
        creator.getChats().add(chat);
        interlocutor.getChats().add(chat);

        userRepository.save(creator);
        userRepository.save(interlocutor);

        return chat;
    }

    public XMessChat newGroup(String creatorUsername, List<String> membersUsernames, String title) {

        XMessChat chat = new XMessChat();

        List<XMessUser> members = membersUsernames.stream().map(userService::requestUserFromUsername).collect(Collectors.toList());
        members.add(userService.requestUserFromUsername(creatorUsername));

        chat.setUsers(members);
        chat.setTitle(title);
        chat.setType(XMessChatType.GROUP);
        chat.setCreationDate(new Date());

        chat = chatRepository.save(chat);

        XMessChat finalChat = chat;
        members.forEach(user -> {
            if (user.getChats() == null) user.setChats(new ArrayList<>());
            user.getChats().add(finalChat);
            userRepository.save(user);
        });

        return chat;
    }

    public ChatResponse convertToChatResponse(XMessChat chat, String username) {
        ChatResponse response = new ChatResponse();

        response.setId(chat.getId());
        response.setChatType(chat.getType());
        response.setMembers(chat.getUsers().stream().map(this::toMemberInfo).toList());
        response.setDocuments(chat.getDocuments().stream().map(doc -> convertToDocumentResponse(doc, chat.getId())).toList());
        response.setAvatarId(chat.getAvatarId());

        if (chat.getType() == XMessChatType.GROUP) {
            response.setTitle(chat.getTitle());
        } else if (chat.getType() == XMessChatType.DIALOGUE) {
            response.setTitle(null);
        }

        Optional<XMessMessage> messageOptional = messageRepository.findFirstByChatOrderByTimeDesc(chat);
        if (messageOptional.isPresent()) {
            XMessMessage message = messageOptional.get();
            XMessUser sender = message.getSender();
            if (sender != null && Objects.equals(sender.getUsername(), username)) {
                response.setLastMessage("You: " + message.getTextContent());
                response.setHasUnreadMessage(false);
            } else if (sender != null && chat.getType() == XMessChatType.GROUP) {
                response.setLastMessage(sender.getUsername() + ": " + message.getTextContent());
                response.setHasUnreadMessage(message.getStatus().get(username) == XMessMessageStatus.SENT);
            } else {
                response.setLastMessage(message.getTextContent());
                response.setHasUnreadMessage(message.getStatus().get(username) == XMessMessageStatus.SENT);
            }
            response.setLastMessageTime(message.getTime());
        } else {
            response.setLastMessage(null);
            response.setLastMessageTime(chat.getCreationDate());
            response.setHasUnreadMessage(false);
        }
        return response;
    }

    public XMessChat requestChatById(String id) {
        Optional<XMessChat> chat = chatRepository.findById(id);
        if (chat.isPresent()) {
            return chat.get();
        } else {
            throw new CustomException("Chat with id: " + id + " not found", HttpStatus.BAD_REQUEST);
        }
    }

    public XMessChat requestChatOfUserById(String id, XMessUser user) {
        Optional<XMessChat> chat = user.getChats().stream().filter(item -> Objects.equals(item.getId(), id)).findFirst();
        if (chat.isPresent()) {
            return chat.get();
        } else {
            throw new CustomException("The chat does not exist or you do not have access to this chat", HttpStatus.FORBIDDEN);
        }
    }

    public MemberInfo toMemberInfo(XMessUser user) {
        return MemberInfo.builder()
                .username(user.getUsername())
                .status(user.getStatus())
                .messagingStatus(MessagingStatus.TYPING)
                .avatarId(user.getAvatarId())
                .build();
    }

    public String clearChat(String chatId, String username) {
        XMessUser user = userService.requestUserFromUsername(username);
        for (XMessChat chat : user.getChats()) {
            System.out.println(chat.getId() + " " + chatId);
            if (Objects.equals(chat.getId(), chatId)) {
                messageRepository.deleteAll(chat.getMessages());
                messageRepository.deleteAllByChat(chat);
                chat.setMessages(new ArrayList<>());
                chatRepository.save(chat);
                return chat.getId();
            }
        }
        throw new CustomException("The chat does not exist or you do not have access to this chat", HttpStatus.FORBIDDEN);
    }

    public String deleteChat(String chatId, String username) {
        XMessUser user = userService.requestUserFromUsername(username);
        for (XMessChat chat : user.getChats()) {
            if (chat.getId().equals(chatId)) {
                chat.setDocuments(new ArrayList<>());
                chat.setMessages(new ArrayList<>());
                chatRepository.delete(chat);
                return chat.getId();
            }
        }
        throw new CustomException("The chat does not exist or you do not have access to this chat", HttpStatus.FORBIDDEN);
    }

    public void updateUserStatus(String username, XMessUserStatus status) {
        XMessUser user = userService.requestUserFromUsername(username);
        user.setStatus(status);
        List<XMessChat> userChats = user.getChats();
        for (XMessChat chat : userChats) {
            List<XMessUser> members = chat.getUsers();
            for (XMessUser member : members) {
                if (member.getStatus() == XMessUserStatus.ONLINE)
                    messagingTemplate.convertAndSendToUser(member.getUsername(), "/update/chats", convertToChatResponse(chat, username));
            }
        }
        userRepository.save(user);
    }

    private DocumentResponse convertToDocumentResponse(String id, String chatId) {
        File file = storageService.getFile(id, chatId);
        return DocumentResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .size(file.getSize())
                .chatId(chatId)
                .build();
    }
}
