package com.marcaaiback.validator;

import com.marcaaiback.exception.OperacaoNaoPermitidaException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class MensagemWhatsappValidator {

    public boolean validarOpcao(String texto, int inicio, int fim) {
        try {
            int opcao = Integer.parseInt(texto.trim());

            return opcao >= inicio && opcao <= fim;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public int opcao(String texto) {
        return Integer.parseInt(texto.trim());
    }

    public LocalDate converterData(String texto) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            return LocalDate.parse(texto.trim(), formatter);
        } catch (Exception e) {
            throw new OperacaoNaoPermitidaException("Data inválida.");
        }
    }
}
