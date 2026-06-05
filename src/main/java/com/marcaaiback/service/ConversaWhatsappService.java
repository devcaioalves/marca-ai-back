package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.conversa.ConversaWhatsappRequest;
import com.marcaaiback.model.dto.conversa.ConversaWhatsappResponse;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.ConversaWhatsapp;
import com.marcaaiback.repository.ConversaWhatsappRepository;
import com.marcaaiback.validator.ClienteValidator;
import com.marcaaiback.validator.ConversaWhatsappValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversaWhatsappService {

    private final ConversaWhatsappRepository conversaWhatsappRepository;
    private final ConversaWhatsappValidator conversaWhatsappValidator;
    private final ClienteService clienteService;
    private final ClienteValidator clienteValidator;

    public ConversaWhatsappResponse criar(ConversaWhatsappRequest request) {

        conversaWhatsappValidator.validarEstado(request.getEstadoConversa());

        String telefonePadronizado =
                clienteValidator.validarEPadronizarTelefone(request.getTelefoneCliente());

        Cliente cliente = clienteService.buscarClientePorTelefoneEntidade(telefonePadronizado);

        ConversaWhatsapp conversaWhatsapp = new ConversaWhatsapp();

        conversaWhatsapp.setEstadoConversa(request.getEstadoConversa());
        conversaWhatsapp.setServicoId(request.getServicoId());
        conversaWhatsapp.setHorarioId(request.getHorarioId());
        conversaWhatsapp.setDataEscolhida(request.getDataEscolhida());
        conversaWhatsapp.setUltimaInteracao(LocalDateTime.now());
        conversaWhatsapp.setCliente(cliente);

        return toResponse(conversaWhatsappRepository.save(conversaWhatsapp));
    }

    public ConversaWhatsappResponse buscarPorId(Long id) {

        return toResponse(buscarEntidade(id));
    }

    public ConversaWhatsappResponse buscarPorTelefone(String telefone) {

        String telefonePadronizado =
                clienteValidator.validarEPadronizarTelefone(telefone);

        ConversaWhatsapp conversaWhatsapp =
                conversaWhatsappRepository.findByClienteTelefone(telefonePadronizado)
                        .orElseThrow(() ->
                                new EntidadeNaoEncontradaException("Conversa não encontrada."));

        return toResponse(conversaWhatsapp);
    }

    public List<ConversaWhatsappResponse> listarTodos() {

        List<ConversaWhatsapp> conversas = conversaWhatsappRepository.findAll();

        if (conversas.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Nenhuma conversa encontrada.");
        }

        return conversas.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ConversaWhatsappResponse atualizar(Long id,
                                              ConversaWhatsappRequest request) {

        ConversaWhatsapp conversaWhatsapp = buscarEntidade(id);

        conversaWhatsappValidator.validarEstado(request.getEstadoConversa());

        String telefonePadronizado =
                clienteValidator.validarEPadronizarTelefone(request.getTelefoneCliente());

        Cliente cliente =
                clienteService.buscarClientePorTelefoneEntidade(telefonePadronizado);

        conversaWhatsapp.setEstadoConversa(request.getEstadoConversa());
        conversaWhatsapp.setServicoId(request.getServicoId());
        conversaWhatsapp.setHorarioId(request.getHorarioId());
        conversaWhatsapp.setDataEscolhida(request.getDataEscolhida());
        conversaWhatsapp.setUltimaInteracao(LocalDateTime.now());
        conversaWhatsapp.setCliente(cliente);

        return toResponse(conversaWhatsappRepository.save(conversaWhatsapp));
    }

    public void deletar(Long id) {

        ConversaWhatsapp conversaWhatsapp = buscarEntidade(id);

        conversaWhatsappRepository.delete(conversaWhatsapp);
    }

    public ConversaWhatsapp buscarEntidade(Long id) {

        return conversaWhatsappRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Conversa não encontrada."));
    }

    private ConversaWhatsappResponse toResponse(ConversaWhatsapp conversaWhatsapp) {

        ConversaWhatsappResponse response = new ConversaWhatsappResponse();

        response.setId(conversaWhatsapp.getId());
        response.setEstadoConversa(conversaWhatsapp.getEstadoConversa());
        response.setServicoId(conversaWhatsapp.getServicoId());
        response.setHorarioId(conversaWhatsapp.getHorarioId());
        response.setDataEscolhida(conversaWhatsapp.getDataEscolhida());
        response.setUltimaInteracao(conversaWhatsapp.getUltimaInteracao());

        response.setClienteId(conversaWhatsapp.getCliente().getId());
        response.setClienteNome(conversaWhatsapp.getCliente().getNome());
        response.setTelefoneCliente(conversaWhatsapp.getCliente().getTelefone());

        return response;
    }
}