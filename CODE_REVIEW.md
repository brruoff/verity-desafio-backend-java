# Retorno do processo seletivo (Fullstack) e correções aplicadas

## Retorno recebido

> **Java**: Entregou o desafio corretamente. Demonstrou atenção e maturidade ao utilizar a versão 25 do Java e 4.1 do Spring Boot. Foi entregue todos os 4 endpoints, todas as regras de negócio, todos os diferenciais pedidos. Fez testes de integração com `@EmbeddedKafka` (Kafka em memória, sem Docker).
>
> Pontos a melhorar:
> 1. `ApiResponse` está em `frameworks.exceptions` — semanticamente errado; é um contrato de API, deveria estar em `adapters.in.controller` ou num pacote compartilhado.
> 2. No `GlobalExceptionHandler`, o handler de validação usa `ApiResponse.success(errors, "Dados inválidos")` — chama `.success()` para uma resposta de erro, o que é conceitualmente incorreto.
> 3. `AgendamentoUseCaseImpl` implementa 3 interfaces simultaneamente (Criar, Buscar, AtualizarStatus) — viola princípio de responsabilidade única.
> 4. Exceções de domínio (`RegraDeNegocioException`, `OperacaoNaoPermitidaException`) estão em `frameworks.exceptions`, porém a regra de negócio lançando exceção deveria pertencer à camada de `usecases` ou `entities`.
> 5. `import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` — caminho de import mudou no Spring Boot 4; pode não compilar dependendo da versão exata usada.

## O que foi corrigido

| # | Ponto | Ação |
|---|---|---|
| 1 | `ApiResponse` fora de lugar | Movido para `adapters.in.controller` (mesmo pacote do `AgendamentoController`, que é quem monta o envelope). |
| 2 | `.success()` em resposta de erro | `GlobalExceptionHandler.handleValidation` agora usa `ApiResponse.error(errors, "Dados inválidos")`. |
| 3 | Use case violando SRP | `AgendamentoUseCaseImpl` foi quebrado em `CriarAgendamentoUseCaseImpl`, `BuscarAgendamentoUseCaseImpl` e `AtualizarStatusAgendamentoUseCaseImpl` — uma classe por porta de entrada (`usecases/ports/in`), cada uma com uma única razão para mudar. `BeanConfig` passou a expor os 3 beans. |
| 4 | Exceções de domínio no pacote errado | `AgendamentoNaoEncontradoException`, `OperacaoNaoPermitidaException` e `RegraDeNegocioException` movidas para `usecases.exceptions`. `frameworks.exceptions` ficou só com o `GlobalExceptionHandler` (que é, de fato, infraestrutura de framework). |
| 5 | Import do `AutoConfigureMockMvc` | Verificado contra a documentação oficial do Spring Boot v4.1.0: `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc` é o pacote correto a partir da 4.0.0, e é exatamente o que o projeto usa (parent `4.1.0` + dependency `spring-boot-webmvc-test`). Sem alteração — o import já estava certo para esta versão. |

## Nota sobre o ponto 3

O desafio original só exige as interfaces `CriarAgendamentoUseCase` e `BuscarAgendamentoUseCase` em `ports/in` (ver `Desafio-Tecnico-Desenvolvedor-Java.pdf`), sendo que `BuscarAgendamentoUseCase` já agrupa `listar` e `buscarPorId` por design. A correção separou a implementação em uma classe por interface existente (3 classes), resolvendo a violação de SRP relatada, sem quebrar `BuscarAgendamentoUseCase` em duas interfaces novas só para bater um número específico de classes.

## Nota sobre o ponto 4

Aplicado o retorno do avaliador: as exceções de domínio saíram de `frameworks.exceptions` para `usecases.exceptions`.

Suíte completa validada após as correções: `./mvnw test` — 19 testes, 0 falhas.
