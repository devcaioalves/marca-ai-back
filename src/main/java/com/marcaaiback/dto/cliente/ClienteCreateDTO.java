package com.marcaaiback.dto.cliente;

import com.marcaaiback.model.entity.Agendamento;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClienteCreateDTO {

    @NotBlank(message = "O nome do cliente é obrigatório.")
    private String nome;
    @NotBlank(message = "O telefone do cliente é obrigatório.")
    private String telefone;
    @NotBlank(message = "O cliente teve ter agendamentos.")
    private List<Agendamento> agendamentos;
}
