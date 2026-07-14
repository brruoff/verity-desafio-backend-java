package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.PaginaResultado;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.usecases.ports.in.BuscarAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;

import java.util.Optional;
import java.util.UUID;

public class BuscarAgendamentoUseCaseImpl implements BuscarAgendamentoUseCase {
    private final AgendamentoRepositoryPort repository;

    public BuscarAgendamentoUseCaseImpl(AgendamentoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public PaginaResultado<Agendamento> listar(Optional<StatusAgendamento> status, int pagina, int tamanho) {
        return repository.listar(status, pagina, tamanho);
    }

    @Override
    public Optional<Agendamento> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }
}
