package com.marcaaiback.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.ToString;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorMessage", description = "Estrutura padrão de resposta a erros")
public class ErrorResponse {

    @Schema(description = "Carimbo de data/hora do erro", example = "2026-01-01T13:30:00")
    private final String timestamp = LocalDateTime.now().toString();

    @Schema(description = "Caminho da solicitação onde ocorreu o erro", example = "/events")
    private final String path;

    @Schema(description = "Método HTTP usado")
    private final String method;

    @Schema(description = "Código de status HTTP")
    private final int status;

    @Schema(description = "Texto de status HTTP")
    private final String error;

    @Schema(description = "Mensagem de erro")
    private final String message;

    @Schema(description = "Erros de validação em nível de campo (campos -> mensagem)")
    private final Map<String, String> fieldErrors;

    // Construtor para erros genéricos
    public ErrorResponse(HttpServletRequest request, int status, String error, String message) {
        this.path = request.getRequestURI();
        this.method = request.getMethod();
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = null;
    }

    // Construtor para erros de validação
    public ErrorResponse(HttpServletRequest request, int status, String error, String message, BindingResult bindingResult) {
        this.path = request.getRequestURI();
        this.method = request.getMethod();
        this.status = status;
        this.error = error;
        this.message = message;
        this.fieldErrors = extractFieldErrors(bindingResult);
    }

    // Método auxiliar para extrair erros de campo da validação
    private Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors()) return null;

        Map<String, String> map = new LinkedHashMap<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError f : fieldErrors) {
            map.put(f.getField(), f.getDefaultMessage());
        }
        return map.isEmpty() ? null : map;
    }
}
