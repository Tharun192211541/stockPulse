package com.zycus.StockPulse.event;

import com.zycus.StockPulse.suggestion.TriggerReason;

public class InventoryEvent {
    private Long productId;
    private TriggerReason triggerReason;

    public InventoryEvent(Long productId, TriggerReason triggerReason) {
        this.productId = productId;
        this.triggerReason = triggerReason;
    }

    public Long getProductId() {
        return productId;
    }

    public TriggerReason getTriggerReason() {
        return triggerReason;
    }
}