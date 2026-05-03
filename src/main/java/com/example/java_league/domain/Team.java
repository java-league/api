package com.example.java_league.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long javalis;
    private String name;
    private String uniform1;
    private String uniform2;
    private String emblem;
    private String formation;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public void debitJavalis(Long value) {
        this.javalis = this.javalis - value;
    }

    public void creditJavalis(Long value) {
        this.javalis = this.javalis + value;
    }
}
