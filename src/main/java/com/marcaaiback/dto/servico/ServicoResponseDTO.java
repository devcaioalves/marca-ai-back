package com.marcaaiback.dto.servico;

import com.marcaaiback.model.entity.Agendamento;
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
public class ServicoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private Double duracao;
    private List<Agendamento> agendamentos;
    private boolean ativo;

}
