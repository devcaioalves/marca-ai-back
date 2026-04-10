package com.marcaaiback.model.dto.admin.senha;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlterarSenhaRequest {

    @NotBlank(message = "A senha atual é obrigatória.")
    private String senhaAtual;

    @NotBlank(message = "A nova senha é obrigatória.")
    private String novaSenha;

    @NotBlank(message = "A confirmação da nova senha é obrigatória.")
    private String confirmarNovaSenha;
}
