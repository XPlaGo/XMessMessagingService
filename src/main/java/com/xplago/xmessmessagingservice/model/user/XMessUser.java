package com.xplago.xmessmessagingservice.model.user;

import com.xplago.xmessmessagingservice.model.chat.XMessChat;
import com.xplago.xmessmessagingservice.model.document.Document;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.*;

@org.springframework.data.mongodb.core.mapping.Document("xMessUser")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XMessUser {

    @Id
    private String id;

    private String username;

    private String email;

    private String password;

    private List<XMessUserRole> userRoles;

    private Boolean accountExpired;

    private Boolean accountLocked;

    private Boolean credentialsExpired;

    private Boolean disabled;

    private XMessUserStatus status;

    @DBRef(lazy = true)
    private List<XMessChat> chats = new ArrayList<>();

    private String avatarId;

    private String description = "";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XMessUser xMessUser = (XMessUser) o;
        return id.equals(xMessUser.id) && username.equals(xMessUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
