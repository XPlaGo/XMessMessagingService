package com.xplago.xmessmessagingservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class NewGroupResponse {

    public NewGroupResponse(String chatId) {
        this.chatId = chatId;
    }

    @NotNull
    private String chatId;
}
