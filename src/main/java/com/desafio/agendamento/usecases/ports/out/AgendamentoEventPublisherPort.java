package com.desafio.agendamento.usecases.ports.out;

import com.desafio.agendamento.entities.Agendamento;

public interface AgendamentoEventPublisherPort {
    void publicarCriacao(Agendamento agendamento);
}
