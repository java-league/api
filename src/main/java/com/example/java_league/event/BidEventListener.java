package com.example.java_league.event;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class BidEventListener {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public BidEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @EventListener
    public void onBidProcessed(BidProcessedEvent event) {
        simpMessagingTemplate.convertAndSend("/topic/bid", event.bidResponse());
    }
}
