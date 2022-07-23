package com.xplago.xmessmessagingservice.dto.chat;

import com.xplago.xmessmessagingservice.dto.document.DocumentResponse;
import com.xplago.xmessmessagingservice.model.chat.XMessChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponse {
    private String id;
    private String title;
    private Boolean hasUnreadMessage;
    private String lastMessage;
    private Date lastMessageTime;
    private XMessChatType chatType;
    private List<MemberInfo> members;
    private String avatarId;
    private List<DocumentResponse> documents;
}
