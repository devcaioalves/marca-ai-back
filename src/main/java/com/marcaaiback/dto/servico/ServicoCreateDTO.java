package com.marcaaiback.dto.servico;

import com.marcaaiback.model.entity.Agendamento;
import jakarta.validation.constraints.NotBlank;
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
public class ServicoCreateDTO {

    @NotBlank(message = "O serviço deve ter um nome.")
    private String nome;
    @NotBlank(message = "O serviço deve ter uma descrição.")
    private String descricao;
    @NotBlank(message = "O serviço deve ter um valor.")
    private BigDecimal valor;
    @NotBlank(message = "O serviço deve ter uma duração.")
    private Double duracao;
    @NotBlank(message = "O serviço deve ter seus agendamentos.")
    private List<Agendamento> agendamentos;
    @NotBlank(message = "O serviço deve mostrar se está ativo ou não.")
    private boolean ativo;

}
