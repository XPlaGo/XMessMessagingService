package com.xplago.xmessmessagingservice.dto.chat;

import com.xplago.xmessmessagingservice.model.user.XMessUserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberInfo {
    private String username;
    private XMessUserStatus status;
    private MessagingStatus messagingStatus;
    private String avatarId;
}
