# Desafio Backend Java

## Visão geral
Microserviço para gestão de agendamentos domiciliares, seguindo uma estrutura hexagonal com separação entre domínio, casos de uso, portas, adaptadores e infraestrutura.

## Tecnologias
- Java 25
- Spring Boot 4.1.0 (Spring Framework 7)
- Spring Web
- Spring Data JPA + H2
- Bean Validation
- SpringDoc OpenAPI / Swagger
- Spring Kafka — publica o evento `agendamento-criado` ao criar um agendamento

## Como rodar

Pré-requisito: Java 25 instalado.

1. Entre na pasta do projeto.
2. Execute:
   ```bash
   ./mvnw spring-boot:run
   ```
3. A aplicação ficará disponível em http://localhost:8080
4. O Swagger estará em http://localhost:8080/swagger-ui/index.html

## Testes

```bash
./mvnw test
```

- 12 testes unitários (JUnit 5 + Mockito):
  - `AgendamentoUseCaseImplTest` (8): criação válida, publicação do evento Kafka na criação, CPF inválido, cancelamento sem observação, nome curto, data no passado, atualização de status de agendamento inexistente (404) e alteração de status de agendamento já cancelado (409).
  - `KafkaAgendamentoEventPublisherTest` (2): falha assíncrona e falha síncrona do `KafkaTemplate.send` nunca derrubam a criação do agendamento.
  - `GlobalExceptionHandlerTest` (1): exceção inesperada mapeada para 500.
  - `HomeControllerTest` (1): endpoint raiz.
- 7 testes de integração (`@SpringBootTest` + `MockMvc` + `@EmbeddedKafka`), subindo o contexto Spring completo com um broker Kafka em memória (sem depender de Docker): fluxo criar → buscar → atualizar status, 404 para agendamento inexistente, listagem paginada, listagem filtrada por status, rejeição de payload inválido, rejeição de agendamento duplicado (409) e rejeição de cancelamento sem observação (400).

### Testando manualmente

A pasta [`http/agendamentos.http`](http/agendamentos.http) tem requisições prontas pra todos os endpoints e regras de negócio (sucesso, 400, 404, 409), no formato aceito pela extensão **REST Client** (VS Code) e pelo **HTTP Client** nativo do IntelliJ/JetBrains. Basta abrir o arquivo com a aplicação rodando e clicar em "Send Request" em cada bloco.

## Exemplos de requisições

### Criar agendamento
```bash
curl -X POST http://localhost:8080/api/v1/agendamentos \
  -H 'Content-Type: application/json' \
  -d '{
    "pacienteNome": "Maria Silva",
    "pacienteCpf": "12345678901",
    "dataAgendamento": "2030-01-01T10:30:00",
    "observacao": "Consulta inicial"
  }'
```

### Listar agendamentos (paginado)
```bash
curl "http://localhost:8080/api/v1/agendamentos?pagina=0&tamanho=20"
```
`status`, `pagina` (padrão `0`) e `tamanho` (padrão `20`) são opcionais. A resposta traz `conteudo`, `pagina`, `tamanho`, `totalElementos` e `totalPaginas`.

### Buscar por ID
```bash
curl http://localhost:8080/api/v1/agendamentos/{id}
```

### Atualizar status
```bash
curl -X PATCH http://localhost:8080/api/v1/agendamentos/{id}/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"CANCELADO","observacao":"Cancelado por solicitação"}'
```

## Decisões arquiteturais
- Estrutura hexagonal seguindo exatamente o pacote exigido: `entities`, `usecases/ports/{in,out}`, `usecases/impl`, `adapters/{in/controller, out/persistence, out/messaging}`, `frameworks/{spring, config, exceptions}`.
- Regras de negócio centralizadas em casos de uso para manter a entidade de domínio independente de frameworks (a classe `Agendamento` não importa nada de Spring/JPA).
- Exceções de domínio próprias em `frameworks/exceptions` (`AgendamentoNaoEncontradoException`, `RegraDeNegocioException`, `OperacaoNaoPermitidaException`), mapeadas pelo `GlobalExceptionHandler` para 404, 400 e 409 respectivamente. Um handler genérico de `Exception` cobre falhas inesperadas com 500, mantendo o envelope de resposta consistente em qualquer erro.
- Persistência via JPA com H2 para facilitar execução local e testes.
- Respostas de API padronizadas com envelope contendo data, message e timestamp.
- Publicação de evento Kafka (`agendamento-criado`) desacoplada via port de saída `AgendamentoEventPublisherPort`; o adapter `KafkaAgendamentoEventPublisher` é a única peça que conhece Kafka.
- Java 25 + Spring Boot 4.1.0 (Spring Framework 7): versão mais recente estável disponível no momento, compatível com o baseline do desafio (Java 21+ / Spring Boot 3.x).
- Paginação na listagem sem vazar Spring Data para dentro dos casos de uso: `PaginaResultado<T>` é um record em `entities` (zero dependência externa), devolvido pelos ports `BuscarAgendamentoUseCase`/`AgendamentoRepositoryPort`. Só o adapter de persistência (`AgendamentoRepositoryAdapter`) conhece `Pageable`/`Page` do Spring Data; o controller converte o resultado para `PaginaResponse<AgendamentoResponse>`.
- `@ConfigurationProperties` (`AgendamentoProperties`, prefixo `agendamento`) externaliza o nome do tópico Kafka (`agendamento.kafka.topico-criado`), usado tanto na criação do tópico (`KafkaTopicConfig`) quanto na publicação (`KafkaAgendamentoEventPublisher`) — uma única fonte de verdade em vez de string duplicada.
- **Idempotência**: constraint única `(paciente_cpf, data_agendamento)` em `AgendamentoEntity` impede dois agendamentos pro mesmo paciente no mesmo horário. `AgendamentoRepositoryAdapter` usa `saveAndFlush` pra capturar `DataIntegrityViolationException` na hora e traduzir pra `OperacaoNaoPermitidaException` (409), em vez de deixar vazar exceção de infraestrutura pro cliente.
- **Resiliência**: `KafkaAgendamentoEventPublisher` registra um `.whenComplete()` no `CompletableFuture` do `KafkaTemplate.send`, logando sucesso (`DEBUG`, com partição/offset) ou falha (`ERROR`) explicitamente. Isso cobre falhas *assíncronas* (broker aceita a conexão mas rejeita a mensagem depois), mas `KafkaProducer.send()` também pode falhar de forma **síncrona**: na primeira chamada, ele bloqueia a thread buscando metadata do cluster e lança `TimeoutException` direto (não via o future) se o broker estiver totalmente inacessível — sem tratar isso à parte, a request HTTP travava até `max.block.ms` (60s por padrão) e devolvia 500 mesmo com o agendamento já salvo no banco. Corrigido com `max.block.ms=3000` no producer e um `try/catch` em volta do `send()`: falha de publish nunca mais derruba a criação do agendamento, só fica logada.
- **Observabilidade**: `spring-boot-starter-actuator` expõe `/actuator/health` e `/actuator/info` (nome e descrição da aplicação via `info.app.*`, com `management.info.env.enabled=true`).

### Limitação conhecida: consistência entre banco e Kafka

`AgendamentoUseCaseImpl.criar` salva o agendamento e só depois publica o evento — são duas operações separadas, sem transação distribuída entre elas. O tratamento de resiliência cobre falha *durante* a tentativa de publish (broker fora do ar, timeout), mas não cobre o processo morrer exatamente entre o commit do banco e a chamada ao publisher (crash, OOM, deploy no meio): nesse cenário o agendamento fica salvo e o evento nunca é sequer tentado, sem log ou rastro do que devia acontecer. Fechar essa lacuna de verdade exigiria o padrão **Transactional Outbox** (gravar o evento numa tabela na mesma transação do `INSERT`, e um poller/CDC publicando a partir dali) — fora do escopo desta entrega.

## Kafka

O tópico `agendamento-criado` é criado automaticamente na subida da aplicação (bean `NewTopic`). Para inspecionar as mensagens publicadas, com o Kafka já rodando via Docker:

```bash
docker exec <container_kafka> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic agendamento-criado --from-beginning
```

## Docker Compose

Para subir a aplicação junto com Kafka:

```bash
docker compose up --build
```

Serviços disponibilizados:
- Aplicação Java em http://localhost:8080
- Kafka em localhost:9092
- Zookeeper em localhost:2181
- Kafka UI em http://localhost:8081

O `docker-compose.yml` anuncia o listener do Kafka como `kafka:9092`, endereço resolvível pelo nome do serviço dentro da rede do compose — é assim que o container `app` consegue publicar no `agendamento-criado` sem sair para o host. Validado subindo a stack inteira (`docker compose up -d`) e consumindo o tópico com `kafka-console-consumer` logo após um `POST /api/v1/agendamentos`.
