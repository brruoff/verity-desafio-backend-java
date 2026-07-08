package com.desafio.agendamento;

import com.desafio.agendamento.frameworks.spring.AgendamentoApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AgendamentoApplication.class)
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = "agendamento-criado")
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class AgendamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonNode criar(String nome, String cpf, String dataAgendamento) throws Exception {
        String body = objectMapper.writeValueAsString(new AgendamentoPayload(nome, cpf, dataAgendamento, null));
        MvcResult result = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    @Test
    void deveCriarBuscarEAtualizarStatusDeAgendamento() throws Exception {
        JsonNode criado = criar("Maria Integração", "12345678901", "2030-01-01T10:00:00");
        String id = criado.get("data").get("id").asText();
        assertEquals("PENDENTE", criado.get("data").get("status").asText());

        mockMvc.perform(get("/api/v1/agendamentos/" + id))
                .andExpect(status().isOk());

        MvcResult atualizado = mockMvc.perform(patch("/api/v1/agendamentos/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode corpoAtualizado = objectMapper.readTree(atualizado.getResponse().getContentAsString());
        assertEquals("CONFIRMADO", corpoAtualizado.get("data").get("status").asText());
    }

    @Test
    void deveRetornar404ParaAgendamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveListarAgendamentosPaginado() throws Exception {
        criar("Paciente Pagina Um", "11111111111", "2031-02-01T10:00:00");
        criar("Paciente Pagina Dois", "22222222222", "2031-02-02T10:00:00");

        MvcResult result = mockMvc.perform(get("/api/v1/agendamentos?pagina=0&tamanho=1"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
        assertEquals(1, data.get("conteudo").size());
        assertEquals(1, data.get("tamanho").asInt());
        assertTrue(data.get("totalElementos").asLong() >= 2);
    }

    @Test
    void deveListarAgendamentosFiltrandoPorStatus() throws Exception {
        criar("Paciente Filtro Status", "33322211100", "2031-03-03T10:00:00");

        MvcResult result = mockMvc.perform(get("/api/v1/agendamentos?status=PENDENTE&pagina=0&tamanho=50"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode conteudo = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("conteudo");
        assertTrue(conteudo.size() >= 1);
        for (JsonNode agendamento : conteudo) {
            assertEquals("PENDENTE", agendamento.get("status").asText());
        }
    }

    @Test
    void deveRejeitarCriacaoComDadosInvalidos() throws Exception {
        String body = objectMapper.writeValueAsString(new AgendamentoPayload("Jo", "123", "2031-01-01T10:00:00", null));

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarAgendamentoDuplicadoParaMesmoPacienteEHorario() throws Exception {
        String body = objectMapper.writeValueAsString(
                new AgendamentoPayload("Paciente Duplicado", "99988877766", "2032-03-03T09:00:00", null));

        mockMvc.perform(post("/api/v1/agendamentos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        MvcResult duplicado = mockMvc.perform(post("/api/v1/agendamentos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andReturn();

        JsonNode corpo = objectMapper.readTree(duplicado.getResponse().getContentAsString());
        assertEquals("Já existe um agendamento para este paciente neste horário", corpo.get("message").asText());
    }

    @Test
    void deveRejeitarCancelamentoSemObservacao() throws Exception {
        JsonNode criado = criar("Paciente Sem Observacao", "44455566677", "2032-06-06T09:00:00");
        String id = criado.get("data").get("id").asText();

        MvcResult resposta = mockMvc.perform(patch("/api/v1/agendamentos/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELADO\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode corpo = objectMapper.readTree(resposta.getResponse().getContentAsString());
        assertEquals("A observação é obrigatória para cancelar um agendamento", corpo.get("message").asText());
    }

    private record AgendamentoPayload(String pacienteNome, String pacienteCpf, String dataAgendamento, String observacao) {}
}
