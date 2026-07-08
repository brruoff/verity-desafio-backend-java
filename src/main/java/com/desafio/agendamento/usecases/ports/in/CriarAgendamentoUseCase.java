package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Agendamento;

public interface CriarAgendamentoUseCase {
    Agendamento criar(Agendamento agendamento);
}
