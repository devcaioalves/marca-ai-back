package com.marcaaiback.jwt;

import com.marcaaiback.model.entity.Admin;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collections;

public class JwtUserDetails extends User {

    @Serial
    private static final long serialVersionUID = 1L;
    private final com.marcaaiback.model.entity.Admin admin;

    public JwtUserDetails(Admin admin) {
        super(admin.getEmail(), admin.getSenha(), admin.getAuthorities()
        );
        this.admin = admin;
    }

    public String getId() {
        return String.valueOf(this.admin.getId());
    }


    public Admin getAdmin() {
        return this.admin;
    }
}
