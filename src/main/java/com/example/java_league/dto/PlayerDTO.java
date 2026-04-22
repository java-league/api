package com.example.java_league.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {

    private Long id;
    private String name;
    private Long overall;
    private Long price;
    private String imageUrl;
    private Long teamId;
    private Long priceLimit;
    private Boolean hasBidForTeam;
}
