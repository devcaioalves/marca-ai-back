package com.marcaaiback.config;

import com.marcaaiback.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos de admin
                        .requestMatchers(HttpMethod.GET, "/api/admin/buscar-admin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/criar-admin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth").permitAll()

                        // Endpoints restritos de admin
                        .requestMatchers(HttpMethod.PUT, "/api/admin/atualizar-admin").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/alterar-senha-admin/**").authenticated()

                        // Endpoints públicos de agendamento (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/buscar-agendamento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/listar-agendamento-data/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/listar-por-cliente/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/listar-por-status/**").permitAll()

                        // Endpoints restritos de agendamentos (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos/criar-agendamento").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/agendamentos/cancelar-agendamento/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/agendamentos/realizar-agendamento/**").authenticated()

                        // Endpoints públicos de clientes (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/clientes/buscar-cliente/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clientes/buscar-pelo-telefone/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clientes/listar-clientes").permitAll()

                        // Endpoints restritos de clientes (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/clientes/criar-cliente").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/clientes/atualizar-cliente/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/clientes/deletar-cliente/**").authenticated()

                        // Endpoints públicos de horários disponíveis (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/horarios/buscar-horario/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/horarios/listar-horario-data/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/horarios/listar-horario-data/*/disponiveis").permitAll()

                        // Endpoints restritos de horários disponíveis (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/horarios/criar-horario").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/horarios/alterar-horario-disponibilidade/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/horarios/deletar-horario/**").authenticated()

                        // Endpoints públicos de mensagens (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/mensagens/listar-mensagem-agendamento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mensagens/listar-mensagem-cliente/**").permitAll()

                        // Endpoints restritos de mensagens (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/mensagens/registrar-mensagem").authenticated()

                        // Endpoints públicos de notificação (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/notificacoes/listar-notificacao-agendamento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notificacoes/listar-notificacoes-agendamentos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notificacoes/listar-notificacao-status/**").permitAll()

                        // Endpoints restritos de notificação (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/notificacoes/disparar-notificacao").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/notificacoes/marcar-notificacao-lida/**").authenticated()

                        // Endpoints públicos de serviços (consulta)
                        .requestMatchers(HttpMethod.GET, "/api/servicos/buscar-servico/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/listar-servicos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/listar-servicos/ativos").permitAll()

                        // Endpoints restritos de serviços (CRUD)
                        .requestMatchers(HttpMethod.POST, "/api/servicos/criar-servico").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/servicos/atualizar-servico/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/servicos/ativar-desativar-servico/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/servicos/deletar-servico/**").authenticated()

                        // Endpoints públicos de recuperação de senha
                        .requestMatchers(HttpMethod.POST, "/api/recuperarsenha/esqueci-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recuperarsenha/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/recuperarsenha/validar-token").permitAll()

                        // Swagger e docs
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}