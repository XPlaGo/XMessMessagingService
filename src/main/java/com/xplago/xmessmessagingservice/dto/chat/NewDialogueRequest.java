package com.xplago.xmessmessagingservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewDialogueRequest {
    @NotNull
    private String interlocutorUsername;
}
