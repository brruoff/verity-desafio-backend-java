package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.entities.Agendamento;
import com.desafio.agendamento.entities.PaginaResultado;
import com.desafio.agendamento.entities.StatusAgendamento;
import com.desafio.agendamento.frameworks.exceptions.AgendamentoNaoEncontradoException;
import com.desafio.agendamento.frameworks.exceptions.ApiResponse;
import com.desafio.agendamento.usecases.ports.in.AtualizarStatusAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.in.BuscarAgendamentoUseCase;
import com.desafio.agendamento.usecases.ports.in.CriarAgendamentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agendamentos")
@Tag(name = "Agendamentos", description = "Operações para gestão de agendamentos domiciliares")
public class AgendamentoController {
    private final CriarAgendamentoUseCase criarUseCase;
    private final BuscarAgendamentoUseCase buscarUseCase;
    private final AtualizarStatusAgendamentoUseCase atualizarStatusUseCase;

    public AgendamentoController(
            CriarAgendamentoUseCase criarUseCase,
            BuscarAgendamentoUseCase buscarUseCase,
            AtualizarStatusAgendamentoUseCase atualizarStatusUseCase
    ) {
        this.criarUseCase = criarUseCase;
        this.buscarUseCase = buscarUseCase;
        this.atualizarStatusUseCase = atualizarStatusUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar agendamento", description = "Cria um novo agendamento com validação das regras de negócio")
    public ApiResponse<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest request) {
        Agendamento agendamento = Agendamento.novo(
                request.pacienteNome(),
                request.pacienteCpf(),
                request.dataAgendamento(),
                request.observacao()
        );
        return ApiResponse.success(
                AgendamentoResponse.from(criarUseCase.criar(agendamento)),
                "Agendamento criado com sucesso"
        );
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos", description = "Lista agendamentos de forma paginada, com filtro opcional por status")
    public ApiResponse<PaginaResponse<AgendamentoResponse>> listar(
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho
    ) {
        PaginaResultado<Agendamento> resultado = buscarUseCase.listar(Optional.ofNullable(status), pagina, tamanho);
        return ApiResponse.success(
                PaginaResponse.from(resultado, AgendamentoResponse::from),
                "Agendamentos listados com sucesso"
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID", description = "Retorna um agendamento específico pelo identificador")
    public ApiResponse<AgendamentoResponse> buscarPorId(@PathVariable UUID id) {
        AgendamentoResponse response = buscarUseCase.buscarPorId(id)
                .map(AgendamentoResponse::from)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));
        return ApiResponse.success(response, "Agendamento encontrado com sucesso");
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do agendamento", description = "Atualiza o status e a observação do agendamento")
    public ApiResponse<AgendamentoResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        AgendamentoResponse response = AgendamentoResponse.from(
                atualizarStatusUseCase.atualizarStatus(id, request.status(), request.observacao())
        );
        return ApiResponse.success(response, "Status atualizado com sucesso");
    }
}
