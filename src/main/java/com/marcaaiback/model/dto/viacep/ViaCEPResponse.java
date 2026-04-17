package com.marcaaiback.model.dto.viacep;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ViaCEPResponse {

    private String cep;
    private String logradouro;
    private String bairro;
    private String localidade;
    private String uf;
}
