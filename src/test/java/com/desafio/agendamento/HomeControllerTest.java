package com.desafio.agendamento;

import com.desafio.agendamento.adapters.in.controller.HomeController;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {
    @Test
    void deveRetornarStatusUpENomeDaAplicacao() {
        Map<String, String> resposta = new HomeController().home();

        assertEquals("UP", resposta.get("status"));
        assertEquals("Agendamento API", resposta.get("application"));
    }
}
