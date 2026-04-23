package com.example.java_league.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bid implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long value;
    private ZonedDateTime date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", insertable = false, updatable = false)
    private Team team;

    @NotNull
    @Column(name = "team_id")
    private Long teamId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    @NotNull
    @Column(name = "player_id")
    private Long playerId;

    public Bid(Long value, Long teamId, Long playerId) {
        this.value = value;
        this.teamId = teamId;
        this.playerId = playerId;
        this.date = ZonedDateTime.now();
    }
}
