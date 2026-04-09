package com.marcaaiback.model.dto.mensagem;

import com.marcaaiback.model.enuns.TipoDeMensagem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensagemResponse {

    private Long id;
    private String conteudo;
    private LocalDateTime dataHora;
    private TipoDeMensagem tipo;
    private Long clienteId;
    private String clienteNome;
    private Long agendamentoId;
}