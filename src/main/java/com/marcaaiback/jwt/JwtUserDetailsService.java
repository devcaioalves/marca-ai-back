package com.marcaaiback.jwt;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final JwtUtils jwtUtils; // injetado como componente

    @Override
    public UserDetails loadUserByUsername(String login) throws EntidadeNaoEncontradaException {
        Admin admin = adminRepository.findByEmailOrTelefone(login, login)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Administrador não encontrado: " + login));
        return new JwtUserDetails(admin);
    }

    public JwtToken getTokenAuthenticated(Admin admin) {
        return jwtUtils.gerarToken(admin.getId(), admin.getEmail()); // instância
    }
}