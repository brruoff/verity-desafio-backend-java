package com.desafio.agendamento.frameworks.config;

import com.desafio.agendamento.usecases.impl.AtualizarStatusAgendamentoUseCaseImpl;
import com.desafio.agendamento.usecases.impl.BuscarAgendamentoUseCaseImpl;
import com.desafio.agendamento.usecases.impl.CriarAgendamentoUseCaseImpl;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public CriarAgendamentoUseCaseImpl criarAgendamentoUseCase(
            AgendamentoRepositoryPort repositoryPort,
            AgendamentoEventPublisherPort eventPublisherPort
    ) {
        return new CriarAgendamentoUseCaseImpl(repositoryPort, eventPublisherPort);
    }

    @Bean
    public BuscarAgendamentoUseCaseImpl buscarAgendamentoUseCase(AgendamentoRepositoryPort repositoryPort) {
        return new BuscarAgendamentoUseCaseImpl(repositoryPort);
    }

    @Bean
    public AtualizarStatusAgendamentoUseCaseImpl atualizarStatusAgendamentoUseCase(AgendamentoRepositoryPort repositoryPort) {
        return new AtualizarStatusAgendamentoUseCaseImpl(repositoryPort);
    }
}
