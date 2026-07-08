package com.desafio.agendamento;

import com.desafio.agendamento.adapters.out.messaging.AgendamentoCriadoEvent;
import com.desafio.agendamento.adapters.out.messaging.KafkaAgendamentoEventPublisher;
import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.frameworks.config.AgendamentoProperties;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class KafkaAgendamentoEventPublisherTest {
    private final KafkaTemplate<String, AgendamentoCriadoEvent> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
    private final AgendamentoProperties properties =
            new AgendamentoProperties(new AgendamentoProperties.Kafka("agendamento-criado"));
    private final KafkaAgendamentoEventPublisher publisher =
            new KafkaAgendamentoEventPublisher(kafkaTemplate, properties);

    private Agendamento umAgendamento() {
        return Agendamento.novo("Maria", "12345678901", LocalDateTime.now().plusDays(1), null);
    }

    @Test
    void naoDevePropagarQuandoEnvioFalhaDeFormaAssincrona() {
        CompletableFuture<Object> futuroComFalha = new CompletableFuture<>();
        futuroComFalha.completeExceptionally(new RuntimeException("broker recusou a mensagem"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn((CompletableFuture) futuroComFalha);

        assertDoesNotThrow(() -> publisher.publicarCriacao(umAgendamento()));
    }

    @Test
    void naoDevePropagarQuandoEnvioFalhaDeFormaSincrona() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new TimeoutException("metadata do cluster indisponível"));

        assertDoesNotThrow(() -> publisher.publicarCriacao(umAgendamento()));
    }
}
