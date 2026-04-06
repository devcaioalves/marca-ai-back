package com.marcaaiback.service;

import com.marcaaiback.model.entity.Servico;
import com.marcaaiback.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;


}
