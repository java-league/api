package com.example.java_league.security;

import com.example.java_league.security.jwt.JWTUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only-32+";
    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
    }

    private JWTUser buildUser(Long id, String login, Long teamId) {
        return new JWTUser(id, login, "", teamId);
    }

    @Test
    void generateToken_usuarioValido_retornaTokenNaoNulo() {
        JWTUser user = buildUser(1L, "jogador1", 10L);

        String token = tokenService.generateToken(user);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_tokensDistintos_paraDiferentesUsuarios() {
        JWTUser user1 = buildUser(1L, "jogador1", 10L);
        JWTUser user2 = buildUser(2L, "jogador2", 20L);

        String token1 = tokenService.generateToken(user1);
        String token2 = tokenService.generateToken(user2);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void validateToken_tokenValido_retornaLogin() {
        JWTUser user = buildUser(1L, "jogador1", 10L);
        String token = tokenService.generateToken(user);

        String login = tokenService.validateToken(token);

        assertThat(login).isEqualTo("jogador1");
    }

    @Test
    void validateToken_tokenInvalido_retornaStringVazia() {
        String login = tokenService.validateToken("token.invalido.qualquer");

        assertThat(login).isEmpty();
    }

    @Test
    void validateToken_tokenAlterado_retornaStringVazia() {
        JWTUser user = buildUser(1L, "jogador1", 10L);
        String token = tokenService.generateToken(user);
        String tokenAlterado = token.substring(0, token.length() - 5) + "XXXXX";

        String login = tokenService.validateToken(tokenAlterado);

        assertThat(login).isEmpty();
    }

    @Test
    void validateToken_tokenComSegredoDiferente_retornaStringVazia() {
        TokenService outroService = new TokenService();
        ReflectionTestUtils.setField(outroService, "secret", "outro-segredo-completamente-diferente-32+");
        JWTUser user = buildUser(1L, "jogador1", 10L);
        String tokenDeOutroServico = outroService.generateToken(user);

        String login = tokenService.validateToken(tokenDeOutroServico);

        assertThat(login).isEmpty();
    }
}
