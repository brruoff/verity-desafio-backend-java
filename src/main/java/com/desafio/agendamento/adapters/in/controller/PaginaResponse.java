package com.desafio.agendamento.adapters.in.controller;

import com.desafio.agendamento.entities.PaginaResultado;

import java.util.List;
import java.util.function.Function;

public record PaginaResponse<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
    public static <T, R> PaginaResponse<R> from(PaginaResultado<T> resultado, Function<T, R> mapper) {
        return new PaginaResponse<>(
                resultado.conteudo().stream().map(mapper).toList(),
                resultado.pagina(),
                resultado.tamanho(),
                resultado.totalElementos(),
                resultado.totalPaginas()
        );
    }
}
