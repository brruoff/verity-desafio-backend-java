package com.desafio.agendamento.adapters.out.messaging;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.frameworks.config.AgendamentoProperties;
import com.desafio.agendamento.usecases.ports.out.AgendamentoEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaAgendamentoEventPublisher implements AgendamentoEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaAgendamentoEventPublisher.class);

    private final KafkaTemplate<String, AgendamentoCriadoEvent> kafkaTemplate;
    private final AgendamentoProperties properties;

    public KafkaAgendamentoEventPublisher(
            KafkaTemplate<String, AgendamentoCriadoEvent> kafkaTemplate,
            AgendamentoProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    @Override
    public void publicarCriacao(Agendamento agendamento) {
        try {
            kafkaTemplate.send(
                            properties.kafka().topicoCriado(),
                            agendamento.getId().toString(),
                            AgendamentoCriadoEvent.from(agendamento)
                    )
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Falha ao publicar evento agendamento-criado para o agendamento {}", agendamento.getId(), ex);
                        } else {
                            log.debug("Evento agendamento-criado publicado para o agendamento {} em {}",
                                    agendamento.getId(), result.getRecordMetadata());
                        }
                    });
        } catch (RuntimeException ex) {
            log.error("Falha síncrona ao publicar evento agendamento-criado para o agendamento {}", agendamento.getId(), ex);
        }
    }
}
