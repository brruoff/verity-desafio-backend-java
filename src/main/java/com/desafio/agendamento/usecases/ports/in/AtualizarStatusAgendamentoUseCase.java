package com.desafio.agendamento.usecases.ports.in;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;

import java.util.UUID;

public interface AtualizarStatusAgendamentoUseCase {
    Agendamento atualizarStatus(UUID id, StatusAgendamento status, String observacao);
}
