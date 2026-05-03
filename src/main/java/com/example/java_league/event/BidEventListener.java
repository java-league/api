package com.example.java_league.event;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BidEventListener {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public BidEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBidProcessed(BidProcessedEvent event) {
        simpMessagingTemplate.convertAndSend("/topic/bid", event.bidResponse());
    }
}
