package com.marcaaiback.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collections;

public class JwtUserDetails extends User {

    @Serial
    private static final long serialVersionUID = 1L;
    private final com.marcaaiback.model.entity.Admin admin;

    public JwtUserDetails(com.marcaaiback.model.entity.Admin admin) {
        super(admin.getEmail(), "", Collections.emptyList());
        this.admin = admin;
    }

    public String getId() {
        return String.valueOf(this.admin.getId());
    }



}
