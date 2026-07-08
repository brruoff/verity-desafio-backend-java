package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.PaginaResultado;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.frameworks.exceptions.AgendamentoNaoEncontradoException;
import com.desafio.agendamento.frameworks.exceptions.OperacaoNaoPermitidaException;
import com.desafio.agendamento.frameworks.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.ports.in.AtualizarStatusAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.in.BuscarAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.in.CriarAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class AgendamentoUseCaseImpl implements CriarAgendamentoUseCase, BuscarAgendamentoUseCase, AtualizarStatusAgendamentoUseCase {
    private final AgendamentoRepositoryPort repository;
    private final AgendamentoEventPublisherPort eventPublisher;

    public AgendamentoUseCaseImpl(AgendamentoRepositoryPort repository, AgendamentoEventPublisherPort eventPublisher) {
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

    @Override
    public PaginaResultado<Agendamento> listar(Optional<StatusAgendamento> status, int pagina, int tamanho) {
        return repository.listar(status, pagina, tamanho);
    }

    @Override
    public Optional<Agendamento> buscarPorId(UUID id) {
        return repository.buscarPorId(id);
    }

    @Override
    public Agendamento atualizarStatus(UUID id, StatusAgendamento status, String observacao) {
        Agendamento agendamento = repository.buscarPorId(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        validarAtualizacaoStatus(agendamento, status, observacao);

        agendamento.atualizarStatus(status);
        agendamento.atualizarObservacao(observacao == null || observacao.isBlank() ? agendamento.getObservacao() : observacao);
        return repository.salvar(agendamento);
    }

    private void validarCriacao(Agendamento agendamento) {
        validarNomePaciente(agendamento.getPacienteNome());
        validarCpf(agendamento.getPacienteCpf());
        validarDataAgendamento(agendamento.getDataAgendamento());
    }

    private void validarAtualizacaoStatus(Agendamento agendamento, StatusAgendamento novoStatus, String observacao) {
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new OperacaoNaoPermitidaException("Não é possível alterar o status de um agendamento cancelado");
        }

        if (novoStatus == StatusAgendamento.CANCELADO && (observacao == null || observacao.isBlank())) {
            throw new RegraDeNegocioException("A observação é obrigatória para cancelar um agendamento");
        }
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
