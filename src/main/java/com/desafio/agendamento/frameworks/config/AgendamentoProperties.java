package com.desafio.agendamento.frameworks.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agendamento")
public record AgendamentoProperties(Kafka kafka) {
    public record Kafka(String topicoCriado) {}
}
