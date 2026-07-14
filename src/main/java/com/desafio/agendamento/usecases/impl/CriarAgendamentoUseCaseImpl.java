package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.usecases.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.ports.in.CriarAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;

import java.time.LocalDateTime;

public class CriarAgendamentoUseCaseImpl implements CriarAgendamentoUseCase {
    private final AgendamentoRepositoryPort repository;
    private final AgendamentoEventPublisherPort eventPublisher;

    public CriarAgendamentoUseCaseImpl(AgendamentoRepositoryPort repository, AgendamentoEventPublisherPort eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Agendamento criar(Agendamento agendamento) {
        validarCriacao(agendamento);
        Agendamento salvo = repository.salvar(agendamento);
        eventPublisher.publicarCriacao(salvo);
        return salvo;
    }

    private void validarCriacao(Agendamento agendamento) {
        validarNomePaciente(agendamento.getPacienteNome());
        validarCpf(agendamento.getPacienteCpf());
        validarDataAgendamento(agendamento.getDataAgendamento());
    }

    private void validarNomePaciente(String nome) {
        if (nome == null || nome.isBlank() || nome.length() < 3) {
            throw new RegraDeNegocioException("Nome do paciente deve ter no mínimo 3 caracteres");
        }
    }

    private void validarCpf(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new RegraDeNegocioException("CPF inválido");
        }
    }

    private void validarDataAgendamento(LocalDateTime dataAgendamento) {
        if (dataAgendamento == null || dataAgendamento.isBefore(LocalDateTime.now())) {
            throw new RegraDeNegocioException("Data de agendamento não pode ser no passado");
        }
    }
}
