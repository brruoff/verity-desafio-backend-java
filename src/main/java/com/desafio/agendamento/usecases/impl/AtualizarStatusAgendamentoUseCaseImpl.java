package com.desafio.agendamento.usecases.impl;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.usecases.exceptions.AgendamentoNaoEncontradoException;
import com.desafio.agendamento.usecases.exceptions.OperacaoNaoPermitidaException;
import com.desafio.agendamento.usecases.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.ports.in.AtualizarStatusAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;

import java.util.UUID;

public class AtualizarStatusAgendamentoUseCaseImpl implements AtualizarStatusAgendamentoUseCase {
    private final AgendamentoRepositoryPort repository;

    public AtualizarStatusAgendamentoUseCaseImpl(AgendamentoRepositoryPort repository) {
        this.repository = repository;
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

    private void validarAtualizacaoStatus(Agendamento agendamento, StatusAgendamento novoStatus, String observacao) {
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new OperacaoNaoPermitidaException("Não é possível alterar o status de um agendamento cancelado");
        }

        if (novoStatus == StatusAgendamento.CANCELADO && (observacao == null || observacao.isBlank())) {
            throw new RegraDeNegocioException("A observação é obrigatória para cancelar um agendamento");
        }
    }
}
