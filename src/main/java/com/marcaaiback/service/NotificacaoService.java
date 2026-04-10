package com.marcaaiback.service;

import com.marcaaiback.model.dto.notificacao.NotificacaoRequest;
import com.marcaaiback.model.dto.notificacao.NotificacaoResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Notificacao;
import com.marcaaiback.model.enuns.StatusNotificacao;
import com.marcaaiback.repository.NotificacaoRepository;
import com.marcaaiback.validator.NotificacaoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final AgendamentoService agendamentoService;
    private final NotificacaoValidator notificacaoValidator;

    public NotificacaoResponse disparar(NotificacaoRequest request) {

        Agendamento agendamento = agendamentoService.buscarEntidade(request.getAgendamentoId());

        // validações centralizadas
        notificacaoValidator.validarTipoMensagem(request.getTipoDeMensagem());
        notificacaoValidator.validarAgendamentoParaNotificacao(agendamento);
        notificacaoValidator.validarDuplicidade(
                agendamento.getId(),
                request.getTipoDeMensagem()
        );

        Notificacao notificacao = new Notificacao();
        notificacao.setTipoDeMensagem(request.getTipoDeMensagem());
        notificacao.setDataEnvio(LocalDateTime.now());
        notificacao.setStatusNotificacao(StatusNotificacao.ENVIADO);
        notificacao.setAgendamento(agendamento);

        return toResponse(notificacaoRepository.save(notificacao));
    }

    public List<NotificacaoResponse> listarPorAgendamento(Long agendamentoId) {
        return notificacaoRepository.findByAgendamentoId(agendamentoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<NotificacaoResponse> listarPorStatus(StatusNotificacao status) {
        return notificacaoRepository.findByStatusNotificacao(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void marcarComoLida(Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Notificação não encontrada."));

        notificacaoValidator.validarMarcacaoComoLida(notificacao);

        notificacao.setStatusNotificacao(StatusNotificacao.LIDO);

        notificacaoRepository.save(notificacao);
    }

    private NotificacaoResponse toResponse(Notificacao notificacao) {
        NotificacaoResponse response = new NotificacaoResponse();
        response.setId(notificacao.getId());
        response.setDataEnvio(notificacao.getDataEnvio());
        response.setStatusNotificacao(notificacao.getStatusNotificacao());
        response.setTipoDeMensagem(notificacao.getTipoDeMensagem());
        response.setAgendamentoId(notificacao.getAgendamento().getId());
        response.setClienteNome(notificacao.getAgendamento().getCliente().getNome());
        response.setClienteTelefone(notificacao.getAgendamento().getCliente().getTelefone());
        return response;
    }
}