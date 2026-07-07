package com.marcaaiback.service.chatbot;

import com.marcaaiback.component.WhatsappMessageBuilder;
import com.marcaaiback.model.dto.agendamento.AgendamentoRequest;
import com.marcaaiback.model.dto.agendamento.AgendamentoResponse;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.dto.servico.ServicoResponse;
import com.marcaaiback.model.dto.webhook.WhatsappWebhookRequest;
import com.marcaaiback.model.dto.webhook.WhatsappWebhookResponse;
import com.marcaaiback.model.entity.*;
import com.marcaaiback.model.enuns.TipoMensagemWhatsapp;
import com.marcaaiback.service.*;
import com.marcaaiback.validator.MensagemWhatsappValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.marcaaiback.model.enuns.EstadoConversa.*;

@Service
@RequiredArgsConstructor
public class WhatsappChatbotService {

    private static final ZoneId ZONA_BRASIL = ZoneId.of("America/Sao_Paulo");

    private final ConversaWhatsappService conversaService;
    private final WhatsappMessageBuilder messages;
    private final MensagemWhatsappValidator validator;
    private final ServicoService servicoService;
    private final HorarioDisponivelService horarioDisponivelService;
    private final AgendamentoService agendamentoService;
    private final AdminService adminService;
    private final NotificacaoAdminService notificacaoAdminService;

    @Transactional
    public WhatsappWebhookResponse processarMensagem(WhatsappWebhookRequest request) {
        ConversaWhatsapp conversa = conversaService.buscarOuCriarCliente(
                request.getTelefoneCliente(), request.getNomeCliente());

        if (Boolean.TRUE.equals(request.getFromMe())) {

            if ("/fim".equalsIgnoreCase(request.getMensagemWhatsapp().trim())) {

                conversa.setEstadoConversa(MENU_INICIAL);
                conversa.setAtendimentoHumano(false);

                conversaService.salvarFluxo(conversa);

                return WhatsappWebhookResponse.of(
                        conversa.getCliente().getTelefone(),
                        List.of("Seu atendimento foi encerrado. Sempre que precisar, basta enviar uma nova mensagem.")
                );
            }

            return WhatsappWebhookResponse.semResposta();
        }

        if (conversaService.conversaExpirada(conversa)) {
            conversa = conversaService.reiniciar(conversa);
        }

        if (!TipoMensagemWhatsapp.TEXTO.equals(request.getTipoMensagemWhatsapp())) {
            return WhatsappWebhookResponse.of(
                    conversa.getCliente().getTelefone(), List.of(messages.apenasTexto()));
        }

        String texto = normalizarTexto(request.getMensagemWhatsapp());

        if (tratarCancelamentoFluxo(texto)
                && conversa.getEstadoConversa() != MENU_INICIAL
                && conversa.getEstadoConversa() != INICIO_CONVERSA
                && conversa.getEstadoConversa() != FINALIZADO) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa, "Operação cancelada.", messages.perguntarFinalizar());
        }

        if (Boolean.TRUE.equals(conversa.getAtendimentoHumano())) {
            return WhatsappWebhookResponse.semResposta();
        }

        WhatsappWebhookResponse response = switch (conversa.getEstadoConversa()) {
            case INICIO_CONVERSA                        -> iniciarConversa(conversa);
            case MENU_INICIAL                           -> tratarMenuInicial(conversa, texto);
            case ESCOLHENDO_SERVICO                     -> tratarEscolhendoServico(conversa, texto);
            case ESCOLHENDO_DATA                        -> tratarEscolhendoData(conversa, texto);
            case ESCOLHENDO_HORARIO                     -> tratarEscolhendoHorario(conversa, texto);
            case DECIDINDO_DATA                         -> tratarDecidindoData(conversa, texto);
            case CONFIRMANDO_AGENDAMENTO                -> tratarConfirmandoAgendamento(conversa, texto);
            case PERGUNTANDO_FINALIZAR                  -> tratarPerguntandoFinalizar(conversa, texto);
            case VISUALIZANDO_SERVICOS                  -> tratarVisualizandoServicos(conversa, texto);
            case VISUALIZANDO_HORARIOS_PEDINDO_DATA     -> tratarVisualizandoHorariosPedindoData(conversa, texto);
            case VISUALIZANDO_HORARIOS_OPCOES           -> tratarVisualizandoHorariosOpcoes(conversa, texto);
            case VISUALIZANDO_HORARIOS_SEM_DISPONIBILIDADE -> tratarVisualizandoHorariosSemDisponibilidade(conversa, texto);
            case REAGENDANDO_ESCOLHENDO_AGENDAMENTO     -> tratarReagendandoEscolhendoAgendamento(conversa, texto);
            case ESCOLHENDO_NOVA_DATA                   -> tratarEscolhendoNovaData(conversa, texto);
            case ESCOLHENDO_NOVO_HORARIO                -> tratarEscolhendoNovoHorario(conversa, texto);
            case CONFIRMANDO_REAGENDAMENTO              -> tratarConfirmandoReagendamento(conversa, texto);
            case REAGENDAMENTO_SEM_DISPONIBILIDADE      -> tratarReagendamentoSemDisponibilidade(conversa, texto);
            case CANCELANDO_ESCOLHENDO_AGENDAMENTO      -> tratarCancelandoEscolhendoAgendamento(conversa, texto);
            case CONFIRMANDO_CANCELAMENTO               -> tratarConfirmandoCancelamento(conversa, texto);
            case POS_CANCELAMENTO                       -> tratarPosCancelamento(conversa, texto);
            case ENDERECO_POS_ACAO                      -> tratarEnderecoPosAcao(conversa, texto);
            case CONFIRMANDO_AGENDAMENTO_FUTURO         -> tratarConfirmandoAgendamentoFuturo(conversa, texto);
            case ATENDIMENTO_HUMANO                     -> aguardarAtendimentoHumano();
            case FINALIZADO -> {
                conversa = conversaService.reiniciar(conversa);
                yield iniciarConversa(conversa);
            }
            default -> voltarAoMenu(conversa);
        };

        conversaService.salvarFluxo(conversa);
        return response;
    }

    // ─────────────────────────────────────────
    // CANCELAMENTO DE FLUXO (palavras-chave)
    // ─────────────────────────────────────────

    private boolean tratarCancelamentoFluxo(String texto) {
        return texto.equals("0")
                || texto.equals("sair")
                || texto.equals("cancelar")
                || texto.equals("menu")
                || texto.equals("voltar");
    }

    // ─────────────────────────────────────────
    // INÍCIO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse iniciarConversa(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(MENU_INICIAL);
        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(
                messages.boasVindas(),
                messages.menuInicial()));
    }

    private WhatsappWebhookResponse tratarMenuInicial(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 8)) {
            return respostaInvalida(conversa, messages.menuInicial());
        }

        return switch (validator.opcao(texto)) {
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

    // ─────────────────────────────────────────
    // FINALIZAR
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse finalizarAtendimento(ConversaWhatsapp conversa) {
        conversaService.reiniciar(conversa);
        conversa.setEstadoConversa(FINALIZADO);
        return responder(conversa, "Atendimento finalizado. Obrigado pelo contato!");
    }

    private WhatsappWebhookResponse tratarPerguntandoFinalizar(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, messages.perguntarFinalizar());
        }

        if (validator.opcao(texto) == 1) {
            conversa.setEstadoConversa(FINALIZADO);
            return responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
        }

        conversa.setEstadoConversa(MENU_INICIAL);
        return responder(conversa, messages.menuInicial());
    }

    // ─────────────────────────────────────────
    // FLUXO DE AGENDAMENTO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse iniciarAgendamento(ConversaWhatsapp conversa) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if (servicos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa,
                    "Não há serviços disponíveis no momento.",
                    messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(ESCOLHENDO_SERVICO);
        return responder(conversa, montarListaServicos(servicos));
    }

    private WhatsappWebhookResponse tratarEscolhendoServico(ConversaWhatsapp conversa, String texto) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if (!validator.validarOpcao(texto, 1, servicos.size())) {
            return respostaInvalida(conversa, montarListaServicos(servicos));
        }

        ServicoResponse servicoEscolhido = servicos.get(validator.opcao(texto) - 1);
        conversa.setServicoId(servicoEscolhido.getId());
        conversa.setEstadoConversa(ESCOLHENDO_DATA);

        return responder(conversa,
                "Informe a data desejada no formato dd/mm/aaaa.",
                "0 - Voltar ao menu");
    }

    private WhatsappWebhookResponse tratarEscolhendoData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return responder(conversa,
                    messages.mensagemInvalida(),
                    "Informe a data desejada no formato dd/mm/aaaa.",
                    "0 - Voltar ao menu");
        }

        // Valida data passada
        if (data.isBefore(LocalDate.now(ZONA_BRASIL))) {
            return responder(conversa,
                    "Não é possível agendar para datas passadas.",
                    "Informe uma data a partir de hoje no formato dd/mm/aaaa.",
                    "0 - Voltar ao menu");
        }

        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(data, servico.getDuracao());
        } catch (Exception e) {
            conversa.setEstadoConversa(DECIDINDO_DATA);
            return responder(conversa,
                    "Não há horários disponíveis para essa data.",
                    """
                    O que deseja fazer?
                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(ESCOLHENDO_HORARIO);
        return responder(conversa, montarListaHorarios(horarios));
    }

    private WhatsappWebhookResponse tratarDecidindoData(ConversaWhatsapp conversa, String texto) {
        return switch (texto) {
            case "1" -> {
                conversa.setEstadoConversa(ESCOLHENDO_DATA);
                yield responder(conversa,
                        "Informe uma nova data no formato dd/mm/aaaa.",
                        "0 - Voltar ao menu");
            }
            case "2" -> voltarAoMenu(conversa);
            case "3" -> {
                conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
                yield responder(conversa, messages.perguntarFinalizar());
            }
            default -> respostaInvalida(conversa, """
                    O que deseja fazer?
                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        };
    }

    private WhatsappWebhookResponse tratarEscolhendoHorario(ConversaWhatsapp conversa, String texto) {
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(
                    conversa.getDataEscolhida(), servico.getDuracao());
        } catch (Exception e) {
            conversa.setEstadoConversa(ESCOLHENDO_DATA);
            return responder(conversa,
                    "Não há mais horários disponíveis para essa data.",
                    "Informe uma nova data no formato dd/mm/aaaa.",
                    "0 - Voltar ao menu");
        }

        if (!validator.validarOpcao(texto, 1, horarios.size())) {
            return respostaInvalida(conversa, montarListaHorarios(horarios));
        }

        HorarioDisponivelResponse horarioEscolhido = horarios.get(validator.opcao(texto) - 1);
        conversa.setHorarioId(horarioEscolhido.getId());
        conversa.setHoraInicioEscolhida(horarioEscolhido.getHoraInicio());
        conversa.setHoraFimEscolhida(horarioEscolhido.getHoraFim());
        conversa.setEstadoConversa(CONFIRMANDO_AGENDAMENTO);

        return responder(conversa, resumoAgendamento(servico, conversa));
    }

    private WhatsappWebhookResponse tratarConfirmandoAgendamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
            return respostaInvalida(conversa, resumoAgendamento(servico, conversa));
        }

        if (validator.opcao(texto) == 2) {
            limparDadosAgendamento(conversa);
            conversa.setEstadoConversa(MENU_INICIAL);
            return responder(conversa, "Agendamento cancelado.", messages.menuInicial());
        }

        AgendamentoRequest request = new AgendamentoRequest();
        request.setClienteId(conversa.getCliente().getId());
        request.setServicoId(conversa.getServicoId());
        request.setHorarioDisponivelId(conversa.getHorarioId());
        request.setHoraInicio(conversa.getHoraInicioEscolhida());

        AgendamentoResponse agendamento = agendamentoService.criar(request);
        conversa.setAgendamentoId(agendamento.getId());
        limparDadosAgendamento(conversa);
        conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);

        return responder(conversa, "Agendamento realizado com sucesso!", messages.perguntarFinalizar());
    }

    // ─────────────────────────────────────────
    // FLUXO DE VER SERVIÇOS
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse verServicos(ConversaWhatsapp conversa) {
        List<ServicoResponse> servicos = servicoService.listarAtivos();

        if (servicos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa,
                    "Não há serviços disponíveis no momento.",
                    messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(VISUALIZANDO_SERVICOS);
        return responder(conversa,
                montarListaServicosConsulta(servicos),
                """
                Deseja fazer algum agendamento?
                1- Sim
                2- Não""");
    }

    private WhatsappWebhookResponse tratarVisualizandoServicos(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja fazer algum agendamento?
                    1- Sim
                    2- Não""");
        }

        if (validator.opcao(texto) == 1) {
            return iniciarAgendamento(conversa);
        }

        conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
        return responder(conversa, messages.perguntarFinalizar());
    }

    // ─────────────────────────────────────────
    // FLUXO DE VER HORÁRIOS
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse pedirDataParaVerHorarios(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(VISUALIZANDO_HORARIOS_PEDINDO_DATA);
        return responder(conversa,
                "Informe a data que deseja consultar no formato dd/mm/aaaa.",
                "0 - Para Cancelar");
    }

    private WhatsappWebhookResponse tratarVisualizandoHorariosPedindoData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return responder(conversa,
                    messages.mensagemInvalida(),
                    "Informe a data que deseja consultar no formato dd/mm/aaaa.",
                    "0 - Para Cancelar");
        }

        // Valida data passada
        if (data.isBefore(LocalDate.now(ZONA_BRASIL))) {
            return responder(conversa,
                    "Não é possível consultar horários para datas passadas.",
                    "Informe uma data a partir de hoje no formato dd/mm/aaaa.",
                    "0 - Para cancelar");
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
                    3- Finalizar atendimento""");
        }

        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(VISUALIZANDO_HORARIOS_OPCOES);

        return responder(conversa,
                montarListaHorariosConsulta(data, horarios),
                """
                O que deseja fazer?
                1- Fazer agendamento
                2- Consultar outra data
                3- Voltar ao menu
                4- Finalizar atendimento""");
    }

    private WhatsappWebhookResponse tratarVisualizandoHorariosSemDisponibilidade(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 3)) {
            return respostaInvalida(conversa, """
                    O que deseja fazer?
                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        return switch (validator.opcao(texto)) {
            case 1 -> pedirDataParaVerHorarios(conversa);
            case 2 -> voltarAoMenu(conversa);
            case 3 -> {
                conversa.setEstadoConversa(FINALIZADO);
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
                    4- Finalizar atendimento""");
        }

        return switch (validator.opcao(texto)) {
            case 1 -> iniciarAgendamento(conversa);
            case 2 -> pedirDataParaVerHorarios(conversa);
            case 3 -> voltarAoMenu(conversa);
            case 4 -> {
                conversa.setEstadoConversa(FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
            }
            default -> voltarAoMenu(conversa);
        };
    }

    // ─────────────────────────────────────────
    // FLUXO DE REAGENDAMENTO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse iniciarReagendamento(ConversaWhatsapp conversa) {
        List<AgendamentoResponse> agendamentos =
                agendamentoService.listarReagendaveisPorCliente(conversa.getCliente().getId());

        if (agendamentos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa,
                    "Não há nenhum agendamento disponível para reagendar.",
                    messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(REAGENDANDO_ESCOLHENDO_AGENDAMENTO);
        return responder(conversa, montarListaAgendamentos("Qual agendamento deseja reagendar?", agendamentos));
    }

    private WhatsappWebhookResponse tratarReagendandoEscolhendoAgendamento(ConversaWhatsapp conversa, String texto) {
        List<AgendamentoResponse> agendamentos =
                agendamentoService.listarReagendaveisPorCliente(conversa.getCliente().getId());

        if (!validator.validarOpcao(texto, 1, agendamentos.size())) {
            return respostaInvalida(conversa,
                    montarListaAgendamentos("Qual agendamento deseja reagendar?", agendamentos));
        }

        AgendamentoResponse agendamento = agendamentos.get(validator.opcao(texto) - 1);
        conversa.setAgendamentoId(agendamento.getId());
        conversa.setServicoId(agendamento.getServicoId());
        conversa.setEstadoConversa(ESCOLHENDO_NOVA_DATA);

        return responder(conversa,
                "Informe a nova data no formato dd/mm/aaaa.",
                "0 - Para cancelar");
    }

    private WhatsappWebhookResponse tratarEscolhendoNovaData(ConversaWhatsapp conversa, String texto) {
        LocalDate data;

        try {
            data = validator.converterData(texto);
        } catch (Exception e) {
            return responder(conversa,
                    messages.mensagemInvalida(),
                    "Informe a nova data no formato dd/mm/aaaa.",
                    "0 - Para cancelar");
        }

        // Valida data passada
        if (data.isBefore(LocalDate.now(ZONA_BRASIL))) {
            return responder(conversa,
                    "Não é possível reagendar para datas passadas.",
                    "Informe uma data a partir de hoje no formato dd/mm/aaaa.",
                    "0 - Para cancelar");
        }

        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(data, servico.getDuracao());
        } catch (RuntimeException e) {
            conversa.setEstadoConversa(REAGENDAMENTO_SEM_DISPONIBILIDADE);
            return responder(conversa,
                    "Não há horários disponíveis para essa data.",
                    """
                    O que deseja fazer?
                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        conversa.setDataEscolhida(data);
        conversa.setEstadoConversa(ESCOLHENDO_NOVO_HORARIO);
        return responder(conversa, montarListaHorarios(horarios));
    }

    private WhatsappWebhookResponse tratarReagendamentoSemDisponibilidade(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 3)) {
            return respostaInvalida(conversa, """
                    O que deseja fazer?
                    1- Informar outra data
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        return switch (validator.opcao(texto)) {
            case 1 -> {
                conversa.setEstadoConversa(ESCOLHENDO_NOVA_DATA);
                yield responder(conversa,
                        "Informe a nova data no formato dd/mm/aaaa.",
                        "0 - Para cancelar");
            }
            case 2 -> voltarAoMenu(conversa);
            case 3 -> {
                conversa.setEstadoConversa(FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
            }
            default -> voltarAoMenu(conversa);
        };
    }

    private WhatsappWebhookResponse tratarEscolhendoNovoHorario(ConversaWhatsapp conversa, String texto) {
        Servico servico = servicoService.buscarEntidade(conversa.getServicoId());
        List<HorarioDisponivelResponse> horarios;

        try {
            horarios = horarioDisponivelService.gerarHorariosAgendaveis(
                    conversa.getDataEscolhida(), servico.getDuracao());
        } catch (Exception e) {
            conversa.setEstadoConversa(ESCOLHENDO_NOVA_DATA);
            return responder(conversa,
                    "Não há mais horários disponíveis para essa data.",
                    "Informe uma nova data no formato dd/mm/aaaa.",
                    "0 - Para cancelar");
        }

        if (!validator.validarOpcao(texto, 1, horarios.size())) {
            return respostaInvalida(conversa, montarListaHorarios(horarios));
        }

        HorarioDisponivelResponse horario = horarios.get(validator.opcao(texto) - 1);
        conversa.setHorarioId(horario.getId());
        conversa.setHoraInicioEscolhida(horario.getHoraInicio());
        conversa.setHoraFimEscolhida(horario.getHoraFim());
        conversa.setEstadoConversa(CONFIRMANDO_REAGENDAMENTO);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return responder(conversa, """
                Novo horário escolhido:
                Data: %s
                Horário: %s

                Deseja confirmar?
                1- Confirmar reagendamento
                2- Escolher outra data""".formatted(
                conversa.getDataEscolhida().format(fmt),
                conversa.getHoraInicioEscolhida()));
    }

    private WhatsappWebhookResponse tratarConfirmandoReagendamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja confirmar?
                    1- Confirmar reagendamento
                    2- Escolher outra data""");
        }

        if (validator.opcao(texto) == 2) {
            conversa.setEstadoConversa(ESCOLHENDO_NOVA_DATA);
            return responder(conversa,
                    "Informe a nova data no formato dd/mm/aaaa.",
                    "0 - Para cancelar");
        }

        agendamentoService.remarcar(conversa.getAgendamentoId(), conversa.getHorarioId());
        limparDadosAgendamento(conversa);
        conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);

        return responder(conversa, "Agendamento remarcado com sucesso!", messages.perguntarFinalizar());
    }

    // ─────────────────────────────────────────
    // FLUXO DE CANCELAMENTO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse iniciarCancelamento(ConversaWhatsapp conversa) {
        List<AgendamentoResponse> agendamentos =
                agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (agendamentos.isEmpty()) {
            conversa.setEstadoConversa(PERGUNTANDO_FINALIZAR);
            return responder(conversa,
                    "Não encontrei agendamentos ativos para cancelar.",
                    messages.perguntarFinalizar());
        }

        conversa.setEstadoConversa(CANCELANDO_ESCOLHENDO_AGENDAMENTO);
        return responder(conversa, montarListaAgendamentos("Qual agendamento deseja cancelar?", agendamentos));
    }

    private WhatsappWebhookResponse tratarCancelandoEscolhendoAgendamento(ConversaWhatsapp conversa, String texto) {
        List<AgendamentoResponse> agendamentos =
                agendamentoService.listarPorCliente(conversa.getCliente().getId());

        if (!validator.validarOpcao(texto, 1, agendamentos.size())) {
            return respostaInvalida(conversa,
                    montarListaAgendamentos("Qual agendamento deseja cancelar?", agendamentos));
        }

        AgendamentoResponse agendamento = agendamentos.get(validator.opcao(texto) - 1);
        conversa.setAgendamentoId(agendamento.getId());
        conversa.setEstadoConversa(CONFIRMANDO_CANCELAMENTO);

        return responder(conversa, """
                Deseja cancelar este agendamento?
                1- Sim, cancelar
                2- Não, manter""");
    }

    private WhatsappWebhookResponse tratarConfirmandoCancelamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, """
                    Deseja cancelar este agendamento?
                    1- Sim, cancelar
                    2- Não, manter""");
        }

        conversa.setEstadoConversa(POS_CANCELAMENTO);

        if (validator.opcao(texto) == 1) {
            agendamentoService.cancelar(conversa.getAgendamentoId());
            return responder(conversa, """
                    Agendamento cancelado com sucesso.

                    O que deseja fazer?
                    1- Fazer novo agendamento
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        return responder(conversa, """
                Cancelamento descartado.

                O que deseja fazer?
                1- Voltar ao menu
                2- Finalizar atendimento""");
    }

    private WhatsappWebhookResponse tratarPosCancelamento(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 3)) {
            return respostaInvalida(conversa, """
                    O que deseja fazer?
                    1- Fazer novo agendamento
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        return switch (validator.opcao(texto)) {
            case 1 -> iniciarAgendamento(conversa);
            case 2 -> voltarAoMenu(conversa);
            case 3 -> {
                conversa.setEstadoConversa(FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
            }
            default -> voltarAoMenu(conversa);
        };
    }

    // ─────────────────────────────────────────
    // FLUXO DE VER ENDEREÇO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse verEndereco(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(ENDERECO_POS_ACAO);
        Admin admin = adminService.buscarEntidade();
        Endereco e = admin.getEndereco();

        String enderecoFormatado = String.format("%s, %s - %s, %s - %s",
                e.getRua(), e.getNumero(), e.getBairro(), e.getCidade(), e.getEstado());

        String linkMaps = "https://www.google.com/maps/search/?api=1&query=" +
                enderecoFormatado.replace(" ", "+").replace(",", "%2C");

        return responder(conversa,
                "📍 Nosso endereço:\n" + enderecoFormatado,
                linkMaps,
                """
                O que deseja fazer?
                1- Fazer agendamento
                2- Voltar ao menu
                3- Finalizar atendimento""");
    }

    private WhatsappWebhookResponse tratarEnderecoPosAcao(ConversaWhatsapp conversa, String texto) {
        if (!validator.validarOpcao(texto, 1, 3)) {
            return respostaInvalida(conversa, """
                    Deseja fazer um agendamento?
                    1- Fazer agendamento
                    2- Voltar ao menu
                    3- Finalizar atendimento""");
        }

        return switch (validator.opcao(texto)) {
            case 1 -> iniciarAgendamento(conversa);
            case 2 -> voltarAoMenu(conversa);
            case 3 -> {
                conversa.setEstadoConversa(FINALIZADO);
                yield responder(conversa, "Atendimento finalizado. Obrigada pelo contato.");
            }
            default -> voltarAoMenu(conversa);
        };
    }

    // ─────────────────────────────────────────
    // FLUXO DE CONFIRMAÇÃO PRÉ-AGENDAMENTO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse tratarConfirmandoAgendamentoFuturo(ConversaWhatsapp conversa, String texto) {
        Agendamento agendamento = agendamentoService.buscarEntidade(conversa.getAgendamentoId());

        if (!validator.validarOpcao(texto, 1, 2)) {
            return respostaInvalida(conversa, messages.mensagemConfirmacao(agendamento));
        }

        if (validator.opcao(texto) == 1) {
            agendamentoService.confirmar(conversa.getAgendamentoId());
            conversa.setEstadoConversa(FINALIZADO);
            return responder(conversa, "Agendamento confirmado com sucesso!");
        }

        agendamentoService.cancelar(conversa.getAgendamentoId());
        conversa.setEstadoConversa(FINALIZADO);
        return responder(conversa, "Agendamento cancelado. O horário foi liberado.");
    }

    // ─────────────────────────────────────────
    // ATENDIMENTO HUMANO
    // ─────────────────────────────────────────

    private WhatsappWebhookResponse tratarAtendimentoHumano(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(ATENDIMENTO_HUMANO);
        conversa.setAtendimentoHumano(true);

        notificacaoAdminService.notificar(conversa);

        return WhatsappWebhookResponse.atendimentoHumano(
                conversa.getCliente().getTelefone(),
                List.of(messages.atendimentoHumano()));
    }

    private WhatsappWebhookResponse aguardarAtendimentoHumano() {
        return WhatsappWebhookResponse.semResposta();
    }

    // ─────────────────────────────────────────
    // MÉTODOS AUXILIARES
    // ─────────────────────────────────────────

    private void limparDadosAgendamento(ConversaWhatsapp conversa) {
        conversa.setServicoId(null);
        conversa.setHorarioId(null);
        conversa.setDataEscolhida(null);
        conversa.setHoraInicioEscolhida(null);
        conversa.setHoraFimEscolhida(null);
    }

    private WhatsappWebhookResponse respostaInvalida(ConversaWhatsapp conversa, String mensagemEsperada) {
        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(
                messages.mensagemInvalida(),
                mensagemEsperada));
    }

    private WhatsappWebhookResponse voltarAoMenu(ConversaWhatsapp conversa) {
        conversa.setEstadoConversa(MENU_INICIAL);
        return responder(conversa, messages.menuInicial());
    }

    private WhatsappWebhookResponse responder(ConversaWhatsapp conversa, String... respostas) {
        return WhatsappWebhookResponse.of(conversa.getCliente().getTelefone(), List.of(respostas));
    }

    private String normalizarTexto(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase();
    }

    private String montarListaServicos(List<ServicoResponse> servicos) {
        StringBuilder texto = new StringBuilder("Escolha um serviço:\n");
        for (int i = 0; i < servicos.size(); i++) {
            ServicoResponse s = servicos.get(i);
            texto.append(i + 1).append("- ").append(s.getNome());
            if (s.getValor() != null) texto.append(" | R$ ").append(s.getValor());
            if (s.getDuracao() != null) texto.append(" | ").append(s.getDuracao()).append(" min");
            texto.append("\n");
        }
        return texto.toString();
    }

    private String montarListaServicosConsulta(List<ServicoResponse> servicos) {
        StringBuilder texto = new StringBuilder("Serviços disponíveis:\n");
        for (int i = 0; i < servicos.size(); i++) {
            ServicoResponse s = servicos.get(i);
            texto.append(i + 1).append("- ").append(s.getNome());
            if (s.getValor() != null) texto.append(" | R$ ").append(s.getValor());
            if (s.getDuracao() != null) texto.append(" | ").append(s.getDuracao()).append(" min");
            texto.append("\n");
        }
        return texto.toString();
    }

    private String montarListaHorarios(List<HorarioDisponivelResponse> horarios) {
        StringBuilder texto = new StringBuilder("Escolha um horário disponível:\n");
        for (int i = 0; i < horarios.size(); i++) {
            HorarioDisponivelResponse h = horarios.get(i);
            texto.append(i + 1).append("- ")
                    .append(h.getHoraInicio()).append(" às ").append(h.getHoraFim()).append("\n");
        }
        return texto.toString();
    }

    private String montarListaHorariosConsulta(LocalDate data, List<HorarioDisponivelResponse> horarios) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder texto = new StringBuilder("Horários disponíveis para " + data.format(fmt) + ":\n\n");
        for (HorarioDisponivelResponse h : horarios) {
            texto.append(h.getHoraInicio()).append(" às ").append(h.getHoraFim()).append("\n");
        }
        return texto.toString();
    }

    private String montarListaAgendamentos(String titulo, List<AgendamentoResponse> agendamentos) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder texto = new StringBuilder(titulo).append("\n");
        for (int i = 0; i < agendamentos.size(); i++) {
            AgendamentoResponse a = agendamentos.get(i);
            texto.append(i + 1).append("- ")
                    .append(a.getServicoNome()).append(" - ")
                    .append(a.getData().format(fmt)).append(" às ")
                    .append(a.getHoraInicio()).append("\n");
        }
        return texto.toString();
    }

    private String resumoAgendamento(Servico servico, ConversaWhatsapp conversa) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return """
                Confira os dados do agendamento:
                Serviço: %s
                Data: %s
                Horário: %s

                Deseja confirmar?
                1- Confirmar agendamento
                2- Cancelar""".formatted(
                servico.getNome(),
                conversa.getDataEscolhida().format(fmt),
                conversa.getHoraInicioEscolhida());
    }

}