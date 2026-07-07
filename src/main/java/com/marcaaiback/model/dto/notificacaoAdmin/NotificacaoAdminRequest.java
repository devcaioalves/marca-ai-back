package com.marcaaiback.model.dto.notificacaoAdmin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoAdminRequest {

    private String telefoneAdmin;

    private String nomeCliente;

    private String telefoneCliente;
}
