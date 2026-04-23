package com.example.java_league.security;

import com.example.java_league.domain.Team;
import com.example.java_league.domain.User;
import com.example.java_league.enums.UserRole;
import com.example.java_league.repository.TeamRepository;
import com.example.java_league.repository.UserRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private User buildUser(Long id, String login) {
        User user = new User(login, "encoded-password", UserRole.USER);
        user.setId(id);
        return user;
    }

    private Team buildTeam(Long id) {
        Team team = new Team();
        team.setId(id);
        return team;
    }

    @Test
    void doFilter_tokenValido_defineAutenticacaoNoContexto() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = buildUser(1L, "jogador1");
        Team team = buildTeam(10L);

        when(tokenService.validateToken("token-valido")).thenReturn("jogador1");
        when(userRepository.findByLogin("jogador1")).thenReturn(user);
        when(teamRepository.findFirstByUserId(1L)).thenReturn(team);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_tokenQueRetornaLoginVazio_naoDefineAutenticacao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.validateToken("token-invalido")).thenReturn("");

        securityFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_usuarioNaoEncontradoNoBanco_naoDefineAutenticacao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenService.validateToken("token-valido")).thenReturn("desconhecido");
        when(userRepository.findByLogin("desconhecido")).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_semHeaderAuthorization_naoDefineAutenticacao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        securityFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenService, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_usuarioSemTime_defineAutenticacaoComTeamIdZero() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User user = buildUser(1L, "jogador1");

        when(tokenService.validateToken("token-valido")).thenReturn("jogador1");
        when(userRepository.findByLogin("jogador1")).thenReturn(user);
        when(teamRepository.findFirstByUserId(1L)).thenReturn(null);

        securityFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }
}
