package com.marcaaiback.model.enuns;

public enum TipoDeMensagem {
    TEXTO("Texto"),
    CONFIRMACAO("Confirmação"),
    LEMBRETE("Lembrete"),
    CANCELAMENTO("Cancelamento");

    private final String descricao;

    TipoDeMensagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}