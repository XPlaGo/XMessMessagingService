package com.xplago.xmessmessagingservice.dto.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequest {
    @JsonProperty
    private MultipartFile file;
    @JsonProperty
    private String chatId;
}
