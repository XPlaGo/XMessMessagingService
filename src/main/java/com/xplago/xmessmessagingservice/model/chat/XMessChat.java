package com.xplago.xmessmessagingservice.model.chat;

import com.xplago.xmessmessagingservice.model.message.XMessMessage;
import com.xplago.xmessmessagingservice.model.user.XMessUser;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document
@AllArgsConstructor
@NoArgsConstructor
public class XMessChat {

    @Id
    private String id;

    @DBRef(lazy = true)
    private List<XMessUser> users = new ArrayList<>();

    private XMessChatType type;

    private String title;

    private String description;

    @DBRef
    private List<XMessMessage> messages = new ArrayList<>();

    private Date creationDate;

    private List<String> documents = new ArrayList<>();

    private String avatarId = "";
}
