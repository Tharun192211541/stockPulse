package com.zycus.StockPulse.commerce;

import org.springframework.stereotype.Component;

@Component
public class CommerceAdvisorFactory {
    private final RuleBasedCommerceAdvisor ruleBasedAdvisor;
    private final AiCommerceAdvisor aiCommerceAdvisor;
    private final CommerceStrategyHolder strategyHolder;

    public CommerceAdvisorFactory(RuleBasedCommerceAdvisor ruleBasedAdvisor,
                                  AiCommerceAdvisor aiCommerceAdvisor,
                                  CommerceStrategyHolder strategyHolder) {
        this.ruleBasedAdvisor = ruleBasedAdvisor;
        this.aiCommerceAdvisor = aiCommerceAdvisor;
        this.strategyHolder = strategyHolder;
    }

    public CommerceAdvisor getAdvisor() {
        return strategyHolder.getCurrentStrategy() == CommerceStrategyHolder.CommerceStrategy.AI
                ? aiCommerceAdvisor : ruleBasedAdvisor;
    }
}
