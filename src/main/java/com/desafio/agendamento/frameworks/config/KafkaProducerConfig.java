package com.desafio.agendamento.frameworks.config;

import com.desafio.agendamento.adapters.out.messaging.AgendamentoCriadoEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, AgendamentoCriadoEvent> agendamentoProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 3_000
        );
        JacksonJsonSerializer<AgendamentoCriadoEvent> valueSerializer = new JacksonJsonSerializer<AgendamentoCriadoEvent>().noTypeInfo();
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, AgendamentoCriadoEvent> agendamentoKafkaTemplate(
            ProducerFactory<String, AgendamentoCriadoEvent> agendamentoProducerFactory
    ) {
        return new KafkaTemplate<>(agendamentoProducerFactory);
    }
}
