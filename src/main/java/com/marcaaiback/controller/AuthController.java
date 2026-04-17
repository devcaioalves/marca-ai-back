package com.marcaaiback.controller;

import com.marcaaiback.exception.ErrorResponse;
import com.marcaaiback.jwt.JwtToken;
import com.marcaaiback.jwt.JwtUserDetails;
import com.marcaaiback.jwt.JwtUserDetailsService;
import com.marcaaiback.model.dto.admin.login.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUserDetailsService detailsServices;
    private final AuthenticationManager authenticationManager;


    @PostMapping
    public ResponseEntity<?> auth(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Processo de autenticação por identificador {}", loginRequest.getLogin());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getLogin(),
                            loginRequest.getSenha()
                    )
            );

            JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();

            JwtToken token = detailsServices.getTokenAuthenticated(userDetails.getAdmin());

            return ResponseEntity.ok(token);

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            request,
                            HttpStatus.UNAUTHORIZED.value(),
                            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                            "Login ou senha inválidos."
                    ));
        }
    }
}
