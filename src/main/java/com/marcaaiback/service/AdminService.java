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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final SenhaResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final JwtUtils jwtUtils; // injetado como componente

    public AdminResponse criarAdmin(AdminRequest request) {
        if (!adminRepository.findAll().isEmpty()) {
            throw new OperacaoNaoPermitidaException("Já existe um administrador cadastrado.");
        }
        validarSenha(request.getSenha(), request.getConfirmaSenha());

        Admin admin = Admin.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .senha(passwordEncoder.encode(request.getSenha()))
                .build();

        return toResponse(adminRepository.save(admin));
    }

    public AdminResponse buscar() {
        return toResponse(buscarEntidade());
    }

    public AdminResponse atualizar(AdminRequest request) {
        Admin admin = buscarEntidade();
        admin.setNome(request.getNome());
        admin.setTelefone(request.getTelefone());
        admin.setEmail(request.getEmail());
        return toResponse(adminRepository.save(admin));
    }

    public Admin buscarEntidade() {
        return adminRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Admin não encontrado."));
    }

    public Admin buscarPorLogin(String login) {
        return adminRepository
                .findByEmailOrTelefone(login, login)
                .orElseThrow(() -> new EntityNotFoundException("Administrador não encontrado."));
    }

    public void alterarSenha(Long id, AlterarSenhaRequest request) {
        Admin admin = buscarAdminPeloId(id);

        if (!passwordEncoder.matches(request.getSenhaAtual(), admin.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }

        validarSenha(request.getNovaSenha(), request.getConfirmarNovaSenha());

        admin.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        adminRepository.save(admin);
    }

    public AdminResponse autenticar(LoginRequest request) {
        if (!campoValido(request.getLogin()) || !campoValido(request.getSenha())) {
            throw new IllegalArgumentException("Login e senha são obrigatórios.");
        }

        Admin admin = adminRepository
                .findByEmailOrTelefone(request.getLogin(), request.getLogin())
                .orElseThrow(() -> new EntityNotFoundException("Admin não encontrado."));

        if (!passwordEncoder.matches(request.getSenha(), admin.getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }

        JwtToken jwtToken = jwtUtils.gerarToken(admin.getId(), admin.getEmail()); // instância

        AdminResponse response = toResponse(admin);
        response.setToken(jwtToken.getToken());

        return response;
    }

    public void enviarEmailRedefinicao(String email) {
        Admin admin = adminRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Administrador não encontrado.")
        );

        tokenRepository.findByAdmin(admin).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();

        SenhaResetToken reset = new SenhaResetToken();
        reset.setToken(token);
        reset.setAdmin(admin);
        reset.setExpiracao(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(reset);

        String link = "http://localhost:5173/receive-code?token=" + token;

        emailService.enviar(
                admin.getEmail(),
                "Redefinição de Senha - MarcaAi",
                "\nToken: " + token +
                        "\nClique no link para redefinir sua senha:\n\n" + link
        );
    }

    public void validarToken(String token) {
        SenhaResetToken reset = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (reset.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }
    }

    public void redefinirSenha(String token, String novaSenha) {
        SenhaResetToken reset = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (reset.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }

        Admin admin = reset.getAdmin();
        admin.setSenha(passwordEncoder.encode(novaSenha));
        adminRepository.save(admin);
        tokenRepository.delete(reset);
    }

    private AdminResponse toResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setNome(admin.getNome());
        response.setTelefone(admin.getTelefone());
        response.setEmail(admin.getEmail());
        return response;
    }

    private boolean campoValido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private Admin buscarAdminPeloId(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Admin com id '" + id + "' não encontrado."));
    }

    private void validarSenha(String senha, String confirmarSenha) {
        if (!campoValido(senha)) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }
        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }
    }
}