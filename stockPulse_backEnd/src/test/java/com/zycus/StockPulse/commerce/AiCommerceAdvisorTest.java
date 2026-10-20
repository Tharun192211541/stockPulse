package com.zycus.StockPulse.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zycus.StockPulse.ai.LLMGateway;
import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.suggestion.PriceDirection;
import com.zycus.StockPulse.suggestion.PricingSuggestion;
import com.zycus.StockPulse.suggestion.TriggerReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiCommerceAdvisorTest {
    private LLMGateway gateway;
    private RuleBasedCommerceAdvisor fallback;
    private ProductRepository products;
    private AiCommerceAdvisor advisor;
    private Product product;

    @BeforeEach void setUp() {
        gateway = mock(LLMGateway.class);
        fallback = mock(RuleBasedCommerceAdvisor.class);
        products = mock(ProductRepository.class);
        advisor = new AiCommerceAdvisor(gateway, new ObjectMapper(), fallback, products);
        product = new Product("SKU-1", "Keyboard", ProductCategory.ELECTRONICS);
        product.setCurrentPrice(new BigDecimal("100.00"));
        product.setStockLevel(12);
        product.setReorderThreshold(5);
        product.setDemandVelocity(9);
        when(products.findByCategory(ProductCategory.ELECTRONICS)).thenReturn(List.of(product));
    }

    @Test void validAiPricingIsAcceptedWhenDirectionMatches() {
        when(gateway.callLLM(anyString())).thenReturn("{\"recommendedPrice\":110,\"direction\":\"INCREASE\",\"confidence\":0.8,\"reasoning\":\"Demand is elevated\"}");
        PricingSuggestion result = advisor.generatePricingSuggestion(product, TriggerReason.DEMAND_SPIKE);
        assertEquals(new BigDecimal("110.00"), result.getRecommendedPrice());
        assertEquals(PriceDirection.INCREASE, result.getDirection());
        verify(fallback, never()).generatePricingSuggestion(any(), any());
    }

    @Test void invalidDirectionFallsBackToRules() {
        PricingSuggestion expected = new PricingSuggestion(product, product.getCurrentPrice(), new BigDecimal("110.00"),
                PriceDirection.INCREASE, new BigDecimal("0.9"), "Rule fallback", TriggerReason.DEMAND_SPIKE);
        when(gateway.callLLM(anyString())).thenReturn("{\"recommendedPrice\":110,\"direction\":\"DECREASE\",\"confidence\":0.8,\"reasoning\":\"wrong\"}");
        when(fallback.generatePricingSuggestion(product, TriggerReason.DEMAND_SPIKE)).thenReturn(expected);
        assertSame(expected, advisor.generatePricingSuggestion(product, TriggerReason.DEMAND_SPIKE));
    }

    @Test void gatewayFailureFallsBackToRules() {
        PricingSuggestion expected = new PricingSuggestion(product, product.getCurrentPrice(), product.getCurrentPrice(),
                PriceDirection.HOLD, new BigDecimal("0.6"), "Rule fallback", TriggerReason.MANUAL);
        when(gateway.callLLM(anyString())).thenThrow(new IllegalStateException("timeout"));
        when(fallback.generatePricingSuggestion(product, TriggerReason.MANUAL)).thenReturn(expected);
        assertSame(expected, advisor.generatePricingSuggestion(product, TriggerReason.MANUAL));
    }
}
