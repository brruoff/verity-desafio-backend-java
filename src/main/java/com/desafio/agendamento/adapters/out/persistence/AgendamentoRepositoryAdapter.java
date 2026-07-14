package com.desafio.agendamento.adapters.out.persistence;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.PaginaResultado;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.usecases.exceptions.OperacaoNaoPermitidaException;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AgendamentoRepositoryAdapter implements AgendamentoRepositoryPort {
    private final AgendamentoJpaRepository repository;

    public AgendamentoRepositoryAdapter(AgendamentoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        AgendamentoEntity entity = new AgendamentoEntity(agendamento);
        try {
            return repository.saveAndFlush(entity).toDomain();
        } catch (DataIntegrityViolationException ex) {
            throw new OperacaoNaoPermitidaException("Já existe um agendamento para este paciente neste horário");
        }
    }

    @Override
    public PaginaResultado<Agendamento> listar(Optional<StatusAgendamento> status, int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho);
        Page<AgendamentoEntity> page = status.isPresent()
                ? repository.findByStatus(status.get(), pageable)
                : repository.findAll(pageable);
        return new PaginaResultado<>(
                page.getContent().stream().map(AgendamentoEntity::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public Optional<Agendamento> buscarPorId(UUID id) {
        return repository.findById(id).map(AgendamentoEntity::toDomain);
    }
}
