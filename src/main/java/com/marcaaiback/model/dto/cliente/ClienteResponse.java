package com.marcaaiback.model.dto.cliente;

import com.marcaaiback.model.dto.agendamento.AgendamentoResumoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClienteResponse {

    private Long id;
    private String nome;
    private String telefone;
    private List<AgendamentoResumoResponse> agendamentos;
}