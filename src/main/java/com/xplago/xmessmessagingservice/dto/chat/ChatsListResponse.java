package com.xplago.xmessmessagingservice.dto.chat;

import lombok.Data;

import java.util.List;

@Data
public class ChatsListResponse {

    public ChatsListResponse(List<ChatResponse> chatDataList) {
        this.chatDataList = chatDataList;
    }

    private List<ChatResponse> chatDataList;

    private final ChatResponseType type = ChatResponseType.COLLECTION;
}
