package com.marcaaiback.service;

import com.marcaaiback.client.ViaCEPClient;
import com.marcaaiback.exception.CEPNaoEncontradoException;
import com.marcaaiback.model.dto.viacep.ViaCEPResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViaCEPService {

    private final ViaCEPClient viaCEPClient;

    public ViaCEPResponse buscarEnderecoPeloCEP(String cep) {
        String cepFormated = cep.replaceAll("\\D", "");

        ViaCEPResponse address =  viaCEPClient.getViaCEP(cepFormated);
        if (address == null || address.getCep() == null) {
            throw new CEPNaoEncontradoException("CEP não encontrado no ViaCEP base de dados.");
        }
        return address;
    }
}
