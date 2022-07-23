package com.xplago.xmessmessagingservice.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MessageRequest {
    @JsonProperty
    private String chatId;
    @JsonProperty
    private Date time;
    @JsonProperty
    private String textContent;
    @JsonProperty
    private List<String> documents;
}
