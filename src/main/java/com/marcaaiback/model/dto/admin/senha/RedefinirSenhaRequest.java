package com.marcaaiback.model.dto.admin.senha;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedefinirSenhaRequest {

    @NotBlank(message = "O token é obrigatório.")
    private String token;

    @NotBlank(message = "A senha é obrigatória.")
    private String novaSenha;
}
