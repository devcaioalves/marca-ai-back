package com.marcaaiback.service;

import com.marcaaiback.client.ViaCEPClient;
import com.marcaaiback.exception.CEPNaoEncontradoException;
import com.marcaaiback.model.dto.viacep.ViaCEPResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViaCEPServiceTest {

    @Mock
    ViaCEPClient viaCEPClient;

    @InjectMocks
    ViaCEPService viaCEPService;

    @Test
    @DisplayName("buscarEnderecoPeloCEP: retorna endereço válido")
    void buscar_sucesso() {
        ViaCEPResponse response = new ViaCEPResponse();
        response.setCep("50000-000");
        response.setLogradouro("Rua A");
        response.setBairro("Centro");
        response.setLocalidade("Recife");
        response.setUf("PE");

        when(viaCEPClient.getViaCEP("50000000")).thenReturn(response);

        ViaCEPResponse result = viaCEPService.buscarEnderecoPeloCEP("50000000");

        assertThat(result.getCep()).isEqualTo("50000-000");
        assertThat(result.getLocalidade()).isEqualTo("Recife");
    }

    @Test
    @DisplayName("buscarEnderecoPeloCEP: remove formatação do CEP antes de consultar")
    void buscar_removeFormatacao() {
        ViaCEPResponse response = new ViaCEPResponse();
        response.setCep("50000-000");

        when(viaCEPClient.getViaCEP("50000000")).thenReturn(response);

        ViaCEPResponse result = viaCEPService.buscarEnderecoPeloCEP("50.000-000");

        assertThat(result).isNotNull();
        verify(viaCEPClient).getViaCEP("50000000");
    }

    @Test
    @DisplayName("buscarEnderecoPeloCEP: lança CEPNaoEncontradoException quando retorno nulo")
    void buscar_retornoNulo() {
        when(viaCEPClient.getViaCEP("00000000")).thenReturn(null);

        assertThatThrownBy(() -> viaCEPService.buscarEnderecoPeloCEP("00000000"))
                .isInstanceOf(CEPNaoEncontradoException.class);
    }

    @Test
    @DisplayName("buscarEnderecoPeloCEP: lança CEPNaoEncontradoException quando cep nulo no retorno")
    void buscar_cepNuloNoRetorno() {
        ViaCEPResponse response = new ViaCEPResponse();
        // cep null

        when(viaCEPClient.getViaCEP("99999999")).thenReturn(response);

        assertThatThrownBy(() -> viaCEPService.buscarEnderecoPeloCEP("99999999"))
                .isInstanceOf(CEPNaoEncontradoException.class);
    }
}
