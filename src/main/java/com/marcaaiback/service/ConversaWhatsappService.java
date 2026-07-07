package com.marcaaiback.service;

import com.marcaaiback.exception.EntidadeNaoEncontradaException;
import com.marcaaiback.model.dto.cliente.ClienteRequest;
import com.marcaaiback.model.dto.conversa.ConversaWhatsappRequest;
import com.marcaaiback.model.dto.conversa.ConversaWhatsappResponse;
import com.marcaaiback.model.entity.Cliente;
import com.marcaaiback.model.entity.ConversaWhatsapp;
import com.marcaaiback.model.enuns.EstadoConversa;
import com.marcaaiback.repository.ConversaWhatsappRepository;
import com.marcaaiback.validator.ClienteValidator;
import com.marcaaiback.validator.ConversaWhatsappValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversaWhatsappService {

    private final ConversaWhatsappRepository conversaWhatsappRepository;
    private final ConversaWhatsappValidator conversaWhatsappValidator;
    private final ClienteService clienteService;
    private final ClienteValidator clienteValidator;

    public ConversaWhatsapp buscarOuCriarCliente(String telefone, String nomeCliente) {
        String telefonePadronizado = clienteValidator.validarEPadronizarTelefone(telefone);

        Optional<ConversaWhatsapp> conversa = conversaWhatsappRepository.findByClienteTelefone(telefonePadronizado);

        if(conversa.isPresent()){
            return conversa.get();
        }
        return criarConversaInicial(telefone, telefonePadronizado, nomeCliente);
    }

    // Cria a conversa inicial
    private ConversaWhatsapp criarConversaInicial(String telefoneOriginal, String telefonePadronizado, String nomeCliente) {
        Cliente cliente;

        try{
            cliente = clienteService.buscarClientePorTelefoneEntidade(telefonePadronizado);
        }catch (EntidadeNaoEncontradaException e){
            ClienteRequest request = new ClienteRequest();

            if(nomeCliente == null || nomeCliente.isBlank()){
                nomeCliente = "Cliente whatsapp";
            }

            request.setNome(nomeCliente);
            request.setTelefone(telefoneOriginal);
            clienteService.criar(request);
            cliente = clienteService.buscarClientePorTelefoneEntidade(telefonePadronizado);
        }

        ConversaWhatsapp conversaWhatsapp = new ConversaWhatsapp();
        conversaWhatsapp.setEstadoConversa(EstadoConversa.INICIO_CONVERSA);
        conversaWhatsapp.setAtendimentoHumano(false);
        conversaWhatsapp.setAtiva(true);
        conversaWhatsapp.setUltimaInteracao(LocalDateTime.now());
        conversaWhatsapp.setCliente(cliente);

        return conversaWhatsappRepository.save(conversaWhatsapp);
    }

    // Verifica a ultima interação
    public boolean conversaExpirada(ConversaWhatsapp conversaWhatsapp) {
        if (conversaWhatsapp.getUltimaInteracao() == null) {
            return false;
        }

        return conversaWhatsapp.getUltimaInteracao().isBefore(LocalDateTime.now().minusMinutes(30));
    }

    // Reinicia a conversa
    public ConversaWhatsapp reiniciar(ConversaWhatsapp conversaWhatsapp) {
        conversaWhatsapp.setEstadoConversa(EstadoConversa.INICIO_CONVERSA);
        conversaWhatsapp.setServicoId(null);
        conversaWhatsapp.setHorarioId(null);
        conversaWhatsapp.setDataEscolhida(null);
        conversaWhatsapp.setHoraInicioEscolhida(null);
        conversaWhatsapp.setHoraFimEscolhida(null);

        conversaWhatsapp.setAtendimentoHumano(false);

        conversaWhatsapp.setUltimaInteracao(LocalDateTime.now());

        return conversaWhatsappRepository.save(conversaWhatsapp);
    }

    // Salva o fluxo da conversa
    public ConversaWhatsapp salvarFluxo(ConversaWhatsapp conversaWhatsapp) {
        conversaWhatsapp.setUltimaInteracao(LocalDateTime.now());

        return conversaWhatsappRepository.save(conversaWhatsapp);
    }

    public ConversaWhatsappResponse criar(ConversaWhatsappRequest request) {

        conversaWhatsappValidator.validarEstado(request.getEstadoConversa());

        String telefonePadronizado = clienteValidator.validarEPadronizarTelefone(request.getTelefoneCliente());

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
        response.setHoraInicioEscolhida(conversaWhatsapp.getHoraInicioEscolhida());
        response.setHoraFimEscolhida(conversaWhatsapp.getHoraFimEscolhida());

        response.setClienteId(conversaWhatsapp.getCliente().getId());
        response.setClienteNome(conversaWhatsapp.getCliente().getNome());
        response.setTelefoneCliente(conversaWhatsapp.getCliente().getTelefone());

        return response;
    }
}