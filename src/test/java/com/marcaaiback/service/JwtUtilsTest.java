package com.marcaaiback.service;

import com.marcaaiback.jwt.JwtToken;
import com.marcaaiback.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretKey",
                "chave-secreta-para-testes-unitarios-com-32-chars!!");
    }

    @Test
    @DisplayName("gerarToken: gera token não nulo")
    void gerarToken_sucesso() {
        JwtToken token = jwtUtils.gerarToken(1L, "admin@email.com");

        assertThat(token).isNotNull();
        assertThat(token.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("tokenValido: retorna true para token válido")
    void tokenValido_verdadeiro() {
        JwtToken token = jwtUtils.gerarToken(1L, "admin@email.com");

        boolean valido = jwtUtils.tokenValido("Bearer " + token.getToken());

        assertThat(valido).isTrue();
    }

    @Test
    @DisplayName("tokenValido: retorna false para token inválido")
    void tokenValido_invalido() {
        boolean valido = jwtUtils.tokenValido("Bearer token.invalido.aqui");

        assertThat(valido).isFalse();
    }

    @Test
    @DisplayName("obterEmailDoToken: extrai email corretamente")
    void obterEmail_sucesso() {
        JwtToken token = jwtUtils.gerarToken(1L, "admin@email.com");

        String email = jwtUtils.obterEmailDoToken("Bearer " + token.getToken());

        assertThat(email).isEqualTo("admin@email.com");
    }

    @Test
    @DisplayName("obterEmailDoToken: retorna null para token inválido")
    void obterEmail_invalido() {
        String email = jwtUtils.obterEmailDoToken("Bearer invalido");

        assertThat(email).isNull();
    }

    @Test
    @DisplayName("refatorarToken: remove prefixo Bearer")
    void refatorarToken_sucesso() {
        String resultado = jwtUtils.refatorarToken("Bearer meu.token.aqui");

        assertThat(resultado).isEqualTo("meu.token.aqui");
    }
}
