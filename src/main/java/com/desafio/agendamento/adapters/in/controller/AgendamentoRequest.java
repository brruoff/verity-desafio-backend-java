package com.desafio.agendamento.adapters.in.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AgendamentoRequest(
        @Schema(description = "Nome do paciente", example = "Maria Souza")
        @NotBlank(message = "Nome do paciente é obrigatório")
        @Size(min = 3, message = "Nome do paciente deve ter no mínimo 3 caracteres")
        String pacienteNome,

        @Schema(description = "CPF do paciente com 11 dígitos", example = "12345678901")
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF inválido")
        String pacienteCpf,

        @Schema(description = "Data e hora do agendamento", example = "2026-12-31T10:30:00")
        @NotNull(message = "Data de agendamento é obrigatória")
        LocalDateTime dataAgendamento,

        @Schema(description = "Observação opcional", example = "Apartamento 302")
        String observacao
) {
}
