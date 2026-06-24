package com.marcaaiback.component;

import com.marcaaiback.model.entity.Agendamento;
import org.springframework.stereotype.Component;

@Component
public class WhatsappMessageBuilder {

    public String boasVindas(){
        return "Olá, seja bem vinda(o)! Sou a assistente virtual da Nadja. " +
                "Como posso te ajudar?";
    }

    public String menuInicial(){
        return """
              Escolha uma opção:
              1- Fazer agendamento
              2- Ver serviços
              3- Ver horários
              4- Reagendar
              5- Cancelar agendamento
              6- Ver endereço
              7- Atendimento humanizado
              8- Finalizar atendimento
              """;
    }

    public String mensagemInvalida(){
        return "Mensagem inválida. Por favor, escolha uma opção válida.";
    }

    public String apenasTexto(){
        return "No momento não aceitamos nenhuma mensagem que não seja de texto. Por favor, insira somente mensagem de texto.";
    }

    public String perguntarFinalizar(){
        return """
                Deseja finalizar o atendimento?
                1- Finalizar Atendimento
                2- Não finalizar
                """;
    }

    public String mensagemConfirmacao(Agendamento agendamento){
        return """
                Olá!
                
                Seu agendamento de %s está marcado para amanhã às %s.
    
                Deseja confirmar?
    
                1- Confirmar
                2- Cancelar
                """.formatted(
                agendamento.getServico().getNome(),
                agendamento.getHoraInicio());
    }

    public String atendimentoHumano() {
        return """
                Seu atendimento será direcionado para nossa atendente.
                O tempo de resposta pode variar de acordo com o horário de funcionamento.
                """;
    }

    public String atendimentoFinalizado() {
        return """
            Atendimento finalizado.
            
            Obrigada pelo contato.
            Até a próxima!""";
    }
}
