package com.marcaaiback.model.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {

    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private String mensagemErro;
    private String token;
}