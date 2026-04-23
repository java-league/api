package com.example.java_league.event;

import com.example.java_league.dto.BidResponseDTO;
import org.springframework.context.ApplicationEvent;

public class BidProcessedEvent extends ApplicationEvent {

    private final BidResponseDTO bidResponse;

    public BidProcessedEvent(Object source, BidResponseDTO bidResponse) {
        super(source);
        this.bidResponse = bidResponse;
    }

    public BidResponseDTO bidResponse() {
        return bidResponse;
    }
}
