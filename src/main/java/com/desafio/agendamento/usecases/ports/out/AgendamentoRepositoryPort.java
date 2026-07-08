package com.desafio.agendamento.usecases.ports.out;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.PaginaResultado;
import com.desafio.agendamento.entities.StatusAgendamento;

import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepositoryPort {
    Agendamento salvar(Agendamento agendamento);
    PaginaResultado<Agendamento> listar(Optional<StatusAgendamento> status, int pagina, int tamanho);
    Optional<Agendamento> buscarPorId(UUID id);
}
