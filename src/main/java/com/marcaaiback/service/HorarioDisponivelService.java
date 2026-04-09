package com.marcaaiback.service;

import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelRequest;
import com.marcaaiback.model.dto.horariodisponivel.HorarioDisponivelResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.model.entity.HorarioDisponivel;
import com.marcaaiback.repository.HorarioDisponivelRepository;
import com.marcaaiback.validator.HorarioDisponivelValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioDisponivelService {

    private final HorarioDisponivelRepository horarioDisponivelRepository;
    private final AdminService adminService;
    private final HorarioDisponivelValidator horarioValidator;

    public HorarioDisponivelResponse criar(HorarioDisponivelRequest request) {

        // validações centralizadas
        horarioValidator.validarDuplicidade(request.getData(), request.getHoraInicio());
        horarioValidator.validarIntervalo(request.getHoraInicio(), request.getHoraFim());

        Admin admin = adminService.buscarEntidade();

        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setData(request.getData());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFim(request.getHoraFim());
        horario.setDisponivel(true);
        horario.setAdmin(admin);

        return toResponse(horarioDisponivelRepository.save(horario));
    }

    public void alterarDisponibilidade(Long id) {
        HorarioDisponivel horario = buscarEntidade(id);

        horario.setDisponivel(!horario.isDisponivel());

        horarioDisponivelRepository.save(horario);
    }

    public HorarioDisponivel salvar(HorarioDisponivel horario) {
        return horarioDisponivelRepository.save(horario);
    }

    public void deletar(Long id) {
        HorarioDisponivel horario = buscarEntidade(id);

        // validação antes de deletar
        horarioValidator.validarExclusao(horario);

        horarioDisponivelRepository.delete(horario);
    }

    // método interno reutilizável pelos outros services
    public HorarioDisponivel buscarEntidade(Long id) {
        return horarioDisponivelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Horário não encontrado."));
    }

    private HorarioDisponivelResponse toResponse(HorarioDisponivel horario) {
        HorarioDisponivelResponse response = new HorarioDisponivelResponse();
        response.setId(horario.getId());
        response.setData(horario.getData());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFim(horario.getHoraFim());
        response.setDisponivel(horario.isDisponivel());
        response.setAgendamentos(List.of()); // carregado separadamente se necessário
        return response;
    }
}