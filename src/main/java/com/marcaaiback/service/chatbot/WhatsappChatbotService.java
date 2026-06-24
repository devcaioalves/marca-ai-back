package com.marcaaiback.service.chatbot;

import com.marcaaiback.component.WhatsappMessageBuilder;
import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.model.dto.webhook.WhatsappWebhookRequest;
import com.marcaaiback.model.dto.webhook.WhatsappWebhookResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.ConversaWhatsapp;
import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.model.enuns.EstadoConversa;
import com.marcaaiback.model.enuns.TipoMensagemWhatsapp;
import com.marcaaiback.service.*;
import com.marcaaiback.validator.MensagemWhatsappValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.marcaaiback.model.enuns.EstadoConversa.*;

@Service
@RequiredArgsConstructor
public class WhatsappChatbotService {

    private final ConversaWhatsappService conversaService;
    private final WhatsappMessageBuilder messages;
    private final MensagemWhatsappValidator validator;
    private final ServicoService servicoService;
    private final HorarioDisponivelService horarioDisponivelService;
    private final AgendamentoService agendamentoService;
    private final AdminService adminService;

    @Transactional
    public WhatsappWebhookResponse processarMensagem(WhatsappWebhookRequest request) {
        ConversaWhatsapp conversa = conversaService.buscarOuCriarCliente(request.getTelefoneCliente(), request.getNomeCliente());

        if (conversaService.conversaExpirada(conversa)) {
            conversa = conversaService.reiniciar(conversa);
        }

        if (!TipoMensagemWhatsapp.TEXTO.equals(request.getTipoMensagemWhatsapp())) {
            return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(messages.apenasTexto()));
        }

        String texto = normalizarTexto(request.getMensagemWhatsapp());

        if(tratarCancelamentoFluxo(texto) &&
        conversa.getEstadoConversa() != MENU_INICIAL &&
        conversa.getEstadoConversa() != INICIO_CONVERSA &&
        conversa.getEstadoConversa() != FINALIZADO){
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);

            return responder(conversa, "Operação cancelada.", messages.perguntarFinalizar());
        }

        WhatsappWebhookResponse response = switch (conversa.getEstadoConversa()) {
            case INICIO_CONVERSA -> iniciarConversa(conversa);
            case MENU_INICIAL -> tratarMenuInicial(conversa, texto);
            case ESCOLHENDO_SERVICO -> tratarEscolhendoServico(conversa, texto);
            case ESCOLHENDO_DATA -> tratarEscolhendoData(conversa, texto);
            case ESCOLHENDO_HORARIO -> tratarEscolhendoHorario(conversa, texto);
            case DECIDINDO_DATA -> tratarDecidindoData(conversa, texto);
            case CONFIRMANDO_AGENDAMENTO -> tratarConfirmandoAgendamento(conversa, texto);
            case PERGUNTANDO_FINALIZAR -> tratarPerguntandoFinalizar(conversa, texto);

            case VISUALIZANDO_SERVICOS -> tratarVisualizandoServicos(conversa, texto);
            case VISUALIZANDO_HORARIOS_PEDINDO_DATA -> tratarVisualizandoHorariosPedindoData(conversa, texto);
            case VISUALIZANDO_HORARIOS_OPCOES -> tratarVisualizandoHorariosOpcoes(conversa, texto);

            case VISUALIZANDO_HORARIOS_SEM_DISPONIBILIDADE -> tratarVisualizandoHorariosSemDisponibilidade(conversa, texto);

            case REAGENDANDO_ESCOLHENDO_AGENDAMENTO -> tratarReagendandoEscolhendoAgendamento(conversa, texto);
            case ESCOLHENDO_NOVA_DATA -> tratarEscolhendoNovaData(conversa, texto);
            case ESCOLHENDO_NOVO_HORARIO -> tratarEscolhendoNovoHorario(conversa, texto);
            case CONFIRMANDO_REAGENDAMENTO -> tratarConfirmandoReagendamento(conversa, texto);

            case CANCELANDO_ESCOLHENDO_AGENDAMENTO -> tratarCancelandoEscolhendoAgendamento(conversa, texto);
            case CONFIRMANDO_CANCELAMENTO -> tratarConfirmandoCancelamento(conversa, texto);
            case POS_CANCELAMENTO -> tratarPosCancelamento(conversa, texto);

            case ENDERECO_POS_ACAO -> tratarEnderecoPosAcao(conversa, texto);
            case CONFIRMANDO_AGENDAMENTO_FUTURO -> tratarConfirmandoAgendamentoFuturo(conversa, texto);
            case ATENDIMENTO_HUMANO -> tratarAtendimentoHumano(conversa);
            case FINALIZADO -> {
                conversa = conversaService.reiniciar(conversa);
                yield iniciarConversa(conversa);
            }

            default -> voltarAoMenu(conversa);
        };

        conversaService.salvarFluxo(conversa);
        return response;
    }

    private boolean tratarCancelamentoFluxo(String texto) {
        String textoNormalizado = normalizarTexto(texto);

        return textoNormalizado.equals("0")
                || textoNormalizado.equals("sair")
                || textoNormalizado.equals("cancelar")
                || textoNormalizado.equals("menu")
                || textoNormalizado.equals("voltar");
    }

    // INICIA A CONVERSA
    private WhatsappWebhookResponse iniciarConversa(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(EstadoConversa.MENU_INICIAL);

        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(
                        messages.boasVindas(),
                        messages.menuInicial()));
    }

    private WhatsappWebhookResponse tratarMenuInicial(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 8)) {
            return respostaInvalida(conversa, messages.menuInicial());
        }

        int opcao = validator.opcao(texto);

        return switch (opcao) {
            case 1 -> iniciarAgendamento(conversa);
            case 2 -> verServicos(conversa);
            case 3 -> pedirDataParaVerHorarios(conversa);
            case 4 -> iniciarReagendamento(conversa);
            case 5 -> iniciarCancelamento(conversa);
            case 6 -> verEndereco(conversa);
            case 7 -> tratarAtendimentoHumano(conversa);
            case 8 -> finalizarAtendimento(conversa);
            default -> respostaInvalida(conversa, messages.menuInicial());
        };
    }

    // FINALIZA O ATENDIMENTO
    private WhatsappWebhookResponse finalizarAtendimento(ConversaWhatsapp conversa) {
        conversaService.reiniciar(conversa);
        conversa.setEstadoConversa(FINALIZADO);

        return responder(conversa, "Atendimento finalizado. Obrigado pelo contanto!");
    }

    // USO DO METODO PARA FINALIZAR ATENDIMENTO NAS OPÇÕES DE FINALIZAR
    private WhatsappWebhookResponse tratarPerguntandoFinalizar(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, messages.perguntarFinalizar());
        }

        if (validator.opcao(texto) == 1) {
            conversa.setEstadoConversa(EstadoConversa.FINALIZADO);
            return responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
        }

        conversa.setEstadoConversa(EstadoConversa.MENU_INICIAL);
        return responder(conversa, messages.menuInicial());
    }

    // FLUXO DO AGENDAMENTO
    private WhatsappWebhookResponse iniciarAgendamento(ConversaWhatsapp conversa) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if(servicos.isEmpty()){
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa, "Não há serviços disponíveis no momento.", messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_SERVICO);

        return responder(conversa, montarListaServicos(servicos));
    }

    private WhatsappWebhookResponse tratarEscolhendoServico(ConversaWhatsapp conversa, String texto) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if (!validator.validarOpcao(texto, 1, servicos.size())) {
            return respostaInvalida(conversa, montarListaServicos(servicos));
        }

        ServicoResponse servicoEscolhido = servicos.get(validator.opcao(texto) - 1);

        conversa.setServicoId(servicoEscolhido.getId());
        conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_DATA);

        return responder(conversa, "Informe a data desejada no formato dd/mm/aaaa.");
    }

    private WhatsappWebhookResponse tratarEscolhendoData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return responder(conversa, messages.mensagemInvalida(),
                    "Informe a data desejada no formato dd/mm/aaaa.");
        }
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(data, servico.getDuracao());
        } catch (Exception e) {
            conversa.setEstadoConversa(DECIDINDO_DATA);
            return responder(conversa, "Não há horários disponíveis para essa data.",
                    "1 - Informar outra data",
                    "2 - Voltar ao menu principal",
                    "3 - Finalizar atendimento");
        }
        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_HORARIO);

        return responder(conversa, montarListaHorarios(horarios));
    }

    private WhatsappWebhookResponse tratarDecidindoData(ConversaWhatsapp conversa, String texto) {
        switch (texto) {
            case "1":
                conversa.setEstadoConversa(ESCOLHENDO_DATA);
                return responder(conversa, "Informe uma nova data no formato dd/mm/aaaa.");

            case "2":
                conversa.setEstadoConversa(MENU_INICIAL);
                return responder(conversa, messages.menuInicial());

            case "3":
                conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
                return responder(conversa, messages.perguntarFinalizar());

            default:
                return respostaInvalida(conversa, """
                        1 - Informar outra data
                        2 - Voltar ao menu principal
                        3 - Finalizar atendimento""");
        }

    }

    private WhatsappWebhookResponse tratarEscolhendoHorario(ConversaWhatsapp conversa, String texto) {
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());

        List<HorarioDisponivelResponse> horarios;

        try{
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(conversa.getDataEscolhida(), servico.getDuracao());
        }catch (Exception e){
            conversa.setEstadoConversa(ESCOLHENDO_DATA);
            return responder(conversa, "Não há horários disponíveis para essa data.", "Informe novamente uma nova data no formato dd/mm/aaaa.");
        }

        if(!validator.validarOpcao(texto, 1, horarios.size())){
            return respostaInvalida(conversa, montarListaHorarios(horarios));
        }

        HorarioDisponivelResponse horarioEscolhido = horarios.get(validator.opcao(texto) - 1);

        conversa.setHorarioId(horarioEscolhido.getId());
        conversa.setHoraInicioEscolhida(horarioEscolhido.getHoraInicio());
        conversa.setHoraFimEscolhida(horarioEscolhido.getHoraFim());
        conversa.setEstadoConversa(EstadoConversa.CONFIRMANDO_AGENDAMENTO);

        return responder(conversa, resumoAgendamento(servico, conversa));
    }

    private WhatsappWebhookResponse tratarConfirmandoAgendamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            Servico servico = servicoService.buscarEntidade(conversa.getServicoId());

            return respostaInvalida(conversa, resumoAgendamento(servico, conversa));
        }

        if (validator.opcao(texto) == 2) {
            conversa.setServicoId(null);
            conversa.setHorarioId(null);
            conversa.setDataEscolhida(null);
            conversa.setHoraInicioEscolhida(null);
            conversa.setHoraFimEscolhida(null);
            conversa.setEstadoConversa(MENU_INICIAL);
            return responder(conversa, "Agendamento cancelado com sucesso.", messages.menuInicial());
        }

        AgendamentoRequest request = new AgendamentoRequest();
        request.setClienteId(conversa.getCliente().getId());
        request.setServicoId(conversa.getServicoId());
        request.setHorarioDisponivelId(conversa.getHorarioId());
        request.setHoraInicio(conversa.getHoraInicioEscolhida());

        AgendamentoResponse agendamento = agendamentoService.criar(request);

        conversa.setAgendamentoId(agendamento.getId());
        conversa.setEstadoConversa(EstadoConversa.PERGUNTANDO_FINALIZAR);

        return responder(conversa, "Agendamento realizado com sucesso.", messages.perguntarFinalizar());
    }

    // FLUXO DE VER SERVIÇOS
    private WhatsappWebhookResponse verServicos(ConversaWhatsapp conversa) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if(servicos.isEmpty()){
            return responder(conversa, "Não há serviços disponíveis no momento.", messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(EstadoConversa.VISUALIZANDO_SERVICOS);

        return responder(conversa, montarListaServicosConsulta(servicos),
                """
                    Deseja fazer algum agendamento?
                    1- Sim
                    2- Não
                   """);
    }

    private WhatsappWebhookResponse tratarVisualizandoServicos(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja fazer algum agendamento?
                    1- Sim
                    2- Não
                    """);
        }
        if (validator.opcao(texto) == 1) {
            return iniciarAgendamento(conversa);
        }

        conversa.setEstadoConversa(EstadoConversa.PERGUNTANDO_FINALIZAR);
        return responder(conversa, messages.perguntarFinalizar());
    }

    // FLUXO DE VER HORÁRIOS
    private WhatsappWebhookResponse pedirDataParaVerHorarios(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(EstadoConversa.VISUALIZANDO_HORARIOS_PEDINDO_DATA);
        return responder(conversa, "Informe a data que deseja consultar no formato dd/mm/aaaa.");
    }

    private WhatsappWebhookResponse tratarVisualizandoHorariosPedindoData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return respostaInvalida(conversa, "Informe a data que deseja consultar no formato dd/mm/aaaa.");
        }

        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.listarDisponiveisPorData(data);
        } catch (Exception e) {
            conversa.setEstadoConversa(VISUALIZANDO_HORARIOS_SEM_DISPONIBILIDADE);
            return responder(conversa,
                    "Não há horários disponíveis para essa data.",
                    """
                    O que deseja fazer?

                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento """);
        }

        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(EstadoConversa.VISUALIZANDO_HORARIOS_OPCOES);

        return responder(conversa,
                montarListaHorariosConsulta(data, horarios),
                """
                        O que deseja fazer?
                        1- Fazer agendamento
                        2- Consultar outra data
                        3- Voltar ao menu
                        4- Finalizar atendimento
                        """
        );
    }

    private WhatsappWebhookResponse tratarVisualizandoHorariosSemDisponibilidade(ConversaWhatsapp conversa, String texto) {
        if(!validator.validarOpcao(texto, 1, 3)) {
            return respostaInvalida(conversa, """
                    O que deseja fazer?

                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento """);
        }

        return switch (validator.opcao(texto)){
            case 1 -> pedirDataParaVerHorarios(conversa);
            case 2 -> voltarAoMenu(conversa);
            case 3 -> {
                conversa.setEstadoConversa(EstadoConversa.FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
        }
        default -> voltarAoMenu(conversa);
        };
    }

    private WhatsappWebhookResponse tratarVisualizandoHorariosOpcoes(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 4)) {
            return respostaInvalida(conversa, """
                    O que deseja fazer?
                    1- Fazer agendamento
                    2- Consultar outra data
                    3- Voltar ao menu
                    4- Finalizar atendimento
                    """);
        }

        return switch (validator.opcao(texto)) {
            case 1 -> iniciarAgendamento(conversa);
            case 2 -> pedirDataParaVerHorarios(conversa);
            case 3 -> voltarAoMenu(conversa);
            case 4 -> {
                conversa.setEstadoConversa(EstadoConversa.FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
            }
            default -> voltarAoMenu(conversa);
        };
    }

    // FLUXO DE REAGENDAMENTO
    private WhatsappWebhookResponse iniciarReagendamento(ConversaWhatsapp conversa) {
        List<AgendamentoResponse> agendamentos = agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (agendamentos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa,
                    "Não há nenhum agendamento seu para reagendar.",
                    messages.perguntarFinalizar());
        }
        conversa.setEstadoConversa(EstadoConversa.REAGENDANDO_ESCOLHENDO_AGENDAMENTO);

        return responder(conversa, montarListaAgendamentos("Qual agendamento deseja reagendar?", agendamentos));
    }

    private WhatsappWebhookResponse tratarReagendandoEscolhendoAgendamento(ConversaWhatsapp conversa, String texto) {

        List<AgendamentoResponse> agendamentos = agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (!validator.validarOpcao(texto, 1, agendamentos.size())) {
            return respostaInvalida(conversa, montarListaAgendamentos("Qual agendamento deseja reagendar?", agendamentos));
        }

        AgendamentoResponse agendamento = agendamentos.get(validator.opcao(texto) - 1);

        conversa.setAgendamentoId(agendamento.getId());
        conversa.setServicoId(agendamento.getServicoId());
        conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_NOVA_DATA);

        return responder(conversa, "Informe a nova data no formato dd/MM/yyyy.");
    }

    private WhatsappWebhookResponse tratarEscolhendoNovaData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return respostaInvalida(conversa, "Informe a nova data no formato dd/mm/aaaa.");
        }

        List<HorarioDisponivelResponse> horarios;
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(data, servico.getDuracao());
        } catch (RuntimeException e) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa, "Não há horários disponíveis para essa data.", messages.perguntarFinalizar());
        }

        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_NOVO_HORARIO);

        return responder(conversa, montarListaHorarios(horarios));
    }

    private WhatsappWebhookResponse tratarEscolhendoNovoHorario(ConversaWhatsapp conversa, String texto) {
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios = horarioDisponivelService.gerarHorariosAgendaveis(conversa.getDataEscolhida(),  servico.getDuracao());

        if (!validator.validarOpcao(texto, 1, horarios.size())) {
            return respostaInvalida(conversa, montarListaHorarios(horarios));
        }

        HorarioDisponivelResponse horario = horarios.get(validator.opcao(texto) - 1);

        conversa.setHorarioId(horario.getId());
        conversa.setHoraInicioEscolhida(horario.getHoraInicio());
        conversa.setHoraFimEscolhida(horario.getHoraFim());
        conversa.setEstadoConversa(CONFIRMANDO_REAGENDAMENTO);

        return responder(conversa, """
                Novo horário escolhido:
                Data: %s
                Horário: %s
                
                Deseja confirmar?
                1- Agendar
                2- Escolher outro horário
                """.formatted(conversa.getDataEscolhida(), conversa.getHoraInicioEscolhida()));
    }

    private WhatsappWebhookResponse tratarConfirmandoReagendamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja confirmar?
                    1- Agendar
                    2- Escolher outro horário
                    """);
        }

        if (validator.opcao(texto) == 2) {
            conversa.setEstadoConversa(EstadoConversa.ESCOLHENDO_NOVA_DATA);
            return responder(conversa, "Informe a nova data no formato dd/MM/yyyy.");
        }

        agendamentoService.remarcar(conversa.getAgendamentoId(), conversa.getHorarioId());
        conversa.setEstadoConversa(EstadoConversa.PERGUNTANDO_FINALIZAR);

        return responder(conversa, "Agendamento remarcado com sucesso.", messages.perguntarFinalizar());
    }

    // FLUXO DE CANCELAMENTO
    private WhatsappWebhookResponse iniciarCancelamento(ConversaWhatsapp conversa) {
        List<AgendamentoResponse> agendamentos = agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (agendamentos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa, "Não encontrei seus agendamentos ativos para cancelar.",
                    messages.perguntarFinalizar());
        }
        conversa.setEstadoConversa(EstadoConversa.CANCELANDO_ESCOLHENDO_AGENDAMENTO);

        return responder(conversa, montarListaAgendamentos("Qual agendamento deseja cancelar?", agendamentos));
    }

    private WhatsappWebhookResponse tratarCancelandoEscolhendoAgendamento(ConversaWhatsapp conversa, String texto) {
        List<AgendamentoResponse> agendamentos = agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (!validator.validarOpcao(texto, 1, agendamentos.size())) {
            return respostaInvalida(conversa, montarListaAgendamentos("Qual agendamento deseja cancelar?", agendamentos));
        }

        AgendamentoResponse agendamento = agendamentos.get(validator.opcao(texto) - 1);

        conversa.setAgendamentoId(agendamento.getId());
        conversa.setEstadoConversa(CONFIRMANDO_CANCELAMENTO);

        return responder(conversa, """
                Deseja cancelar este agendamento?
                1- Sim
                2- Não
                """);
    }

    private WhatsappWebhookResponse tratarConfirmandoCancelamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja cancelar este agendamento?
                    1- Sim
                    2- Não
                    """);
        }

        conversa.setEstadoConversa(EstadoConversa.POS_CANCELAMENTO);

        if (validator.opcao(texto) == 1) {
            agendamentoService.cancelar(conversa.getAgendamentoId());
            return responder(conversa, """
                    Agendamento cancelado com sucesso.
                    
                    O que deseja fazer?
                    1- Fazer novo agendamento
                    2- Voltar ao menu
                    3- Finalizar atendimento
                    """);
        }

        return responder(conversa, """
                Cancelamento descartado.
                
                O que deseja fazer?
                1- Voltar ao menu
                2- Finalizar atendimento
                """);
    }

    private WhatsappWebhookResponse tratarPosCancelamento(ConversaWhatsapp conversa, String texto) {
        if (validator.validarOpcao(texto, 1, 3)) {
            return switch (validator.opcao(texto)) {
                case 1 -> iniciarAgendamento(conversa);
                case 2 -> voltarAoMenu(conversa);
                case 3 -> {
                    conversa.setEstadoConversa(EstadoConversa.FINALIZADO);
                    yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
                }
                default -> voltarAoMenu(conversa);
            };
        }

        return respostaInvalida(conversa, """
                O que deseja fazer?
                1- Fazer novo agendamento
                2- Voltar ao menu
                3- Finalizar atendimento
                """);
    }

    // FLUXO DE VER ENDEREÇO
    private WhatsappWebhookResponse verEndereco(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(EstadoConversa.ENDERECO_POS_ACAO);

        Admin admin = adminService.buscarEntidade();

        return responder(conversa,
                "Nosso endereço é: " + admin.getEndereco(),
                """
                Deseja fazer um agendamento?
                1- Fazer agendamento
                2- Finalizar atendimento
                """);
    }

    private WhatsappWebhookResponse tratarEnderecoPosAcao(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja fazer um agendamento?
                    1- Fazer agendamento
                    2- Finalizar atendimento
                    """);
        }

        if (validator.opcao(texto) == 1) {
            return iniciarAgendamento(conversa);
        }

        conversa.setEstadoConversa(EstadoConversa.FINALIZADO);
        return responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
    }

    // FLUXO DE CONFIRMAR AGENDAMENTO UM DIA ANTES
    private WhatsappWebhookResponse tratarConfirmandoAgendamentoFuturo(ConversaWhatsapp conversa, String texto) {
        Agendamento agendamento = agendamentoService.buscarEntidade(conversa.getAgendamentoId());

        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, messages.mensagemConfirmacao(agendamento));
        }

        if (validator.opcao(texto) == 1) {
            agendamentoService.confirmar(conversa.getAgendamentoId());
            conversa.setEstadoConversa(EstadoConversa.FINALIZADO);

            return responder(conversa, "Agendamento confirmado com sucesso.");
        }

        agendamentoService.cancelar(conversa.getAgendamentoId());
        conversa.setEstadoConversa(EstadoConversa.FINALIZADO);

        return responder(conversa, "Agendamento cancelado com sucesso. O horário foi liberado."
        );
    }


    // FLUXO DE LEVAR AO ATENDIMENTO HUMANO
    private WhatsappWebhookResponse tratarAtendimentoHumano(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(EstadoConversa.ATENDIMENTO_HUMANO);
        conversa.setAtendimentoHumano(true);

        return WhatsappWebhookResponse.atendimentoHumano(
                conversa.getCliente().getTelefone(),
                List.of(messages.atendimentoHumano())
        );
    }


    // METODOS AUXILIARES
    private WhatsappWebhookResponse respostaInvalida(ConversaWhatsapp conversa, String mensagemEsperada) {
        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(
                messages.mensagemInvalida(),
                mensagemEsperada
        ));
    }

    private String montarListaServicos(List<ServicoResponse> servicos) {
        StringBuilder texto = new StringBuilder("Escolha um serviço:\n");

        for (int i = 0; i < servicos.size(); i++) {
            ServicoResponse servico = servicos.get(i);

            texto.append(i + 1)
                    .append("- ")
                    .append(servico.getNome());

            if (servico.getValor() != null) {
                texto.append(" | R$ ")
                        .append(servico.getValor());
            }

            if(servico.getDuracao() != null) {
                texto.append(" | ")
                    .append(servico.getDuracao())
                    .append(" min ");
            }

            texto.append("\n");
        }
        return texto.toString();
    }

    private String montarListaServicosConsulta(List<ServicoResponse> servicos) {
        StringBuilder texto = new StringBuilder("Serviços disponíveis:\n");

        for (int i = 0; i < servicos.size(); i++) {
            ServicoResponse servico = servicos.get(i);

            texto.append(i + 1)
                    .append("- ")
                    .append(servico.getNome());

            if (servico.getValor() != null) {
                texto.append(" | R$ ")
                        .append(servico.getValor());
            }

            if(servico.getDuracao() != null) {
                texto.append(" | ")
                        .append(servico.getDuracao())
                        .append(" min ");
            }

            texto.append("\n");
        }
        return texto.toString();
    }

    private String montarListaHorarios(List<HorarioDisponivelResponse> horarios) {
        StringBuilder texto = new StringBuilder("Escolha um horário disponível:\n");

        for (int i = 0; i < horarios.size(); i++) {
            HorarioDisponivelResponse horario = horarios.get(i);

            texto.append(i + 1)
                    .append("- ")
                    .append(horario.getHoraInicio())
                    .append(" às ")
                    .append(horario.getHoraFim())
                    .append("\n");
        }

        return texto.toString();
    }

    private String montarListaHorariosConsulta(LocalDate data, List<HorarioDisponivelResponse> horarios) {
        DateTimeFormatter dataFormatada = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        StringBuilder texto = new StringBuilder("Horários disponíveis para " + data.format(dataFormatada) + ":\n\n");

        for(HorarioDisponivelResponse horario : horarios) {
            texto.append(horario.getHoraInicio())
                    .append(" às ")
                    .append(horario.getHoraFim())
                    .append("\n");
        }
        return texto.toString();
    }

    private String montarListaAgendamentos(String titulo, List<AgendamentoResponse> agendamentos) {
        StringBuilder texto = new StringBuilder(titulo).append("\n");

        for (int i = 0; i < agendamentos.size(); i++) {
            AgendamentoResponse agendamento = agendamentos.get(i);

            texto.append(i + 1)
                    .append("- ")
                    .append(agendamento.getServicoNome())
                    .append(" - ")
                    .append(agendamento.getData())
                    .append(" às ")
                    .append(agendamento.getHoraInicio())
                    .append("\n");
        }

        return texto.toString();
    }

    private String resumoAgendamento(Servico servico, ConversaWhatsapp conversa) {
        return """
                Confira os dados do agendamento:
                Serviço: %s
                Data: %s
                Horário: %s

                Deseja confirmar?
                1- Agendar
                2- Não agendar
                """.formatted(
                servico.getNome(),
                conversa.getDataEscolhida(),
                conversa.getHoraInicioEscolhida()
        );
    }

    private WhatsappWebhookResponse voltarAoMenu(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(EstadoConversa.MENU_INICIAL);
        return responder(conversa, messages.menuInicial());
    }

    private WhatsappWebhookResponse responder(ConversaWhatsapp conversa, String... respostas) {
        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(respostas));
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase();
    }
}
