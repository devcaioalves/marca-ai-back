package com.marcaaiback.client;

import com.marcaaiback.model.dto.viacep.ViaCEPResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "viacep", url = "${viacep.url}")
public interface ViaCEPClient {

    @GetMapping("/{cep}/json/")
    ViaCEPResponse getViaCEP(@PathVariable String cep);
}
