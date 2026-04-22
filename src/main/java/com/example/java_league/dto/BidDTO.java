package com.example.java_league.dto;

import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BidDTO implements Serializable {

    private Long id;
    private Long value;
    private ZonedDateTime date;
    private Long teamId;
    private Long playerId;
}
