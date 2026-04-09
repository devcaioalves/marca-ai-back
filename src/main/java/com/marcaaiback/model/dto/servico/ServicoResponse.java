package com.marcaaiback.model.dto.servico;

import com.marcaaiback.model.dto.agendamento.AgendamentoResumoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServicoResponse {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private Integer duracao; // em minutos
    private boolean ativo;
    private List<AgendamentoResumoResponse> agendamentos;
}
