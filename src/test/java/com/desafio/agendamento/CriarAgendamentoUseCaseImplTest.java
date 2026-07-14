package com.desafio.agendamento;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.usecases.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.impl.CriarAgendamentoUseCaseImpl;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CriarAgendamentoUseCaseImplTest {
    private final AgendamentoRepositoryPort repository = Mockito.mock(AgendamentoRepositoryPort.class);
    private final AgendamentoEventPublisherPort eventPublisher = Mockito.mock(AgendamentoEventPublisherPort.class);
    private final CriarAgendamentoUseCaseImpl useCase = new CriarAgendamentoUseCaseImpl(repository, eventPublisher);

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
}
