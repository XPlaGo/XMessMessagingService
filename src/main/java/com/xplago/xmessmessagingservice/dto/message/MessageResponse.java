package com.xplago.xmessmessagingservice.dto.message;

import com.xplago.xmessmessagingservice.dto.document.DocumentResponse;
import com.xplago.xmessmessagingservice.model.message.XMessMessageStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String chatId;
    private Date time;
    private String textContent;
    private List<DocumentResponse> documents;
    private String sender;
    private Map<String, XMessMessageStatus> status;
    private MessageResponseType type;
}
