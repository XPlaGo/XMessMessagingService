package com.xplago.xmessmessagingservice.service;

import com.xplago.xmessmessagingservice.exception.CustomException;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.model.user.XMessUserStatus;
import com.xplago.xmessmessagingservice.repository.XMessUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class XMessUserService {

    @Autowired
    private XMessUserRepository userRepository;

    public XMessUser requestUserFromUsername(String username) {
        Optional<XMessUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new CustomException("User with username: " + username + " not found", HttpStatus.BAD_REQUEST);
        }
    }

    public List<XMessUser> searchUserByUsernamePart(String part, String username) {
        return userRepository.findXMessUsersByUsernameContainsIgnoreCase(part);
    }
}
