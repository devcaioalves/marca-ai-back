package com.marcaaiback.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUserDetailsService detailsService;
    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUserDetailsService detailsService, JwtUtils jwtUtils) {
        this.detailsService = detailsService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String token = request.getHeader(JwtUtils.JWT_AUTHORIZATION);

        if (token == null || !token.startsWith(JwtUtils.JWT_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtUtils.tokenValido(token)) { // instância
            log.warn("JWT Token inválido ou expirado!");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtils.obterEmailDoToken(token); // instância

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            toAuth(request, email);
        }

        filterChain.doFilter(request, response);
    }

    private void toAuth(HttpServletRequest request, String email) {
        var authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}