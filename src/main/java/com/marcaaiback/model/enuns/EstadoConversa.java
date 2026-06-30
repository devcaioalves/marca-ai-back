package com.marcaaiback.model.enuns;

public enum EstadoConversa {
    INICIO_CONVERSA("Inicio"),
    MENU_INICIAL("Menu Inicial"),
    ESCOLHENDO_SERVICO("Escolhendo Serviço"),
    ESCOLHENDO_DATA("Escolhendo Data"),
    ESCOLHENDO_HORARIO("Escolhendo Horário"),
    DECIDINDO_DATA("Decidindo Data"),
    CONFIRMANDO_AGENDAMENTO("Confirmando Agendamento"),
    PERGUNTANDO_FINALIZAR("Perguntando Finalizar"),

    VISUALIZANDO_SERVICOS("Visualizando Servicos"),
    VISUALIZANDO_HORARIOS_PEDINDO_DATA("Visualizando Horarios Pedindo Data"),
    VISUALIZANDO_HORARIOS_OPCOES("Visualizando Horarios Opcoes"),

    VISUALIZANDO_HORARIOS_SEM_DISPONIBILIDADE("Visualizando Horarios Sem Disponibilidade"),

    REAGENDANDO_ESCOLHENDO_AGENDAMENTO("Reagendando Escolhendo Agendamento"),
    ESCOLHENDO_NOVA_DATA("Escolhendo Nova Data"),
    ESCOLHENDO_NOVO_HORARIO("Escolhendo Nova Horario"),
    CONFIRMANDO_REAGENDAMENTO("Reagendando Confirmacao"),
    REAGENDAMENTO_SEM_DISPONIBILIDADE("Reagendamentos Sem Disponibilidade"),

    CANCELANDO_ESCOLHENDO_AGENDAMENTO("Cancelando Escolhendo Agendamento"),
    CONFIRMANDO_CANCELAMENTO("Cancelando Cancelamento"),
    POS_CANCELAMENTO("Pós Cancelamento"),

    ENDERECO_POS_ACAO("Endereço Pos Ação"),
    ATENDIMENTO_HUMANO("Atendimento Humano"),
    CONFIRMANDO_AGENDAMENTO_FUTURO("Confirmando Agendamento Futuro"),
    FINALIZADO("Finalizado");

    private final String descricao;

    EstadoConversa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}