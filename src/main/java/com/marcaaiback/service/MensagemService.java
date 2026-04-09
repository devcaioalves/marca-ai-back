package com.marcaaiback.service;

import com.marcaaiback.model.dto.mensagem.MensagemRequest;
import com.marcaaiback.model.dto.mensagem.MensagemResponse;
import com.marcaaiback.model.entity.Agendamento;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.Mensagem;
import com.marcaaiback.repository.MensagemRepository;
import com.marcaaiback.validator.MensagemValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final ClienteService clienteService;
    private final AgendamentoService agendamentoService;
    private final MensagemValidator mensagemValidator;

    public MensagemResponse registrar(MensagemRequest request) {

        Cliente cliente = clienteService.buscarEntidade(request.getClienteId());
        Agendamento agendamento = agendamentoService.buscarEntidade(request.getAgendamentoId());

        // validações centralizadas
        mensagemValidator.validarConteudo(request.getConteudo());
        mensagemValidator.validarClienteDoAgendamento(cliente, agendamento);
        mensagemValidator.validarTipo(request.getTipo());

        Mensagem mensagem = new Mensagem();
        mensagem.setConteudo(request.getConteudo());
        mensagem.setTipo(request.getTipo());
        mensagem.setDataHora(LocalDateTime.now());
        mensagem.setCliente(cliente);
        mensagem.setAgendamento(agendamento);

        return toResponse(mensagemRepository.save(mensagem));
    }

    public List<MensagemResponse> listarPorAgendamento(Long agendamentoId) {
        return mensagemRepository.findByAgendamentoId(agendamentoId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MensagemResponse> listarPorCliente(Long clienteId) {
        return mensagemRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MensagemResponse toResponse(Mensagem mensagem) {
        MensagemResponse response = new MensagemResponse();
        response.setId(mensagem.getId());
        response.setConteudo(mensagem.getConteudo());
        response.setDataHora(mensagem.getDataHora());
        response.setTipo(mensagem.getTipo());
        response.setClienteId(mensagem.getCliente().getId());
        response.setClienteNome(mensagem.getCliente().getNome());
        response.setAgendamentoId(mensagem.getAgendamento().getId());
        return response;
    }
}