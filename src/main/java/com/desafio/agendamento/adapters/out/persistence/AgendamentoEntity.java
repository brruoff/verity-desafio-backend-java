package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agendamentos", uniqueConstraints = @UniqueConstraint(columnNames = {"paciente_cpf", "data_agendamento"}))
public class AgendamentoEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String pacienteNome;

    @Column(nullable = false, length = 11)
    private String pacienteCpf;

    @Column(nullable = false)
    private LocalDateTime dataAgendamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status;

    private String observacao;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    public AgendamentoEntity() {}

    public AgendamentoEntity(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.pacienteNome = agendamento.getPacienteNome();
        this.pacienteCpf = agendamento.getPacienteCpf();
        this.dataAgendamento = agendamento.getDataAgendamento();
        this.status = agendamento.getStatus();
        this.observacao = agendamento.getObservacao();
        this.criadoEm = agendamento.getCriadoEm();
    }

    public Agendamento toDomain() {
        return new Agendamento(id, pacienteNome, pacienteCpf, dataAgendamento, status, observacao, criadoEm);
    }
}
