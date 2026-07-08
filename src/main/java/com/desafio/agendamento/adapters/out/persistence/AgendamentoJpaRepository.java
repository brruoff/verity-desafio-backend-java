package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.entities.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgendamentoJpaRepository extends JpaRepository<AgendamentoEntity, UUID> {
    Page<AgendamentoEntity> findByStatus(StatusAgendamento status, Pageable pageable);
}
