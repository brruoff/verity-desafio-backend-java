package com.desafio.agendamento.entities;

import java.util.List;

public record PaginaResultado<T>(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
}
