package com.marcaaiback.model.dto.servico;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServicoRequest {

    @NotBlank(message = "O serviço deve ter um nome.")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres.")
    private String nome;

    @NotBlank(message = "O serviço deve ter uma descrição.")
    private String descricao;

    @NotNull(message = "O serviço deve ter um valor.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "O serviço deve ter uma duração.")
    @Min(value = 1, message = "A duração deve ser de no mínimo 1 minuto.")
    private Integer duracao; // em minutos
}
