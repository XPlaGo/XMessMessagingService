package com.xplago.xmessmessagingservice.config;

import com.xplago.xmessmessagingservice.model.user.XMessUserStatus;
import com.xplago.xmessmessagingservice.service.XMessChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class StompDisconnectConfig implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private XMessChatService chatService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = sha.getUser();
        if (user != null) {
            String username = sha.getUser().getName();
            chatService.updateUserStatus(username, XMessUserStatus.OFFLINE);
        }
    }
}
