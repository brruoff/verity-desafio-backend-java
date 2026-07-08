package com.desafio.agendamento.frameworks.config;

import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import com.desafio.agendamento.usecases.impl.AgendamentoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public AgendamentoUseCaseImpl agendamentoUseCase(
            AgendamentoRepositoryPort repositoryPort,
            AgendamentoEventPublisherPort eventPublisherPort
    ) {
        return new AgendamentoUseCaseImpl(repositoryPort, eventPublisherPort);
    }
}
