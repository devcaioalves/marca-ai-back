package com.marcaaiback.service;

import com.marcaaiback.model.dto.admin.AdminRequest;
import com.marcaaiback.model.dto.admin.AdminResponse;
import com.marcaaiback.model.entity.Admin;
import com.marcaaiback.repository.AdminRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminResponse buscar() {
        return toResponse(buscarEntidade());
    }

    public AdminResponse atualizar(AdminRequest request) {
        Admin admin = buscarEntidade();
        admin.setNome(request.getNome());
        admin.setTelefone(request.getTelefone());
        admin.setEmail(request.getEmail());
        return toResponse(adminRepository.save(admin));
    }

    // método interno reutilizável pelos outros services
    public Admin buscarEntidade() {
        return adminRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Admin não encontrado."));
    }

    private AdminResponse toResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setNome(admin.getNome());
        response.setTelefone(admin.getTelefone());
        response.setEmail(admin.getEmail());
        return response;
    }
}