package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        String pacienteNome,
        String pacienteCpf,
        LocalDateTime dataAgendamento,
        StatusAgendamento status,
        String observacao,
        LocalDateTime criadoEm
) {
    public static AgendamentoResponse from(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId(),
                agendamento.getPacienteNome(),
                agendamento.getPacienteCpf(),
                agendamento.getDataAgendamento(),
                agendamento.getStatus(),
                agendamento.getObservacao(),
                agendamento.getCriadoEm()
        );
    }
}
