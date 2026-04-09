package com.marcaaiback.model.dto.mensagem;

import com.marcaaiback.model.enuns.TipoDeMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensagemRequest {

    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    private String conteudo;

    @NotNull(message = "O tipo da mensagem é obrigatório.")
    private TipoDeMensagem tipo;

    @NotNull(message = "O cliente é obrigatório.")
    private Long clienteId;

    @NotNull(message = "O agendamento é obrigatório.")
    private Long agendamentoId;
}