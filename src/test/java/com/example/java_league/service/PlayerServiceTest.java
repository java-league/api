package com.example.java_league.service;

import com.example.java_league.domain.Bid;
import com.example.java_league.domain.Player;
import com.example.java_league.domain.Team;
import com.example.java_league.dto.BidResponseDTO;
import com.example.java_league.dto.PlayerDTO;
import com.example.java_league.enums.BidType;
import com.example.java_league.mapper.PlayerMapper;
import com.example.java_league.repository.BidRepository;
import com.example.java_league.repository.PlayerRepository;
import com.example.java_league.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.example.java_league.event.BidProcessedEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PlayerService playerService;

    private Player buildPlayer(Long id, Long price) {
        Player p = new Player();
        p.setId(id);
        p.setPrice(price);
        return p;
    }

    private Team buildTeam(Long id, Long javalis) {
        Team t = new Team();
        t.setId(id);
        t.setJavalis(javalis);
        return t;
    }

    @Test
    void bid_semLanceAnterior_debitaTimeEAtribuiJogador() {
        Player player = buildPlayer(1L, 5000L);
        Team team = buildTeam(10L, 250000L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(bidRepository.findFirstByPlayerIdOrderByValueDesc(1L)).thenReturn(null);
        when(bidRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BidResponseDTO result = playerService.bid(5000L, 10L, 1L);

        assertThat(result.message()).isEqualTo(BidType.FIRST_BID);
        assertThat(result.teamIdHighest()).isEqualTo(10L);
        assertThat(team.getJavalis()).isEqualTo(245000L);  // 250000 - 5000
        assertThat(player.getPrice()).isEqualTo(5100L);    // 5000 + 100
        assertThat(player.getTeam()).isEqualTo(team);
        verify(bidRepository).save(any(Bid.class));
        verify(playerRepository).save(player);
        verify(eventPublisher).publishEvent(any(BidProcessedEvent.class));
    }

    @Test
    void bid_lanceAcimaDeMaiorLance_transfereJogadorECreditaTimeAnterior() {
        Team oldTeam = buildTeam(10L, 200000L);
        Team newTeam = buildTeam(20L, 250000L);
        Player player = buildPlayer(1L, 5100L);
        player.setTeam(oldTeam);

        Bid existingBid = new Bid(5000L, oldTeam.getId(), 1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(20L)).thenReturn(Optional.of(newTeam));
        when(bidRepository.findFirstByPlayerIdOrderByValueDesc(1L)).thenReturn(existingBid);
        when(bidRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BidResponseDTO result = playerService.bid(6000L, 20L, 1L);

        assertThat(result.message()).isEqualTo(BidType.HIGHEST_BID);
        assertThat(result.teamIdHighest()).isEqualTo(20L);
        assertThat(result.teamIdLowest()).isEqualTo(10L);
        // oldTeam recebe crédito de (price - 100) = 5000 -> 200000 + 5000 = 205000
        assertThat(oldTeam.getJavalis()).isEqualTo(205000L);
        // newTeam debita (highestBid.value + 100) = 5100 -> 250000 - 5100 = 244900
        assertThat(newTeam.getJavalis()).isEqualTo(244900L);
        // novo preço = highestBid.value + 200 = 5200
        assertThat(player.getPrice()).isEqualTo(5200L);
        assertThat(player.getTeam()).isEqualTo(newTeam);
        verify(teamRepository).save(oldTeam);
        verify(bidRepository).save(any(Bid.class));
        verify(playerRepository).save(player);
        verify(eventPublisher).publishEvent(any(BidProcessedEvent.class));
    }

    @Test
    void bid_lanceAbaixoDeMaiorLance_cobraTimeDonoEAtualizaPreco() {
        Team currentOwner = buildTeam(10L, 250000L);
        Team bidder = buildTeam(20L, 250000L);
        Player player = buildPlayer(1L, 5100L);
        player.setTeam(currentOwner);

        Bid existingBid = new Bid(5000L, currentOwner.getId(), 1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(20L)).thenReturn(Optional.of(bidder));
        when(bidRepository.findFirstByPlayerIdOrderByValueDesc(1L)).thenReturn(existingBid);

        BidResponseDTO result = playerService.bid(4000L, 20L, 1L);

        assertThat(result.message()).isEqualTo(BidType.LOWEST_BID);
        assertThat(result.teamIdHighest()).isEqualTo(10L);
        assertThat(result.teamIdLowest()).isEqualTo(20L);
        // debitJavalis(4000 - (5100 - 100)) = debitJavalis(-1000) -> javalis aumenta 1000
        assertThat(currentOwner.getJavalis()).isEqualTo(251000L);
        assertThat(player.getPrice()).isEqualTo(4100L); // 4000 + 100
        verify(teamRepository).save(currentOwner);
        verify(playerRepository).save(player);
        verify(eventPublisher).publishEvent(any(BidProcessedEvent.class));
    }

    @Test
    void bid_teamIdNulo_lancaIllegalArgumentException() {
        assertThatThrownBy(() -> playerService.bid(5000L, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team ID is required");
    }

    @Test
    void bid_jogadorNaoEncontrado_lancaEntityNotFoundException() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.bid(5000L, 10L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void bid_timeNaoEncontrado_lancaEntityNotFoundException() {
        Player player = buildPlayer(1L, 5000L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.bid(5000L, 99L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getPlayerById_jogadorExistente_retornaDto() {
        Player player = buildPlayer(1L, 5000L);
        PlayerDTO dto = new PlayerDTO(1L, "Jogador", 80L, 5000L, null, null, null, null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(playerMapper.toDto(player)).thenReturn(dto);

        assertThat(playerService.getPlayerById(1L)).isEqualTo(dto);
    }

    @Test
    void getPlayerById_jogadorNaoEncontrado_lancaEntityNotFoundException() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }
}
