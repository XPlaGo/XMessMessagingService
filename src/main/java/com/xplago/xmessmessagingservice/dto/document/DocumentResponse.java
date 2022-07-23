package com.xplago.xmessmessagingservice.dto.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponse {
    private String id;
    private String name;
    private Long size;
    private String chatId;
}
