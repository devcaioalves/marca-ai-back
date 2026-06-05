package com.marcaaiback.model.enuns;

public enum EstadoConversa {

    MENU_INICIAL("Menu Inicial"),

    ESCOLHENDO_SERVICO("Escolhendo Serviço"),

    ESCOLHENDO_DATA("Escolhendo Data"),

    ESCOLHENDO_HORARIO("Escolhendo Horário"),

    CONFIRMANDO_AGENDAMENTO("Confirmando Agendamento"),

    REAGENDANDO("Reagendando"),

    CANCELANDO("Cancelando"),

    ATENDIMENTO_HUMANO("Atendimento Humano"),

    FINALIZADO("Finalizado");

    private final String descricao;

    EstadoConversa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}