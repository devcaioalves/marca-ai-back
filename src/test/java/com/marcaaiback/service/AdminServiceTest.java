package com.marcaaiback.service;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import com.marcaaiback.jwt.JwtToken;
import com.marcaaiback.jwt.JwtUtils;
import com.marcaaiback.model.dto.admin.AdminRequest;
import com.marcaaiback.model.dto.admin.AdminResponse;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import com.marcaaiback.model.dto.admin.senha.AlterarSenhaRequest;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.SenhaResetToken;
import com.marcaaiback.repository.AdminRepository;
import com.marcaaiback.repository.SenhaResetTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private AdminRepository adminRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SenhaResetTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AdminService adminService;

    private Admin criarAdmin() {
        Admin a = new Admin();
        a.setId(1L);
        a.setNome("Admin");
        a.setEmail("admin@email.com");
        a.setTelefone("11999998888");
        a.setSenha("senhaCriptografada");
        return a;
    }

    private AdminRequest criarRequest() {
        AdminRequest r = new AdminRequest();
        r.setNome("Admin");
        r.setEmail("admin@email.com");
        r.setTelefone("11999998888");
        r.setSenha("senha123");
        r.setConfirmaSenha("senha123");
        return r;
    }

    // ---- criarAdmin ----

    @Test
    void deveCriarAdminComSucesso() {
        when(adminRepository.findAll()).thenReturn(List.of());
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(adminRepository.save(any())).thenReturn(criarAdmin());

        AdminResponse response = adminService.criarAdmin(criarRequest());

        assertThat(response.getNome()).isEqualTo("Admin");
    }

    @Test
    void deveLancarExcecaoQuandoAdminJaExiste() {
        when(adminRepository.findAll()).thenReturn(List.of(criarAdmin()));

        assertThatThrownBy(() -> adminService.criarAdmin(criarRequest()))
                .isInstanceOf(OperacaoNaoPermitidaException.class)
                .hasMessageContaining("Já existe um administrador");
    }

    @Test
    void deveLancarExcecaoQuandoSenhasNaoConferem() {
        when(adminRepository.findAll()).thenReturn(List.of());

        AdminRequest request = criarRequest();
        request.setConfirmaSenha("outraSenha");

        assertThatThrownBy(() -> adminService.criarAdmin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não coincidem");
    }

    // ---- buscar ----

    @Test
    void deveBuscarAdminComSucesso() {
        when(adminRepository.findAll()).thenReturn(List.of(criarAdmin()));

        AdminResponse response = adminService.buscar();

        assertThat(response.getEmail()).isEqualTo("admin@email.com");
    }

    @Test
    void deveLancarExcecaoQuandoAdminNaoEncontrado() {
        when(adminRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> adminService.buscar())
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ---- atualizar ----

    @Test
    void deveAtualizarAdminComSucesso() {
        Admin admin = criarAdmin();
        when(adminRepository.findAll()).thenReturn(List.of(admin));
        when(adminRepository.save(any())).thenReturn(admin);

        AdminResponse response = adminService.atualizar(criarRequest());

        assertThat(response.getNome()).isEqualTo("Admin");
    }

    // ---- autenticar ----

    @Test
    void deveAutenticarComSucesso() {
        Admin admin = criarAdmin();
        LoginRequest request = new LoginRequest();
        request.setLogin("admin@email.com");
        request.setSenha("senha123");

        when(adminRepository.findByEmailOrTelefone("admin@email.com", "admin@email.com"))
                .thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("senha123", "senhaCriptografada")).thenReturn(true);
        when(jwtUtils.gerarToken(1L, "admin@email.com")).thenReturn(new JwtToken("token123"));

        AdminResponse response = adminService.autenticar(request);

        assertThat(response.getToken()).isEqualTo("token123");
    }

    @Test
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        Admin admin = criarAdmin();
        LoginRequest request = new LoginRequest();
        request.setLogin("admin@email.com");
        request.setSenha("senhaErrada");

        when(adminRepository.findByEmailOrTelefone("admin@email.com", "admin@email.com"))
                .thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("senhaErrada", "senhaCriptografada")).thenReturn(false);

        assertThatThrownBy(() -> adminService.autenticar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    void deveLancarExcecaoQuandoLoginVazio() {
        LoginRequest request = new LoginRequest();
        request.setLogin("");
        request.setSenha("senha123");

        assertThatThrownBy(() -> adminService.autenticar(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obrigatórios");
    }

    // ---- alterarSenha ----

    @Test
    void deveAlterarSenhaComSucesso() {
        Admin admin = criarAdmin();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("senhaAtual", "senhaCriptografada")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha")).thenReturn("novoHash");

        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual("senhaAtual");
        request.setNovaSenha("novaSenha");
        request.setConfirmarNovaSenha("novaSenha");

        assertThatCode(() -> adminService.alterarSenha(1L, request))
                .doesNotThrowAnyException();
        verify(adminRepository).save(admin);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaAtualIncorreta() {
        Admin admin = criarAdmin();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("errada", "senhaCriptografada")).thenReturn(false);

        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setSenhaAtual("errada");
        request.setNovaSenha("novaSenha");
        request.setConfirmarNovaSenha("novaSenha");

        assertThatThrownBy(() -> adminService.alterarSenha(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha atual incorreta");
    }

    // ---- validarToken ----

    @Test
    void deveValidarTokenComSucesso() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("abc");
        reset.setExpiracao(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(reset));

        assertThatCode(() -> adminService.validarToken("abc"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarExcecaoQuandoTokenExpirado() {
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("abc");
        reset.setExpiracao(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(reset));

        assertThatThrownBy(() -> adminService.validarToken("abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado");
    }

    // ---- redefinirSenha ----

    @Test
    void deveRedefinirSenhaComSucesso() {
        Admin admin = criarAdmin();
        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken("abc");
        reset.setAdmin(admin);
        reset.setExpiracao(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(reset));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novoHash");

        assertThatCode(() -> adminService.redefinirSenha("abc", "novaSenha"))
                .doesNotThrowAnyException();

        verify(adminRepository).save(admin);
        verify(tokenRepository).delete(reset);
    }
}
