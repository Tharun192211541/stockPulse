package com.zycus.StockPulse.commerce;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class CommerceStrategyHolder {
    private final AtomicReference<CommerceStrategy> currentStrategy =
            new AtomicReference<>(CommerceStrategy.RULE);

    public CommerceStrategy getCurrentStrategy() { return currentStrategy.get(); }
    public void setStrategy(CommerceStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("Strategy is required");
        currentStrategy.set(strategy);
    }

    public enum CommerceStrategy { RULE, AI }
}
