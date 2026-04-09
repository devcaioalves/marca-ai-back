package com.marcaaiback.dto.cliente;

import com.marcaaiback.model.entity.Agendamento;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String telefone;
    private List<Agendamento> agendamentos;
}
