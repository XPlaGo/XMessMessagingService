package com.xplago.xmessmessagingservice.controller;

import com.xplago.xmessmessagingservice.dto.chat.*;
import com.xplago.xmessmessagingservice.dto.profile.ProfileResponse;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.repository.XMessUserRepository;
import com.xplago.xmessmessagingservice.service.XMessChatService;
import com.xplago.xmessmessagingservice.service.XMessMessageService;
import com.xplago.xmessmessagingservice.service.XMessUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/chats")
@CrossOrigin(origins = {"http://localhost:3000", "https://xmess.jelastic.regruhosting.ru"})
public class ChatsController {

    @Autowired
    private XMessChatService chatService;

    @Autowired
    private XMessMessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private XMessUserService userService;

    @Autowired
    private XMessUserRepository userRepository;

    @PostMapping("/newDialogue")
    public @ResponseBody NewDialogueResponse newDialogue(@RequestBody NewDialogueRequest request) {

        String creatorUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        XMessChat chat = chatService.newDialogue(creatorUsername, request.getInterlocutorUsername());

        messageService.createAndSendInfoMessage(chat.getId(), creatorUsername + " created a chat", "/update/messages/" + chat.getId());

        ChatResponse chatResponseCreator = ChatResponse.builder()
                .id(chat.getId())
                .title(request.getInterlocutorUsername())
                .hasUnreadMessage(true)
                .lastMessage(null)
                .lastMessageTime(null).build();
        messagingTemplate.convertAndSendToUser(creatorUsername, "/update/chats", chatResponseCreator);

        ChatResponse chatResponseInterlocutor = ChatResponse.builder()
                .id(chat.getId())
                .title(creatorUsername)
                .hasUnreadMessage(true)
                .lastMessage(null)
                .lastMessageTime(null).build();
        messagingTemplate.convertAndSendToUser(request.getInterlocutorUsername(), "/update/chats", chatResponseInterlocutor);

        return new NewDialogueResponse(chat.getId());
    }

    @PostMapping("/newGroup")
    public @ResponseBody NewGroupResponse newDialogue(@RequestBody NewGroupRequest request) {

        String senderUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        XMessChat chat = chatService.newGroup(senderUsername, request.getMembersUsernames(), request.getTitle());

        messageService.createAndSendInfoMessage(chat.getId(), senderUsername + " created a chat", "/update/messages/" + chat.getId());

        for (XMessUser user : chat.getUsers()) {
            ChatResponse chatResponse = ChatResponse.builder()
                    .id(chat.getId())
                    .title(chat.getTitle())
                    .hasUnreadMessage(true)
                    .lastMessage(null)
                    .lastMessageTime(null).build();
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/update/chats", chatResponse);
        }
        return new NewGroupResponse(chat.getId());
    }

    @PostMapping("/searchUsersGlobal")
    public @ResponseBody List<MemberInfo> searchUsersGlobal(@RequestBody SearchUsersGlobalRequest request) {
        return userService.searchUserByUsernamePart(
                        request.getPart(),
                        SecurityContextHolder.getContext().getAuthentication().getName()
                ).stream()
                .map(this::convertUserToMemberInfo)
                .toList();
    }

    @PostMapping("/clearChat")
    public @ResponseBody String clearChat(@RequestBody ClearChatRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String id = chatService.clearChat(request.getChatId(), username);
        messageService.createAndSendInfoMessage(id, username + " cleaned the chat", "/update/messages/" + id);
        return id;
    }

    @PostMapping("/deleteChat")
    public @ResponseBody String deleteChat(@RequestBody DeleteChatRequest request) {
        return chatService.deleteChat(request.getChatId(), SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @SubscribeMapping("/update/chats")
    public void getChats(Principal user) {
        String username = user.getName();
        messagingTemplate.convertAndSendToUser(username, "/update/chats", chatService.getChatsOfUser(username));
    }

    @GetMapping("/getProfile")
    public @ResponseBody ProfileResponse getProfile() {
        XMessUser user = userService.requestUserFromUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        return ProfileResponse.builder()
                .username(user.getUsername())
                .description(user.getDescription())
                .avatarId(user.getAvatarId())
                .build();
    }

    @PostMapping("/setProfileDescription")
    public String setProfileDescription(@RequestBody String description) {
        XMessUser user = userService.requestUserFromUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setDescription(description);
        userRepository.save(user);
        return user.getUsername();
    }

    public MemberInfo convertUserToMemberInfo(XMessUser user) {
        return MemberInfo.builder()
                .username(user.getUsername())
                .avatarId(user.getAvatarId())
                .messagingStatus(MessagingStatus.DEFAULT)
                .status(user.getStatus())
                .build();
    }
}