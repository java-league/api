package com.example.java_league.service;

import com.example.java_league.domain.User;
import com.example.java_league.enums.UserRole;
import com.example.java_league.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    void loadUserByUsername_usuarioExistente_retornaUserDetails() {
        User user = new User("jogador1", "encoded-password", UserRole.USER);
        when(repository.findByLogin("jogador1")).thenReturn(user);

        UserDetails result = authorizationService.loadUserByUsername("jogador1");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("jogador1");
    }

    @Test
    void loadUserByUsername_usuarioNaoEncontrado_lancaUsernameNotFoundException() {
        when(repository.findByLogin("desconhecido")).thenReturn(null);

        assertThatThrownBy(() -> authorizationService.loadUserByUsername("desconhecido"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("desconhecido");
    }

    @Test
    void loadUserByUsername_usuarioAdmin_retornaAuthoritiesDeAdmin() {
        User admin = new User("admin", "encoded-password", UserRole.ADMIN);
        when(repository.findByLogin("admin")).thenReturn(admin);

        UserDetails result = authorizationService.loadUserByUsername("admin");

        assertThat(result.getAuthorities())
                .extracting(a -> a.getAuthority())
                .contains("ROLE_ADMIN", "ROLE_USER");
    }
}
