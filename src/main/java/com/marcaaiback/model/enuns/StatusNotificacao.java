package com.marcaaiback.model.enuns;

public enum StatusNotificacao {
    ENVIADO("Enviado"),
    LIDO("Lido");

    private final String descricao;

    StatusNotificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}