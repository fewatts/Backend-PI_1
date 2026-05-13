package com.api.reserva.condominio;

import com.api.reserva.condominio.domain.model.Reserva;
import com.api.reserva.condominio.domain.model.Usuario;
import com.api.reserva.condominio.domain.model.Espaco;
import com.api.reserva.condominio.domain.repository.ReservaRepository;
import com.api.reserva.condominio.domain.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "api.security.token.secret=minha-chave-secreta-de-teste-12345678901234567890")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaEAuthTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ReservaRepository reservaRepository;

    // Método auxiliar para não repetir código de login
    private String obterToken(String login, String senha) throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\""+login+"\",\"senha\":\""+senha+"\"}"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    @DisplayName("Fluxo completo: Registro, Login e Verificação de Permissão de Exclusão")
    void fluxoCompleto() throws Exception {
        // 1. Registro
        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"userA\",\"senha\":\"123\"}")).andExpect(status().isOk());

        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"userB\",\"senha\":\"123\"}")).andExpect(status().isOk());

        // 2. Tokens
        String tokenA = obterToken("userA", "123");
        String tokenB = obterToken("userB", "123");

        // 3. Criar reserva para User A
        Usuario userA = (Usuario) usuarioRepository.findByLogin("userA");
        Reserva resA = reservaRepository.save(new Reserva(null, userA, Espaco.SALAO_DE_FESTAS, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(3)));

        // 4. B tenta deletar A (ERRO)
        mockMvc.perform(delete("/reservas/" + resA.getId())
                .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest());

        // 5. A deleta A (SUCESSO)
        mockMvc.perform(delete("/reservas/" + resA.getId())
                .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Validação: Não deve permitir reservas duplicadas no mesmo horário")
    void validarConflitoHorario() throws Exception {
        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"userC\",\"senha\":\"123\"}"));
        String token = obterToken("userC", "123");
        
        LocalDateTime data = LocalDateTime.now().plusDays(10);
        reservaRepository.save(new Reserva(null, null, Espaco.CHURRASQUEIRA_LAZER, data, data.plusHours(2)));

        String jsonConflito = "{\"espaco\": \"CHURRASQUEIRA_LAZER\", \"dataHoraInicio\": \""+data+"\", \"dataHoraFim\": \""+data.plusHours(2)+"\"}";

        mockMvc.perform(post("/reservas")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonConflito))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN: Deve conseguir deletar qualquer reserva")
    void adminDeletaQualquerUma() throws Exception {
        // Criar Admin
        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"adminRoot\",\"senha\":\"admin\",\"role\":\"ADMIN\"}"));
        String tokenAdmin = obterToken("adminRoot", "admin");

        // Criar Reserva de um user qualquer
        Reserva res = reservaRepository.save(new Reserva(null, null, Espaco.QUADRA, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(1)));

        // Admin deleta
        mockMvc.perform(delete("/reservas/" + res.getId())
                .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("API: Deve retornar erro 403 se tentar acessar sem token")
    void erroSemToken() throws Exception {
        mockMvc.perform(get("/reservas")).andExpect(status().isForbidden());
    }
}
