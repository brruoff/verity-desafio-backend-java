package com.desafio.agendamento;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.frameworks.exceptions.AgendamentoNaoEncontradoException;
import com.desafio.agendamento.frameworks.exceptions.OperacaoNaoPermitidaException;
import com.desafio.agendamento.frameworks.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import com.desafio.agendamento.usecases.impl.AgendamentoUseCaseImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendamentoUseCaseImplTest {
    private final AgendamentoRepositoryPort repository = Mockito.mock(AgendamentoRepositoryPort.class);
    private final AgendamentoEventPublisherPort eventPublisher = Mockito.mock(AgendamentoEventPublisherPort.class);
    private final AgendamentoUseCaseImpl useCase = new AgendamentoUseCaseImpl(repository, eventPublisher);

    @Test
    void deveCriarAgendamentoValido() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), "teste");
        when(repository.salvar(any())).thenReturn(agendamento);

        Agendamento salvo = useCase.criar(agendamento);

        assertNotNull(salvo);
        assertEquals(StatusAgendamento.PENDENTE, salvo.getStatus());
        verify(repository).salvar(any());
    }

    @Test
    void devePublicarEventoAoCriarAgendamento() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), "teste");
        when(repository.salvar(any())).thenReturn(agendamento);

        useCase.criar(agendamento);

        verify(eventPublisher).publicarCriacao(agendamento);
    }

    @Test
    void deveFalharQuandoCpfInvalido() {
        Agendamento agendamento = Agendamento.novo("Maria", "123", LocalDateTime.now().plusDays(1), "teste");

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(agendamento));

        assertEquals("CPF inválido", ex.getMessage());
    }

    @Test
    void deveFalharAoCancelarSemObservacao() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), null);
        when(repository.buscarPorId(any())).thenReturn(Optional.of(agendamento));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> useCase.atualizarStatus(UUID.randomUUID(), StatusAgendamento.CANCELADO, "   "));

        assertEquals("A observação é obrigatória para cancelar um agendamento", ex.getMessage());
    }

    @Test
    void deveFalharQuandoNomeForCurto() {
        Agendamento agendamento = Agendamento.novo("Ma", "12345678901", LocalDateTime.now().plusDays(1), "teste");

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(agendamento));

        assertEquals("Nome do paciente deve ter no mínimo 3 caracteres", ex.getMessage());
    }

    @Test
    void deveFalharQuandoDataForNoPassado() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().minusDays(1), "teste");

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> useCase.criar(agendamento));

        assertEquals("Data de agendamento não pode ser no passado", ex.getMessage());
    }

    @Test
    void deveFalharAoAtualizarStatusDeAgendamentoInexistente() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThrows(AgendamentoNaoEncontradoException.class,
                () -> useCase.atualizarStatus(id, StatusAgendamento.CONFIRMADO, null));
    }

    @Test
    void deveFalharAoAlterarStatusDeAgendamentoCancelado() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), "cancelado");
        agendamento.atualizarStatus(StatusAgendamento.CANCELADO);
        when(repository.buscarPorId(any())).thenReturn(Optional.of(agendamento));

        assertThrows(OperacaoNaoPermitidaException.class,
                () -> useCase.atualizarStatus(UUID.randomUUID(), StatusAgendamento.CONFIRMADO, null));
    }
}
