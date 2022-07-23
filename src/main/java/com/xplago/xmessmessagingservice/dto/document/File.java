package com.xplago.xmessmessagingservice.dto.document;

import com.xplago.xmessmessagingservice.model.document.DocumentAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class File {
    private String id;
    private String name;
    private Long size;
    private String chatId;
    private DocumentAccess access;
}
