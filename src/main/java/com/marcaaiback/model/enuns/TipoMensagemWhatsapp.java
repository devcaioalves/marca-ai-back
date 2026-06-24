package com.marcaaiback.model.enuns;

public enum TipoMensagemWhatsapp {

    TEXTO("Texto"),
    AUDIO("Audio"),
    IMAGEM("Imagem"),
    VIDEO("Video"),
    DOCUMENTO("Documento"),
    UNKNOWN("Unknown");

    private final String descricao;

    TipoMensagemWhatsapp(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
