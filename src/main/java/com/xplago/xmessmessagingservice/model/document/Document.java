package com.xplago.xmessmessagingservice.model.document;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.io.InputStream;


@org.springframework.data.mongodb.core.mapping.Document
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Document {

    @Id
    private String id;

    private String name;

    private String type;

    private Long size;

    private InputStream data;

    private DocumentAccess access = DocumentAccess.CHAT;

    private String chatId;

    public Document(String name, String type, Long size, InputStream data) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.data = data;
    }
}
