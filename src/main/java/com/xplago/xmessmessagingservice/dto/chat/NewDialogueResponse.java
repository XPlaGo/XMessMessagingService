package com.xplago.xmessmessagingservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewDialogueResponse {

    public NewDialogueResponse(String chatId) {
        this.chatId = chatId;
    }

    private String chatId;
}
