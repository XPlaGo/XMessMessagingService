package com.xplago.xmessmessagingservice.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadMessagesRequest {
    @JsonProperty
    private Integer page;
    @JsonProperty
    private String chatId;
}
