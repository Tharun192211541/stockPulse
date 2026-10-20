package com.zycus.StockPulse.event;

import com.zycus.StockPulse.suggestion.TriggerReason;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventService {

    private final ApplicationEventPublisher eventPublisher;

    public InventoryEventService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishInventoryEvent(Long productId, TriggerReason triggerReason) {
        InventoryEvent event = new InventoryEvent(productId, triggerReason);
        eventPublisher.publishEvent(event);
    }
}