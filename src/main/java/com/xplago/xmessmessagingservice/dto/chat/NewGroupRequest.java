package com.xplago.xmessmessagingservice.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewGroupRequest {
    private List<String> membersUsernames;
    private String title;
    private String description;
}
