package com.desafio.agendamento.adapters.in.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "Agendamento API",
                "status", "UP",
                "swagger", "/swagger-ui.html"
        );
    }

}
