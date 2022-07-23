package com.xplago.xmessmessagingservice.config;

import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.model.user.XMessUserStatus;
import com.xplago.xmessmessagingservice.repository.XMessUserRepository;
import com.xplago.xmessmessagingservice.security.JwtTokenProvider;
import com.xplago.xmessmessagingservice.service.XMessChatService;
import com.xplago.xmessmessagingservice.service.XMessUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PresenceChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String jwtToken = jwtTokenProvider.resolveToken(accessor.getNativeHeader("Authorization").get(0));
            if (jwtTokenProvider.validateToken(jwtToken)) {
                Authentication user = jwtTokenProvider.getAuthentication(jwtToken);
                accessor.setUser(user);
            }
        }
        return message;
    }
}
