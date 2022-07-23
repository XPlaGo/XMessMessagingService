package com.xplago.xmessmessagingservice.model.user;

import org.springframework.security.core.GrantedAuthority;

public enum XMessUserRole implements GrantedAuthority {
    ROLE_CLIENT, ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}
