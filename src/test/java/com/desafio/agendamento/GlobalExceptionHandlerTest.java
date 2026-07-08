package com.desafio.agendamento;

import com.desafio.agendamento.frameworks.exceptions.ApiResponse;
import com.desafio.agendamento.frameworks.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveMapearExcecaoInesperadaParaErroInterno500() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(new RuntimeException("falha imprevista"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro interno inesperado", response.getBody().message());
    }
}
