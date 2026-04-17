package com.marcaaiback.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request){
        log.warn("Erro de validação: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), "Falha na validação", ex.getBindingResult());
        return buildResponse(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest request){
        log.warn("Erro de argumento inválido: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> handleNaoAutorizado(NaoAutorizadoException ex, HttpServletRequest request){
        log.warn("Erro de autorização: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleCredenciais(BadCredentialsException ex, HttpServletRequest request){
        log.warn("Erro de credenciais: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, body);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicado(RecursoDuplicadoException ex, HttpServletRequest request){
        log.warn("Recurso duplicado: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, body);
    }

    @ExceptionHandler({CEPNaoEncontradoException.class, RecursoNaoEncontradoException.class, EntidadeNaoEncontradaException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNaoEncontrado(RuntimeException ex, HttpServletRequest request){
        log.warn("Não encontrado: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, body);
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleOperacaoNaoPermitida(OperacaoNaoPermitidaException ex, HttpServletRequest request){
        log.warn("Operação não permitida: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, body);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest request) {
        log.warn("Violação de regra de negócios: {}", ex.getMessage());
        ErrorResponse body = new ErrorResponse(request, HttpStatus.UNPROCESSABLE_ENTITY.value(), HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, body);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> handleConflito(ConflitoException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(request, HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleErroInesperado(Exception ex, HttpServletRequest request){
        log.error("Erro inesperado na API", ex);
        ErrorResponse body = new ErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, body);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, ErrorResponse body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
