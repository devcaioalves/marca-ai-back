package com.marcaaiback.model.dto.admin.senha;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsqueciSenhaRequest {

    @NotBlank(message = "O e-mail é obrigatório.")
    private String email;
}

