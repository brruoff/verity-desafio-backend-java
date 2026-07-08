package com.desafio.agendamento.adapters.out.messaging;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoCriadoEvent(
        UUID id,
        String pacienteNome,
        String pacienteCpf,
        LocalDateTime dataAgendamento,
        StatusAgendamento status,
        LocalDateTime criadoEm
) {
    public static AgendamentoCriadoEvent from(Agendamento agendamento) {
        return new AgendamentoCriadoEvent(
                agendamento.getId(),
                agendamento.getPacienteNome(),
                agendamento.getPacienteCpf(),
                agendamento.getDataAgendamento(),
                agendamento.getStatus(),
                agendamento.getCriadoEm()
        );
    }
}
