package com.zycus.StockPulse.commerce;

import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.suggestion.PriceDirection;
import com.zycus.StockPulse.suggestion.PricingSuggestion;
import com.zycus.StockPulse.suggestion.ReorderSuggestion;
import com.zycus.StockPulse.suggestion.TriggerReason;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class RuleBasedCommerceAdvisor implements CommerceAdvisor {

    private static final BigDecimal CONFIDENCE_HIGH = new BigDecimal("0.90");
    private static final BigDecimal CONFIDENCE_MEDIUM = new BigDecimal("0.75");
    private static final BigDecimal CONFIDENCE_LOW = new BigDecimal("0.60");

    @Override
    public PricingSuggestion generatePricingSuggestion(Product product, TriggerReason triggerReason) {
        BigDecimal currentPrice = product.getCurrentPrice() != null ? product.getCurrentPrice() : BigDecimal.ZERO;
        BigDecimal recommendedPrice = currentPrice;
        PriceDirection direction = PriceDirection.HOLD;
        BigDecimal confidence = CONFIDENCE_LOW;
        String reasoning = "Default hold recommendation";

        // Rule 1: If stock is below reorder threshold, increase price by 10%
        if (product.isLowInventory()) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = CONFIDENCE_HIGH;
            reasoning = "Low inventory detected. Recommended 10% price increase to balance supply and demand.";
        }
        // Rule 2: If demand velocity exceeds twice the category average, increase price by 5%
        else if (product.isDemandSpike(getCategoryAverageDemand(product.getCategory()))) {
            recommendedPrice = currentPrice.multiply(new BigDecimal("1.05")).setScale(2, RoundingMode.HALF_UP);
            direction = PriceDirection.INCREASE;
            confidence = CONFIDENCE_MEDIUM;
            reasoning = "High demand detected. Recommended 5% price increase to optimize revenue.";
        }
        // Rule 3: Otherwise, hold the price
        else {
            direction = PriceDirection.HOLD;
            confidence = CONFIDENCE_LOW;
            reasoning = "Stable inventory and demand levels. No price adjustment recommended.";
        }

        // Ensure price is positive
        if (recommendedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            recommendedPrice = currentPrice.max(BigDecimal.ONE);
        }

        return new PricingSuggestion(product, currentPrice, recommendedPrice, direction, confidence, reasoning, triggerReason);
    }

    @Override
    public ReorderSuggestion generateReorderSuggestion(Product product, TriggerReason triggerReason) {
        Integer currentStock = product.getStockLevel();
        Integer reorderThreshold = product.getReorderThreshold();
        
        // Rule: recommendedQuantity = max(1, reorderThreshold * 3 - currentStock)
        Integer recommendedQuantity = Math.max(1, reorderThreshold * 3 - currentStock);
        Integer suggestedLeadTimeDays = 7; // Default lead time
        BigDecimal confidence = CONFIDENCE_HIGH;
        String reasoning = "Calculated reorder quantity based on threshold and current stock.";

        return new ReorderSuggestion(product, currentStock, recommendedQuantity, suggestedLeadTimeDays, confidence, reasoning, triggerReason);
    }

    private Integer getCategoryAverageDemand(ProductCategory category) {
        // Simplified implementation - in a real system, this would come from historical data
        switch (category) {
            case ELECTRONICS:
                return 10;
            case APPAREL:
                return 20;
            case HOME:
                return 15;
            default:
                return 10;
        }
    }
}