package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.exception.NaoAutorizadoException;
import com.marcaaiback.exception.RecursoDuplicadoException;
import com.marcaaiback.jwt.JwtToken;
import com.marcaaiback.jwt.JwtUtils;
import com.marcaaiback.model.dto.admin.*;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import com.marcaaiback.model.dto.admin.senha.AlterarSenhaRequest;
import com.marcaaiback.model.dto.viacep.ViaCEPResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.Endereco;
import com.marcaaiback.model.entity.SenhaResetToken;
import com.marcaaiback.repository.AdminRepository;
import com.marcaaiback.repository.SenhaResetTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock AdminRepository adminRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SenhaResetTokenRepository tokenRepository;
    @Mock EmailService emailService;
    @Mock JwtUtils jwtUtils;
    @Mock ViaCEPService viaCEPService;

    @InjectMocks AdminService adminService;

    private Admin adminBase;
    private EnderecoRequest enderecoRequest;

    @BeforeEach
    void setUp() {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua A");
        endereco.setNumero("10");
        endereco.setBairro("Centro");
        endereco.setCidade("Recife");
        endereco.setEstado("PE");
        endereco.setCep("50000000");

        adminBase = Admin.builder()
                .id(1L)
                .nome("Admin Teste")
                .email("admin@email.com")
                .telefone("81999999999")
                .senha("senhaEncoded")
                .endereco(endereco)
                .build();

        enderecoRequest = EnderecoRequest.builder()
                .rua("Rua A")
                .numero("10")
                .bairro("Centro")
                .cep("50000000")
                .build();
    }

    // ── criarAdmin ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criarAdmin: sucesso quando não há admin cadastrado")
    void criarAdmin_sucesso() {
        AdminRequest request = new AdminRequest("Admin", "81999999999", "admin@email.com",
                enderecoRequest, "senha123", "senha123");

        ViaCEPResponse viacep = new ViaCEPResponse();
        viacep.setCep("50000-000");
        viacep.setBairro("Centro");
        viacep.setLocalidade("Recife");
        viacep.setUf("PE");

        when(adminRepository.findAll()).thenReturn(Collections.emptyList());
        when(viaCEPService.buscarEnderecoPeloCEP(anyString())).thenReturn(viacep);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(adminRepository.save(any())).thenReturn(adminBase);

        AdminResponse response = adminService.criarAdmin(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(adminRepository).save(any());
    }

    @Test
    @DisplayName("criarAdmin: lança RecursoDuplicadoException quando admin já existe")
    void criarAdmin_adminJaExiste() {
        when(adminRepository.findAll()).thenReturn(List.of(adminBase));

        AdminRequest request = new AdminRequest("Admin", "81999999999", "admin@email.com",
                enderecoRequest, "senha123", "senha123");

        assertThatThrownBy(() -> adminService.criarAdmin(request))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("Já existe um administrador");
    }

    @Test
    @DisplayName("criarAdmin: lança IllegalArgumentException quando senhas não coincidem")
    void criarAdmin_senhasNaoCoincidem() {
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());

        AdminRequest request = new AdminRequest("Admin", "81999999999", "admin@email.com",
                enderecoRequest, "senha123", "outraSenha");

        assertThatThrownBy(() -> adminService.criarAdmin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("senhas não coincidem");
    }

    // ── buscar ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscar: retorna admin existente")
    void buscar_sucesso() {
        when(adminRepository.findAll()).thenReturn(List.of(adminBase));

        AdminResponse response = adminService.buscar();

        assertThat(response.getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    @DisplayName("buscar: lança EntityNotFoundException quando não há admin")
    void buscar_semAdmin() {
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> adminService.buscar())
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── atualizar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("atualizar: sucesso com dados válidos")
    void atualizar_sucesso() {
        AdminRequest request = new AdminRequest("Novo Nome", "81988888888", "novo@email.com",
                null, "novaSenha1", "novaSenha1");

        when(adminRepository.findAll()).thenReturn(List.of(adminBase));
        when(adminRepository.save(any())).thenReturn(adminBase);

        AdminResponse response = adminService.atualizar(request);

        assertThat(response).isNotNull();
        verify(adminRepository).save(any());
    }

    // ── alterarSenha ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("alterarSenha: sucesso com senha atual correta")
    void alterarSenha_sucesso() {
        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaAtual", "novaSenha1", "novaSenha1");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(adminBase));
        when(passwordEncoder.matches("senhaAtual", "senhaEncoded")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha1")).thenReturn("novaEncoded");
        when(adminRepository.save(any())).thenReturn(adminBase);

        AdminResponse response = adminService.alterarSenha(1L, request);

        assertThat(response).isNotNull();
        verify(adminRepository).save(any());
    }

    @Test
    @DisplayName("alterarSenha: lança IllegalArgumentException com senha atual errada")
    void alterarSenha_senhaAtualErrada() {
        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaErrada", "novaSenha1", "novaSenha1");

        when(adminRepository.findById(1L)).thenReturn(Optional.of(adminBase));
        when(passwordEncoder.matches("senhaErrada", "senhaEncoded")).thenReturn(false);

        assertThatThrownBy(() -> adminService.alterarSenha(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha atual incorreta");
    }

    @Test
    @DisplayName("alterarSenha: lança EntityNotFoundException quando admin não existe")
    void alterarSenha_adminNaoEncontrado() {
        AlterarSenhaRequest request = new AlterarSenhaRequest("senha", "nova1234", "nova1234");
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.alterarSenha(99L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── autenticar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("autenticar: sucesso com credenciais válidas")
    void autenticar_sucesso() {
        LoginRequest request = new LoginRequest("admin@email.com", "senha123");

        when(adminRepository.findByEmailOrTelefone(anyString(), anyString()))
                .thenReturn(Optional.of(adminBase));
        when(passwordEncoder.matches("senha123", "senhaEncoded")).thenReturn(true);
        when(jwtUtils.gerarToken(anyLong(), anyString())).thenReturn(new JwtToken("token.jwt"));

        AuthResponse response = adminService.autenticar(request);

        assertThat(response.getToken()).isEqualTo("token.jwt");
        assertThat(response.getResponse().getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    @DisplayName("autenticar: lança NaoAutorizadoException com senha incorreta")
    void autenticar_senhaErrada() {
        LoginRequest request = new LoginRequest("admin@email.com", "errada");

        when(adminRepository.findByEmailOrTelefone(anyString(), anyString()))
                .thenReturn(Optional.of(adminBase));
        when(passwordEncoder.matches("errada", "senhaEncoded")).thenReturn(false);

        assertThatThrownBy(() -> adminService.autenticar(request))
                .isInstanceOf(NaoAutorizadoException.class);
    }

    @Test
    @DisplayName("autenticar: lança IllegalArgumentException com login em branco")
    void autenticar_loginEmBranco() {
        LoginRequest request = new LoginRequest("", "senha");

        assertThatThrownBy(() -> adminService.autenticar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obrigatórios");
    }

    @Test
    @DisplayName("autenticar: lança EntidadeNaoEncontradaException quando admin não encontrado")
    void autenticar_adminNaoEncontrado() {
        LoginRequest request = new LoginRequest("naoexiste@email.com", "senha123");

        when(adminRepository.findByEmailOrTelefone(anyString(), anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.autenticar(request))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    // ── enviarEmailRedefinicao ───────────────────────────────────────────────────

    @Test
    @DisplayName("enviarEmailRedefinicao: sucesso envia email e salva token")
    void enviarEmailRedefinicao_sucesso() {
        when(adminRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminBase));
        when(tokenRepository.findByAdmin(adminBase)).thenReturn(Optional.empty());
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).enviar(anyString(), anyString(), anyString());

        adminService.enviarEmailRedefinicao("admin@email.com");

        verify(tokenRepository).save(any());
        verify(emailService).enviar(eq("admin@email.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("enviarEmailRedefinicao: deleta token anterior antes de criar novo")
    void enviarEmailRedefinicao_deletaTokenAnterior() {
        SenhaResetToken tokenAntigo = new SenhaResetToken();
        tokenAntigo.setToken("old-token");
        tokenAntigo.setAdmin(adminBase);
        tokenAntigo.setExpiracao(LocalDateTime.now().plusMinutes(10));

        when(adminRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(adminBase));
        when(tokenRepository.findByAdmin(adminBase)).thenReturn(Optional.of(tokenAntigo));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).enviar(anyString(), anyString(), anyString());

        adminService.enviarEmailRedefinicao("admin@email.com");

        verify(tokenRepository).delete(tokenAntigo);
    }

    @Test
    @DisplayName("enviarEmailRedefinicao: lança EntityNotFoundException quando email não encontrado")
    void enviarEmailRedefinicao_emailNaoEncontrado() {
        when(adminRepository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.enviarEmailRedefinicao("nao@existe.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── validarToken ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("validarToken: token válido não lança exceção")
    void validarToken_valido() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("token-valido");
        reset.setAdmin(adminBase);
        reset.setExpiracao(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(reset));

        assertThatCode(() -> adminService.validarToken("token-valido")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarToken: lança IllegalArgumentException quando token expirado")
    void validarToken_expirado() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("token-expirado");
        reset.setAdmin(adminBase);
        reset.setExpiracao(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(reset));

        assertThatThrownBy(() -> adminService.validarToken("token-expirado"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    @DisplayName("validarToken: lança IllegalArgumentException quando token inválido")
    void validarToken_invalido() {
        when(tokenRepository.findByToken("invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.validarToken("invalido"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    // ── redefinirSenha ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("redefinirSenha: sucesso altera senha e deleta token")
    void redefinirSenha_sucesso() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("token-ok");
        reset.setAdmin(adminBase);
        reset.setExpiracao(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken("token-ok")).thenReturn(Optional.of(reset));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaEncoded");
        when(adminRepository.save(any())).thenReturn(adminBase);

        adminService.redefinirSenha("token-ok", "novaSenha");

        verify(adminRepository).save(adminBase);
        verify(tokenRepository).delete(reset);
    }

    @Test
    @DisplayName("redefinirSenha: lança IllegalArgumentException com token expirado")
    void redefinirSenha_tokenExpirado() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("token-exp");
        reset.setAdmin(adminBase);
        reset.setExpiracao(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("token-exp")).thenReturn(Optional.of(reset));

        assertThatThrownBy(() -> adminService.redefinirSenha("token-exp", "novaSenha"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }
}
