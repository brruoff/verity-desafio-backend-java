package com.desafio.agendamento.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Agendamento {
    private UUID id;
    private String pacienteNome;
    private String pacienteCpf;
    private LocalDateTime dataAgendamento;
    private StatusAgendamento status;
    private String observacao;
    private LocalDateTime criadoEm;

    public Agendamento(
            UUID id,
            String pacienteNome,
            String pacienteCpf,
            LocalDateTime dataAgendamento,
            StatusAgendamento status,
            String observacao,
            LocalDateTime criadoEm
    ) {
        this.id = id;
        this.pacienteNome = pacienteNome;
        this.pacienteCpf = pacienteCpf;
        this.dataAgendamento = dataAgendamento;
        this.status = status;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }

    public static Agendamento novo(
            String pacienteNome,
            String pacienteCpf,
            LocalDateTime dataAgendamento,
            String observacao
    ) {
        return new Agendamento(
                UUID.randomUUID(),
                pacienteNome,
                pacienteCpf,
                dataAgendamento,
                StatusAgendamento.PENDENTE,
                observacao,
                LocalDateTime.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public String getPacienteCpf() {
        return pacienteCpf;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void atualizarStatus(StatusAgendamento novoStatus) {
        this.status = novoStatus;
    }

    public void atualizarObservacao(String observacao) {
        this.observacao = observacao;
    }
}
