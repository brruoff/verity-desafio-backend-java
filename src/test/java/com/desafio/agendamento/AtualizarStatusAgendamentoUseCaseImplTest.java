package com.desafio.agendamento;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.usecases.exceptions.AgendamentoNaoEncontradoException;
import com.desafio.agendamento.usecases.exceptions.OperacaoNaoPermitidaException;
import com.desafio.agendamento.usecases.exceptions.RegraDeNegocioException;
import com.desafio.agendamento.usecases.impl.AtualizarStatusAgendamentoUseCaseImpl;
import com.desafio.agendamento.usecases.ports.out.AgendamentoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AtualizarStatusAgendamentoUseCaseImplTest {
    private final AgendamentoRepositoryPort repository = Mockito.mock(AgendamentoRepositoryPort.class);
    private final AtualizarStatusAgendamentoUseCaseImpl useCase = new AtualizarStatusAgendamentoUseCaseImpl(repository);

    @Test
    void deveFalharAoCancelarSemObservacao() {
        Agendamento agendamento = Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), null);
        when(repository.buscarPorId(any())).thenReturn(Optional.of(agendamento));

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                () -> useCase.atualizarStatus(UUID.randomUUID(), StatusAgendamento.CANCELADO, "   "));

        assertEquals("A observação é obrigatória para cancelar um agendamento", ex.getMessage());
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
