package com.marcaaiback.exception;

public class OperacaoNaoPermitidaException extends RuntimeException{

    public OperacaoNaoPermitidaException(String mensagem){
        super(mensagem);
    }
}
