package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.entities.StatusAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @Schema(description = "Novo status do agendamento", example = "CONFIRMADO")
        @NotNull(message = "Status é obrigatório")
        StatusAgendamento status,

        @Schema(description = "Observação para alteração do status", example = "Paciente confirmou o atendimento")
        String observacao
) {
}
