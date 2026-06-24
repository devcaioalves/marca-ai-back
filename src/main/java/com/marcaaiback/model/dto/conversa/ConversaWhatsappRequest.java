package com.marcaaiback.model.dto.conversa;

import com.marcaaiback.model.enuns.EstadoConversa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConversaWhatsappRequest {

    @NotNull(message = "O estado da conversa é obrigatório.")
    private EstadoConversa estadoConversa;

    private Long servicoId;

    private Long horarioId;

    private LocalTime horaInicioEscolhida;

    private LocalTime horaFimEscolhida;

    private Long agendamentoId;

    private LocalDate dataEscolhida;

    @NotBlank(message = "O telefone do cliente é obrigatório.")
    private String telefoneCliente;
}