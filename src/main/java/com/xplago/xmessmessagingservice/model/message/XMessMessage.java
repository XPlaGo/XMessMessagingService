package com.xplago.xmessmessagingservice.model.message;

import com.xplago.xmessmessagingservice.model.user.XMessUser;
import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Document
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XMessMessage {

    @Id
    private String id;

    @DBRef(lazy = true)
    private XMessChat chat;

    private Date time;

    private String textContent;

    private List<String> documents = new ArrayList<>();

    @DBRef(lazy = true)
    private XMessUser sender;

    private Map<String, XMessMessageStatus> status;

    private XMessMessageType type;
}
