package com.desafio.agendamento.frameworks.spring;

import com.desafio.agendamento.frameworks.config.AgendamentoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.desafio.agendamento")
@EntityScan(basePackages = "com.desafio.agendamento.adapters.out.persistence")
@EnableJpaRepositories(basePackages = "com.desafio.agendamento.adapters.out.persistence")
@EnableConfigurationProperties(AgendamentoProperties.class)
public class AgendamentoApplication {
    static void main(String[] args) {
        SpringApplication.run(AgendamentoApplication.class, args);
    }
}
