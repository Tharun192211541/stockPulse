package com.zycus.StockPulse.commerce;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommerceStrategyTest {
    @Test void factorySwitchesAtRuntime() {
        RuleBasedCommerceAdvisor rule = mock(RuleBasedCommerceAdvisor.class);
        AiCommerceAdvisor ai = mock(AiCommerceAdvisor.class);
        CommerceStrategyHolder holder = new CommerceStrategyHolder();
        CommerceAdvisorFactory factory = new CommerceAdvisorFactory(rule, ai, holder);
        assertSame(rule, factory.getAdvisor());
        holder.setStrategy(CommerceStrategyHolder.CommerceStrategy.AI);
        assertSame(ai, factory.getAdvisor());
        holder.setStrategy(CommerceStrategyHolder.CommerceStrategy.RULE);
        assertSame(rule, factory.getAdvisor());
    }

    @Test void rejectsNullStrategy() {
        CommerceStrategyHolder holder = new CommerceStrategyHolder();
        assertThrows(IllegalArgumentException.class, () -> holder.setStrategy(null));
    }
}
